package com.azhon.jtt808.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.util
 * 文件名:    FileUtil
 * 创建时间:  2019/1/9 on 10:49
 * 描述:     TODO
 *
 * @author 阿钟
 */

public class FileUtil {

    /**
     * 图片转byte[]
     */
    public static byte[] file2Bytes(String path) {
        try {
            FileInputStream inputStream = new FileInputStream(path);
            byte[] buffer = new byte[1024];
            int len;
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            while ((len = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            byte[] data = outStream.toByteArray();
            outStream.close();
            inputStream.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
