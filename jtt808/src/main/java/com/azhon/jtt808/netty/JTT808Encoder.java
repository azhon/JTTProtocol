package com.azhon.jtt808.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.netty
 * 文件名:    JTT808Encoder
 * 创建时间:  2020/1/4 on 19:40
 * 描述:     TODO
 *
 * @author 阿钟
 */

public class JTT808Encoder extends MessageToByteEncoder<byte[]> {

    private static final String TAG = "JTT808Encoder";

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, byte[] data, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(data);
    }
}
