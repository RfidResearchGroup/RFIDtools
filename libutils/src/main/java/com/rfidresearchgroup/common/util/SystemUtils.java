package com.rfidresearchgroup.common.util;

/**
 * @author DXL
 */
public class SystemUtils {

    /**
     * 判断当前是否超时
     *
     * @param startTimeStamp 开始时间（时间戳形式）
     * @param timeoutms      超时值，当最新的时间超过了这个值时将会被判断为超时!
     * @return true 超时，
     */
    public static boolean isTimeout(long startTimeStamp, long timeoutms) {
        return System.currentTimeMillis() - startTimeStamp >= timeoutms;
    }

    // 简化休眠
    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculates the checksum of the passed byte buffer.
     *
     * @param buffer
     * @return byte checksum value
     */
    public static byte calcChecksum(byte[] buffer, boolean sub) {
        if (buffer == null) return 0;
        byte checksum = 0;
        int bufPos = 0;
        int byteCount = buffer.length;
        while (byteCount-- != 0) {
            byte b = buffer[bufPos++];
            byte tmp = checksum;
            if (!sub)
                checksum += b;
            else
                checksum -= b;
            System.out.println("value: " + HexUtil.toHexString(b) + "," + HexUtil.toHexString(tmp) + " checksum: " + HexUtil.toHexString(checksum));
        }
        return checksum;
    }

    /**
     * Calculates the checksum of the passed byte buffer.
     *
     * @param buffer
     * @return byte checksum value
     */
    public static byte calcChecksub(byte checkSum, byte[] buffer, boolean sub) {
        if (buffer == null) return 0;
        byte checksum = checkSum;
        int bufPos = 0;
        int byteCount = buffer.length;
        while (byteCount-- != 0) {
            byte b = buffer[bufPos++];
            byte tmp = checksum;
            if (!sub)
                checksum += b;
            else
                checksum -= b;
            System.out.println("value: " + HexUtil.toHexString(tmp) + "," + HexUtil.toHexString(b) + " checksum: " + HexUtil.toHexString(checksum));
        }
        return checksum;
    }
}
