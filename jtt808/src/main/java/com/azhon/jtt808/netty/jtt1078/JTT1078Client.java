package com.azhon.jtt808.netty.jtt1078;

import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.netty.JTT808Decoder;
import com.azhon.jtt808.netty.JTT808Encoder;
import com.azhon.jtt808.util.ByteBufUtil;
import com.azhon.jtt808.util.ByteUtil;
import com.azhon.jtt808.util.FileUtil;
import com.azhon.jtt808.util.JTT808Util;
import com.azhon.jtt808.util.Util;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.netty.jtt1078
 * 文件名:    JTT1078Client
 * 创建时间:  2020/2/4 on 16:34
 * 描述:     TODO
 *
 * @author 阿钟
 */

public class JTT1078Client {
    private static final String TAG = "    JTT1078Client";
    private Channel channel;
    private String ip;
    private int port;
    private byte[] alarmIDNumber;
    private byte[] alarmNumber;
    private String alarmNumberStr;
    private List<File> files;
    //=============================//
    private String fileName;
    //当前文件序号，每上传一个需递增
    private int fileNum = 0;
    //当前需要上传的文件
    private File uploadFile;
    //当前上传文件的类型,默认为其它类型
    private int fileType = 0x04;
    //=============================//

    public JTT1078Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }


    /**
     * 初始化连接
     */
    public void connect() {
        try {
            NioEventLoopGroup group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap()
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535))
                    .channel(NioSocketChannel.class)
                    .group(group)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        ByteBuf delimiter = Unpooled.buffer(1);

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            delimiter.writeByte(0x7E);
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new DelimiterBasedFrameDecoder(65535, delimiter));
                            //数据格式与1078一致
                            pipeline.addLast(new JTT808Decoder());
                            pipeline.addLast(new JTT808Encoder());

                            pipeline.addLast(new JTT1078Handler(JTT1078Client.this));
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(ip, port));
            channel = channelFuture.sync().channel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 当与服务器一连接上时，立即上传 报警附件信息消息
     */
    public synchronized void sendBjfjMsg() {
        JTT808Bean fjMsg = JTT808Util.uploadFJMsg(alarmIDNumber, alarmNumber, alarmNumberStr, files);
        channel.writeAndFlush(fjMsg.getData());
    }

    /**
     * 文件信息上传
     * （第二步）
     */
    public void fileMsg() {
        fileName = getFileName();
        if (fileName == null) {
            //没有再需要上传的文件了，主动断开服务器
            channel.disconnect();
            return;
        }
        //序号加1
        fileNum++;
        JTT808Bean fileMsg = JTT808Util.uploadFileMsg(fileType, fileName, uploadFile);
        channel.writeAndFlush(fileMsg.getData());
    }

    /**
     * 开始上传文件
     * （第三步）
     */
    public void uploadFile() {
        generateFilePkg(uploadFile);
        JTT808Bean bean = JTT808Util.uploadFileDone(fileType, fileName, uploadFile);
        channel.writeAndFlush(bean.getData());

    }

    /**
     * 发送每一个文件数据包
     */
    private void generateFilePkg(File file) {
        long length = file.length();
        //每个包的大小
        double everyPkgSize = 1000.d;
        //分包的总包数
        long totalPkg = Math.round(Math.ceil(length / everyPkgSize));
        byte[] fileBytes = FileUtil.file2Bytes(file.getPath());
        if (fileBytes == null) return;
        //文件名称
        byte[] name = createNameBytes();

        for (int i = 1; i <= totalPkg; i++) {
            ByteBuf buffer = Unpooled.buffer();
            //帧头标识
            buffer.writeBytes(new byte[]{0x30, 0x31, 0x63, 0x64});
            buffer.writeBytes(name);

            long end = (long) (i * everyPkgSize);
            if (end >= length) {
                end = length;
            }
            int offset = (int) ((i - 1) * everyPkgSize);
            byte[] bytes = Arrays.copyOfRange(fileBytes, offset, (int) end);
            //偏移量
            buffer.writeBytes(ByteUtil.longToDword(offset));
            //负载数据的长度
            buffer.writeBytes(ByteUtil.longToDword(bytes.length));
            //负载数据的长度
            buffer.writeBytes(bytes);
            //发送数据
            channel.writeAndFlush(ByteBufUtil.toArray(buffer));
        }
    }

    /**
     * 创建50个字节的名字数组
     *
     * @return
     */
    private byte[] createNameBytes() {
        ByteBuf name = Unpooled.buffer(50);
        name.writeBytes(fileName.getBytes());
        name.writerIndex(50);
        return ByteBufUtil.toArray(name);
    }


    /**
     * 设置报警标识号
     *
     * @param alarmIDNumber
     */
    public void setAlarmIDNumber(byte[] alarmIDNumber) {
        this.alarmIDNumber = alarmIDNumber;
    }

    /**
     * 要上传的附件
     *
     * @param files
     */
    public void setFiles(List<File> files) {
        this.files = files;
    }

    /**
     * 报警编号
     *
     * @param alarmNumber
     */
    public void setAlarmNumber(byte[] alarmNumber) {
        this.alarmNumber = alarmNumber;
        alarmNumberStr = new String(ByteUtil.delete0InArray(alarmNumber));
    }

    /**
     * 生成文件名
     * <文件类型>_<通道号>_<报警类型>_<序号>_<报警编号>.<后缀名>
     */
    private String getFileName() {
        if (fileNum < files.size()) {
            uploadFile = files.get(fileNum);
            String type = Util.getFileType(uploadFile);
            String fileNameType = Util.getFileNameType(type);
            fileType = Integer.parseInt(fileNameType);
            return fileNameType + "_0_0_" + fileNum + "_" + alarmNumberStr + type;
        }
        return null;
    }
}

