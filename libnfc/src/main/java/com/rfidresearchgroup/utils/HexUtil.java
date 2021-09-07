package com.rfidresearchgroup.utils;

import android.util.Log;

import java.nio.charset.Charset;
import java.util.Arrays;

public class HexUtil {
    private final static char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static String dumpHexString(byte[] array) {
        return dumpHexString(array, 0, array.length);
    }

    public static String dumpHexString(byte[] array, int offset, int length) {
        StringBuilder result = new StringBuilder();

        byte[] line = new byte[16];
        int lineIndex = 0;

        result.append("\n0x");
        result.append(toHexString(offset));

        for (int i = offset; i < offset + length; i++) {
            if (lineIndex == 16) {
                result.append(" ");

                for (int j = 0; j < 16; j++) {
                    if (line[j] > ' ' && line[j] < '~') {
                        result.append(new String(line, j, 1));
                    } else {
                        result.append(".");
                    }
                }

                result.append("\n0x");
                result.append(toHexString(i));
                lineIndex = 0;
            }

            byte b = array[i];
            result.append(" ");
            result.append(HEX_DIGITS[(b >>> 4) & 0x0F]);
            result.append(HEX_DIGITS[b & 0x0F]);

            line[lineIndex++] = b;
        }

        if (lineIndex != 16) {
            int count = (16 - lineIndex) * 3;
            count++;
            for (int i = 0; i < count; i++) {
                result.append(" ");
            }

            for (int i = 0; i < lineIndex; i++) {
                if (line[i] > ' ' && line[i] < '~') {
                    result.append(new String(line, i, 1));
                } else {
                    result.append(".");
                }
            }
        }

        return result.toString();
    }

    public static String toHexString(byte b) {
        return toHexString(toByteArray(b));
    }

    public static String toHexString(byte[] array) {
        if (array == null) return null;
        return toHexString(array, 0, array.length);
    }

    public static String toHexString(Byte[] array) {
        if (array == null) return null;
        byte[] bytes = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            bytes[i] = array[i];
        }
        return toHexString(bytes, 0, array.length);
    }

    public static String toHexString(byte[] array, int offset, int length) {
        char[] buf = new char[length * 2];

        int bufIndex = 0;
        for (int i = offset; i < offset + length; i++) {
            byte b = array[i];
            buf[bufIndex++] = HEX_DIGITS[(b >>> 4) & 0x0F];
            buf[bufIndex++] = HEX_DIGITS[b & 0x0F];
        }

        return new String(buf);
    }

    public static String toHexString(int i) {
        return toHexString(toByteArray(i));
    }

    public static String toHexString(short i) {
        return toHexString(toByteArray(i));
    }

    public static String decorateHex(String hexStr) {
        StringBuilder sb = new StringBuilder();
        char[] charArr = hexStr.toCharArray();
        for (int i = 0; i < charArr.length; i += 2) {
            sb.append("0x").append(charArr[i])
                    .append(charArr[i + 1]).append(" ");
        }
        return sb.toString();
    }

    public static byte[] toByteArray(byte b) {
        byte[] array = new byte[1];
        array[0] = b;
        return array;
    }

    public static byte[] toByteArray(int i) {
        byte[] array = new byte[4];

        array[3] = (byte) (i & 0xFF);
        array[2] = (byte) ((i >> 8) & 0xFF);
        array[1] = (byte) ((i >> 16) & 0xFF);
        array[0] = (byte) ((i >> 24) & 0xFF);

        return array;
    }

    public static byte[] toByteArray(short i) {
        byte[] array = new byte[2];

        array[1] = (byte) (i & 0xFF);
        array[0] = (byte) ((i >> 8) & 0xFF);

        return array;
    }

    private static int toByte(char c) {
        if (c >= '0' && c <= '9')
            return (c - '0');
        if (c >= 'A' && c <= 'F')
            return (c - 'A' + 10);
        if (c >= 'a' && c <= 'f')
            return (c - 'a' + 10);
        Log.d("HexUtil", "The byte char is invalid: " + c);
        //throw new RuntimeException("Invalid hex char '" + c + "'");
        return 0;
    }

    public static int toInt(byte b) {
        return b & 0xFF;
    }

    public static int toIntFrom2Byte(byte[] b) {
        return Integer.parseInt(toHexString(b), 16);
    }

    /**
     * 拆分byte数组
     *
     * @param bytes 要拆分的数组
     * @param size  要按几个组成一份
     * @return
     */
    public static byte[][] splitBytes(byte[] bytes, int size) {
        double splitLength = Double.parseDouble(size + "");
        int arrayLength = (int) Math.ceil(bytes.length / splitLength);
        byte[][] result = new byte[arrayLength][];
        int from, to;
        for (int i = 0; i < arrayLength; i++) {
            from = (int) (i * splitLength);
            to = (int) (from + splitLength);
            if (to > bytes.length)
                to = bytes.length;
            result[i] = Arrays.copyOfRange(bytes, from, to);
        }
        return result;
    }

    public static byte[] bytesMerge(byte[]... arrs) {
        byte[] ret = new byte[byteArraysLength(arrs)];
        int pos = 0;
        for (byte[] tmp : arrs) {
            if (tmp != null && tmp.length > 0) {
                System.arraycopy(tmp, 0, ret, pos, tmp.length);
                pos += tmp.length;
            }
        }
        return ret;
    }

    public static int byteArraysLength(byte[]... arrs) {
        int ret = 0;
        for (byte[] tmp : arrs) {
            if (tmp != null)
                ret += tmp.length;
        }
        return ret;
    }

    public static int byteArrayToInt(byte[] b) {
        return (b[3] & 0xFF) |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static int byte2Int(byte[] data, int offset) {
        byte[] bs = new byte[4];
        for (int i = offset, j = 0; j < bs.length; i++, j++) {
            bs[j] = data[i];
        }
        return HexUtil.byteArrayToInt(bs);
    }

    public static int hexString2Int(String hexString) {
        byte[] hexByte = hexStringToByteArray(hexString);
        return byteArrayToInt(hexByte);
    }

    public static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    public static byte[] hexStringToByteArray(String hexString) {
        if (hexString == null) return null;
        if (hexString.length() == 0) return null;
        int length = hexString.length();
        byte[] buffer = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            buffer[i / 2] = (byte) ((toByte(hexString.charAt(i)) << 4) | toByte(hexString
                    .charAt(i + 1)));
        }
        return buffer;
    }

    public static byte[] getAsciiBytes(String str) {
        return str.getBytes(Charset.forName("ascii"));
    }

    //判断是否是十六进制格式的字符串
    public static boolean isHexString(String str) {
        if (str == null) return false;
        if (str.matches("[0-9a-fA-F]+")) return true;
        if (str.matches("0x[0-9a-fA-F]+")) return true;
        return str.matches("0x[0-9a-fA-F] +");
    }
}
