package com.azhon.jtt808.util;

import java.util.Arrays;

/**
 * 文件名:    ByteUtil
 * 创建时间:  2018/10/24 on 21:50
 * 描述:     TODO
 *
 * @author 阿钟
 */

public class ByteUtil {
    /**
     * 多个数组合并一个
     *
     * @return
     */
    public static byte[] byteMergerAll(byte[]... bytes) {
        int allLength = 0;
        for (byte[] b : bytes) {
            allLength += b.length;
        }
        byte[] allByte = new byte[allLength];
        int countLength = 0;
        for (byte[] b : bytes) {
            System.arraycopy(b, 0, allByte, countLength, b.length);
            countLength += b.length;
        }
        return allByte;
    }

    /**
     * 将数据数据恢复成0
     */
    public static void resetBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 0;
        }
    }

    /**
     * 计算校验和
     *
     * @param bytes
     * @param index 校验和结果所在的下标
     * @return 是否成功
     */
    public static boolean checkSum(byte[] bytes, int index) {
        if (index > bytes.length - 1) {
            return false;
        }
        byte right = bytes[index];
        int plus = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (index != i) {
                plus += bytes[i];
            }
        }
        return int2Byte(plus) == right;
    }

    /**
     * 计算CRC16 MOD BUS校验码
     *
     * @param bytes 源数据最后两位为校验码
     *              完整数据：32 00 00 4B 7B 4A 00 30 01 BD 78 5D 78 5D
     *              eg:32 00 00 4B 7B 4A 00 30 01 BD 78 5D 校验码 78 5D
     * @return
     */
    public static boolean checkCRC16(byte[] bytes) {
        byte[] data = Arrays.copyOfRange(bytes, 0, bytes.length - 2);
        byte[] code = Arrays.copyOfRange(bytes, bytes.length - 2, bytes.length);
        String checkCode = HexUtil.byte2HexStrNoSpace(code);
        String result = getCRC16(data);
        return checkCode.equalsIgnoreCase(result);
    }

    /**
     * 计算CRC16 MOD BUS校验码
     */
    public static String getCRC16(byte[] data) {
        int crc = 0x0000FFFF;
        int polynomial = 0X0000A001;
        for (byte b : data) {
            crc ^= ((int) b & 0x000000FF);
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x00000001) != 0) {
                    crc >>= 1;
                    crc ^= polynomial;
                } else {
                    crc >>= 1;
                }
            }
        }
        //高低位转换，(不转换 高位在左低位在右)
        crc = ((crc & 0x0000FF00) >> 8) | ((crc & 0x000000FF) << 8);
        return Integer.toHexString(crc).toUpperCase();
    }

    /**
     * int 转byte字节
     *
     * @param value int数字
     * @return byte 字节
     */
    public static byte int2Byte(int value) {
        return (byte) value;
    }

    /**
     * byte 转int字节
     *
     * @param value int数字
     * @return byte 字节
     */
    public static int byte2Int(byte value) {
        return value & 0xFF;
    }

    /**
     * 两个十六进制字节转成一个int
     * WORD
     *
     * @return int
     */
    public static int bytes2Int(byte[] bytes) {
        int a = ((bytes[0] & 0xf0) >> 4) * 4096;
        int b = (bytes[0] & 0x0f) * 256;
        int c = bytes[1] & 0xf0;
        int d = bytes[1] & 0x0f;
        return a + b + c + d;
    }

    /**
     * 4字节byte转int
     *
     * @return
     */
    public static int fourBytes2Int(byte[] bytes) {
        int mask = 0xff;
        int temp;
        int n = 0;
        for (byte b : bytes) {
            n <<= 8;
            temp = b & mask;
            n |= temp;
        }
        return n;
    }

    /**
     * byte字节转Bit
     * bit位（0～8位）是从右往左数的 eg:10000011 (位0：1，位2：1，位3：0)
     *
     * @param b 字节
     * @return bit
     */
    public static byte[] byteToBit(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte) (b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }

    /**
     * long转DWORD数据类型
     * 低位到高位
     */
    public static byte[] longToDword(long value) {
        byte[] data = new byte[4];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (value >> (8 * (3 - i)));
        }
        return data;
    }

    /**
     * 去除数组中的0
     */
    public static byte[] delete0InArray(byte[] bytes) {
        int count = 0;
        for (byte b : bytes) {
            if (b != 0) count++;
            else
                break;
        }
        byte[] data = new byte[count];
        for (int i = 0; i < count; i++) {
            data[i] = bytes[i];
        }
        return data;
    }


    /**
     * DWORD无符号整型数据转换为java的long型
     */
    public static long DwordToLong(byte buf[], int index) {
        int firstByte = (0x000000FF & ((int) buf[index]));
        int secondByte = (0x000000FF & ((int) buf[index + 1]));
        int thirdByte = (0x000000FF & ((int) buf[index + 2]));
        int fourthByte = (0x000000FF & ((int) buf[index + 3]));
        return ((long) (firstByte | secondByte << 8 | thirdByte << 16 |
                fourthByte << 24)) & 0xFFFFFFFFL;
    }

    /**
     * 一个int转4个字节的byte数组
     *
     * @param value
     * @return
     */
    public static byte[] int2Bytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * 字符串==>BCD字节数组
     * 根据安装后终端自身的手机号转换。手机号不足 12 位，
     * 则在前补充数字，大陆手机号补充数字 0，港澳 台则根据其区号进行位数补充。
     *
     * @param str 转BCD[6]个字节
     * @return BCD字节数组
     */
    public static byte[] string2Bcd(String str) {
        // 奇数,前补零
        if ((str.length() & 0x1) == 1) {
            str = "0" + str;
        }

        byte ret[] = new byte[str.length() / 2];
        byte bs[] = str.getBytes();
        for (int i = 0; i < ret.length; i++) {

            byte high = ascII2Bcd(bs[2 * i]);
            byte low = ascII2Bcd(bs[2 * i + 1]);

            // TODO 只遮罩BCD低四位?
            ret[i] = (byte) ((high << 4) | low);
        }
        return ret;
    }

    /**
     * 将BCD码转成String
     *
     * @param b
     * @return
     */
    public static String bcdToString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte value : b) {
            int h = ((value & 0xff) >> 4) + 48;
            sb.append((char) h);
            int l = (value & 0x0f) + 48;
            sb.append((char) l);
        }
        return sb.toString();
    }

    private static byte ascII2Bcd(byte asc) {
        if ((asc >= '0') && (asc <= '9'))
            return (byte) (asc - '0');
        else if ((asc >= 'A') && (asc <= 'F'))
            return (byte) (asc - 'A' + 10);
        else if ((asc >= 'a') && (asc <= 'f'))
            return (byte) (asc - 'a' + 10);
        else
            return (byte) (asc - 48);
    }

    /**
     * long转8个字节
     *
     * @param res
     * @return
     */
    public static byte[] long2Bytes(long res) {
        byte[] buffer = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = 64 - (i + 1) * 8;
            buffer[i] = (byte) ((res >> offset) & 0xff);
        }
        return buffer;
    }

    /**
     * int 转WORD（无符号双字节整形）
     */
    public static byte[] int2Word(long num) {
        byte[] result = new byte[2];
        for (int i = 0; i < 2; i++) {
            result[i] = (byte) ((num >> (2 - i - 1) * 8) & 0xFF);
        }
        return result;
    }
}
