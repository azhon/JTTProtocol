package com.azhon.jtt808.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.util
 * 文件名:    TimeUtils
 * 创建时间:  2018/6/28 on 9:29
 * 描述:     TODO
 *
 * @author 阿钟
 */

public class TimeUtil {
    /**
     * 格式化当前时间
     *
     * @return
     */
    public static String yyyyMMddHHmmss() {
        Date date = new Date(System.currentTimeMillis());
        return new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(date);
    }

    /**
     * 格式化时间
     *
     * @return
     */
    public static String yyyyMMdd() {
        Date date = new Date(System.currentTimeMillis());
        return new SimpleDateFormat("yyyy_MM_dd").format(date);
    }

    /**
     * @return
     */
    public static String HHmm() {
        Date date = new Date(System.currentTimeMillis());
        return new SimpleDateFormat("HH:mm").format(date);
    }

    public static Date byPattern(String time, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            return format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 格式化时间
     * yyMMddHHmmss 格式在转成bcd[6]
     */
    public static byte[] getBcdTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
        String time = format.format(new Date());
        return ByteUtil.string2Bcd(time);
    }
}
