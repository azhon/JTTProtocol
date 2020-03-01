package com.azhon.jtt808.util;

import java.io.File;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.util
 * 文件名:    Util
 * 创建时间:  2020/2/6 on 11:56
 * 描述:     TODO
 *
 * @author 阿钟
 */

public class Util {
    /**
     * 获取文件类型
     *
     * @return eg: .png
     */
    public static String getFileType(File file) {
        String name = file.getName();
        return name.substring(name.lastIndexOf("."));
    }

    /**
     * 获取文件类型
     * 00——图片;01——音频;02——音视频;03——文本;04——其它。
     * 图片文件为 jpg 或 png，音频文件为 wav，视频文件为 h264 或 mp4，文本 文件为 bin;
     */
    public static String getFileNameType(String type) {
        switch (type) {
            case ".png":
            case ".jpg":
                return "00";
            case ".wav":
                return "01";
            case ".h264":
            case ".mp4":
                return "02";
            case ".bin":
                return "03";
            default:
                return "04";
        }
    }
}
