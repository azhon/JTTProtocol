package com.azhon.jtt808.netty.jtt1078;

import android.util.Log;

import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.util.ByteUtil;
import com.azhon.jtt808.util.HexUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.netty
 * 文件名:    JTT808Handler
 * 创建时间:  2020/1/2 on 22:49
 * 描述:     TODO
 *
 * @author 阿钟
 */

public class JTT1078Handler extends SimpleChannelInboundHandler<JTT808Bean> {
    private static final String TAG = "    JTT1078Handler";
    private JTT1078Client jtt1078Client;

    public JTT1078Handler(JTT1078Client jtt1078Client) {
        this.jtt1078Client = jtt1078Client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, JTT808Bean bean) throws Exception {
        handData(bean);
    }

    /**
     * 处理事件
     *
     * @param bean
     */
    private void handData(JTT808Bean bean) {
        int msgId = ByteUtil.bytes2Int(bean.getMsgHeader().getMsgId());
        switch (msgId) {
            //平台通用应答
            case 0x8001:
                universalResult(bean);
                break;
            case 0x9212:
                uploadFileDone(bean);
                break;
            default:
                break;
        }
    }


    /**
     * 平台通用应答
     *
     * @param bean
     */
    private void universalResult(JTT808Bean bean) {
        ByteBuf msgBody = bean.getMsgBody();
        byte[] flowNum = msgBody.readBytes(2).array();
        byte[] msgId = msgBody.readBytes(2).array();
        byte result = msgBody.readByte();
        String reply = HexUtil.byte2HexStrNoSpace(msgId);
        Log.d(TAG, "平台通用应答 回复的是消息ID=" + reply + " 结果：" + result + "（0:成功/确认;1:失败;2:消息有误;3:不支持;4:报警 处理确认）");

        //继续发送报警附件信息
        if (reply.equals("1210")) {
            jtt1078Client.fileMsg();
        } else if (reply.equals("1211")) {
            jtt1078Client.uploadFile();
        }
    }

    /**
     * 文件上传成功
     * 如果有多个文件则需要继续上传在一个文件
     */
    private void uploadFileDone(JTT808Bean bean) {
        ByteBuf body = bean.getMsgBody();
        //文件名称长度
        byte fileNameLength = body.readByte();
        byte[] nameBytes = body.readBytes(fileNameLength).array();
        String name = new String(nameBytes);
        byte fileType = body.readByte();
        byte uploadResult = body.readByte();
        //补传数据包数量
        byte pkgCount = body.readByte();
        jtt1078Client.fileMsg();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Log.d(TAG, "附件服务器连接成功");
        jtt1078Client.sendBjfjMsg();

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.d(TAG, "附件服务器连接断开");

    }

}
