package com.azhon.jtt808.netty;

import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.util.ByteUtil;
import com.azhon.jtt808.util.HexUtil;
import com.azhon.jtt808.util.JTT808Util;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.netty
 * 文件名:    JTT808Decoder
 * 创建时间:  2020/1/4 on 19:43
 * 描述:     TODO
 *
 * @author 阿钟
 */

public class JTT808Decoder extends ByteToMessageDecoder {

    private static final String TAG = "JTT808Decoder";

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        JTT808Bean jtt808Bean = resolve(byteBuf);
        list.add(jtt808Bean);
    }

    /**
     * 解析数据
     *
     * @param byteBuf
     */
    private JTT808Bean resolve(ByteBuf byteBuf) {
        int length = byteBuf.readableBytes();
        //添加了拆包handler，所以这里拿到的数据都是没有7E的数据包
        byte[] no7EData = new byte[length];
        byteBuf.readBytes(no7EData);
        byteBuf.clear();
//========================================================================
        //转义 7D 02->7E  7D 01->7D
        String hexStr = HexUtil.byte2HexStr(no7EData);
        String replaceHexStr = hexStr.replaceAll(" 7D 02", " 7E")
                .replaceAll(" 7D 01", " 7D")
                // 最后去除空格
                .replaceAll(" ", "");
        byte[] data = HexUtil.hexStringToByte(replaceHexStr);
        //转义后的数据
        byteBuf.writeBytes(data);
//========================================================================
        JTT808Bean jtt808Bean = new JTT808Bean();
        //解析消息头
        JTT808Bean.MsgHeader msgHeader = new JTT808Bean.MsgHeader();
        byte[] msgId = byteBuf.readBytes(2).array();
        byte[] msgAttributes = byteBuf.readBytes(2).array();
        byte[] terminalPhone = byteBuf.readBytes(6).array();
        byte[] flowNum = byteBuf.readBytes(2).array();

        msgHeader.setMsgId(msgId);
        msgHeader.setMsgAttributes(msgAttributes);
        msgHeader.setTerminalPhone(terminalPhone);
        msgHeader.setFlowNum(flowNum);

        //消息体长度
        int[] msgBodyAttr = resolveMsgBodyLength(msgAttributes);
        if (msgBodyAttr[0] == JTT808Util.SUB_PACKAGE_YES) {
            //TODO 分包
            byteBuf.readBytes(4);
        }
        //消息体
        ByteBuf msgBody = byteBuf.readBytes(msgBodyAttr[1]);
        //校验码
        byte checkCode = byteBuf.readByte();

        jtt808Bean.setMsgHeader(msgHeader);
        jtt808Bean.setMsgBody(msgBody);
        jtt808Bean.setCheckCode(checkCode);

        return jtt808Bean;
    }

    /**
     * 解析消息体属性
     *
     * @return
     */
    private int[] resolveMsgBodyLength(byte[] msgAttributes) {
        ByteBuf msgAttr = Unpooled.buffer(16);
        for (byte attribute : msgAttributes) {
            msgAttr.writeBytes(ByteUtil.byteToBit(attribute));
        }
        //保留位
        msgAttr.readBytes(2);
        //是否分包
        byte subpackage = msgAttr.readByte();
        //加密方式
        byte[] encrypt = msgAttr.readBytes(3).array();
        //消息体长度
        byte[] bodyLength = msgAttr.readBytes(10).array();
        String bits = "";
        for (byte b : bodyLength) {
            bits += b;
        }
        int msgBodyLength = Integer.parseInt(bits, 2);
        return new int[]{subpackage, msgBodyLength};
    }
}
