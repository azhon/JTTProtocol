package com.azhon.jtt808.netty.live;

import android.util.Log;

import java.net.InetSocketAddress;
import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.netty.live
 * 文件名:    LiveClient
 * 创建时间:  2020/2/26 on 20:51
 * 描述:     TODO 视频监控推流
 *
 * @author 阿钟
 */

public class LiveClient {

    private static final String TAG = "    LiveClient";

    private String ip;
    private int port;
    private Channel channel;

    public LiveClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
        connect();
    }

    /**
     * 初始化连接
     */
    private void connect() {
        try {
            NioEventLoopGroup group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap()
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535))
                    .channel(NioSocketChannel.class)
                    .group(group)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //解码器
                            pipeline.addLast(
                                    new ByteToMessageDecoder() {
                                        @Override
                                        protected void decode(ChannelHandlerContext channelHandlerContext,
                                                              ByteBuf byteBuf, List<Object> list) throws Exception {
                                            Log.d(TAG, "收到了数据：" + byteBuf.readableBytes());
                                        }
                                    }
                                    //编码器
                            ).addLast(
                                    new MessageToByteEncoder<byte[]>() {
                                        @Override
                                        protected void encode(ChannelHandlerContext channelHandlerContext,
                                                              byte[] data, ByteBuf byteBuf) throws Exception {
                                            byteBuf.writeBytes(data);

                                        }
                                    }
                            );
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(ip, port));
            channel = channelFuture.sync().channel();
            Log.d(TAG, "实时监控服务器连接成功");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "实时监控服务器连接失败：" + e.getMessage());
        }
    }

    /**
     * 发送数据
     *
     * @param data
     */
    public synchronized void sendData(byte[] data) {
        if (channel != null) {
            channel.writeAndFlush(data);
        }
    }

    public void release() {
        if (channel != null) {
            channel.disconnect();
        }
    }

}
