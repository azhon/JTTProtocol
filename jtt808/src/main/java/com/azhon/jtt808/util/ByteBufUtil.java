package com.azhon.jtt808.util;

import io.netty.buffer.ByteBuf;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.util
 * 文件名:    ByteBufUtil
 * 创建时间:  2020/1/4 on 18:52
 * 描述:     TODO
 *
 * @author 阿钟
 */

public class ByteBufUtil {

    /**
     * byteBuf转数组
     *
     * @param byteBuf
     * @return
     */
    public static byte[] toArray(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        byteBuf.resetReaderIndex();
        return bytes;
    }
}
