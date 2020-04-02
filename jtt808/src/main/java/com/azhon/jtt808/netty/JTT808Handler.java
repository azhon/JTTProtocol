package com.azhon.jtt808.netty;

import android.util.Log;

import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.bean.TerminalParamsBean;
import com.azhon.jtt808.listener.OnConnectionListener;
import com.azhon.jtt808.netty.jtt1078.JTT1078Client;
import com.azhon.jtt808.util.ByteUtil;
import com.azhon.jtt808.util.HexUtil;
import com.azhon.jtt808.util.JTT808Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.netty
 * 文件名:    JTT808Handler
 * 创建时间:  2020/1/2 on 22:49
 * 描述:     TODO
 *
 * @author 阿钟
 */

public class JTT808Handler extends SimpleChannelInboundHandler<JTT808Bean> {
    private static final String TAG = "JTT808Handler";
    private JTT808Client jtt808Client;
    private OnConnectionListener listener;

    public JTT808Handler(JTT808Client jtt808Client, OnConnectionListener listener) {
        this.jtt808Client = jtt808Client;
        this.listener = listener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, JTT808Bean bean) throws Exception {
        if (listener != null) {
            listener.receiveData(bean);
        }
        handData(bean, ctx.channel());
    }

    /**
     * 处理事件
     *
     * @param bean
     */
    private void handData(JTT808Bean bean, Channel channel) throws Exception {
        int msgId = ByteUtil.bytes2Int(bean.getMsgHeader().getMsgId());
        switch (msgId) {
            //注册应答
            case 0x8100:
                registerResult(bean);
                break;
            //平台通用应答
            case 0x8001:
                universalResult(bean);
                break;
            //渝标，上传附件指令
            case 0x9208:
                uploadFiles(bean, channel);
                break;
            //jtt808，设置终端参数
            case 0x8103:
                terminalParams(bean);
                break;
            default:
                Log.d(TAG, "收到的消息ID：" + msgId);
                break;
        }
    }


    /**
     * 注册结果
     *
     * @param bean
     */
    private void registerResult(JTT808Bean bean) {
        ByteBuf msgBody = bean.getMsgBody();
        int length = msgBody.readableBytes();
        byte[] flowNum = msgBody.readBytes(2).array();
        byte result = msgBody.readByte();
        Log.d(TAG, "注册结果：" + result + "（0:成功;1:车辆已被注册;2:数据库中无该车辆; 3:终端已被注册;4:数据库中无该终端）");
        if (result != 0) return;
        byte[] authCode = new byte[length - 3];
        msgBody.readBytes(authCode);
        JTT808Bean authBean = JTT808Util.auth(authCode);
        Log.d(TAG, "发送鉴权: " + authBean.toString());
        JTT808Client.getInstance().writeAndFlush(authBean);
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

    }

    /**
     * 渝标上传附件
     *
     * @param bean
     */
    private void uploadFiles(JTT808Bean bean, Channel channel) {
        //响应平台
        JTT808Bean.MsgHeader msgHeader = bean.getMsgHeader();
        JTT808Bean authBean = JTT808Util.universalResponse(msgHeader.getFlowNum(), msgHeader.getMsgId());
        JTT808Client.getInstance().writeAndFlush(authBean);

        ByteBuf body = bean.getMsgBody();
        //ip地址
        byte ipLength = body.readByte();
        byte[] ipBytes = new byte[ipLength];
        body.readBytes(ipBytes);
        String ip = new String(ipBytes);
        //tcp端口
        byte[] portBytes = body.readBytes(2).array();
        int port = ByteUtil.bytes2Int(portBytes);
        //udp端口用不到，先忽略
        body.readBytes(new byte[2]);
        //报警标识号
        byte[] alarmIDNumber = body.readBytes(16).array();
        //报警编号
        byte[] alarmNumber = body.readBytes(32).array();
        //预留
        byte[] yl = body.readBytes(16).array();
        Log.d(TAG, "收到了附件服务器的信息：ip=" + ip + " port=" + port);
        //连接附件服务器
        JTT1078Client jtt1078Client = new JTT1078Client(ip, port);
        //获取需要上传的附件列表
        String key = HexUtil.byte2HexStrNoSpace(alarmIDNumber);
        List<File> files = JTT808Util.ALARM_MAP.get(key);
        if (files == null) {
            Log.e(TAG, "获取报警附件失败：" + key);
            return;
        }
        JTT808Util.ALARM_MAP.remove(key);
        jtt1078Client.setFiles(files);
        jtt1078Client.setAlarmIDNumber(alarmIDNumber);
        jtt1078Client.setAlarmNumber(alarmNumber);
        jtt1078Client.connect();
    }


    /**
     * jtt808，设置终端参数
     *
     * @param bean
     */
    private void terminalParams(JTT808Bean bean) throws Exception {
        List<TerminalParamsBean> params = new ArrayList<>();
        ByteBuf body = bean.getMsgBody();
        //参数总数
        byte paramsCount = body.readByte();
        for (byte i = 0; i < paramsCount; i++) {
            //DWORD 读取4个字节
            int id = body.readInt();
            byte paramsLength = body.readByte();
            byte[] data = body.readBytes(paramsLength).array();
            switch (id) {
                //DWORD
                case 0x0055:
                case 0x0056:
                case 0x0057:
                case 0x0058:
                case 0x0059:
                case 0x005A:
                case 0x0018:
                case 0x0019:
                case 0x0020:
                case 0x0021:
                case 0x0022:
                case 0x0027:
                case 0x0028:
                case 0x0029:
                case 0x002C:
                case 0x002D:
                case 0x002E:
                case 0x002F:
                case 0x0030:
                    params.add(new TerminalParamsBean(id, Integer.class, ByteUtil.fourBytes2Int(data)));
                    break;
                //WORD
                case 0x005B:
                case 0x005C:
                case 0x0081:
                case 0x0082:
                    params.add(new TerminalParamsBean(id, Integer.class, ByteUtil.bytes2Int(data)));
                    break;
                //STRING
                case 0x0010:
                case 0x0011:
                case 0x0012:
                case 0x0013:
                case 0x0014:
                case 0x0015:
                case 0x0016:
                case 0x0017:
                case 0x0083:
                    params.add(new TerminalParamsBean(id, String.class, new String(data, "GBK")));
                    break;
                //BYTE
                case 0x0084:
                    params.add(new TerminalParamsBean(id, Byte.class, data[0]));
                    break;

                default:
                    break;
            }
        }
        if (listener != null) {
            listener.terminalParams(params);
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        if (listener != null) {
            listener.onConnectionSateChange(OnConnectionListener.CONNECTED);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.e(TAG, "断开了连接");
        jtt808Client.reConnect();
        if (listener != null) {
            listener.onConnectionSateChange(OnConnectionListener.DIS_CONNECT);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE) {
                JTT808Bean heartBeatBean = JTT808Util.heartBeat();
                Log.d(TAG, "发送心跳: " + heartBeatBean.toString());
                ctx.writeAndFlush(heartBeatBean.getData());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
