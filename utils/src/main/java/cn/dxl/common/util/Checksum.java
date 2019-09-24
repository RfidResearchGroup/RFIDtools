package cn.dxl.common.util;

public class Checksum {
    /**
     * Calculates the checksum of the passed byte buffer.
     *
     * @param buffer
     * @param byteCount
     * @return byte checksum value
     */
    public static byte calcChecksum(byte[] buffer, int byteCount) {
        byte checksum = 0;
        int bufPos = 0;
        while (byteCount-- != 0) {
            checksum += buffer[bufPos++];
        }
        return checksum;
    }

}
