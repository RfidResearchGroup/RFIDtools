package cn.dxl.common.util;

/**
 * crc16多项式算法
 *
 * @author eguid
 */
public class CRC16 {

    /**
     * CRC16-XMODEM算法（四字节）
     *
     * @param bytes
     * @return
     */
    public static int crc16_ccitt_xmodem(byte[] bytes) {
        return crc16_ccitt_xmodem(bytes, 0, bytes.length);
    }

    /**
     * CRC16-XMODEM算法（四字节）
     *
     * @param bytes
     * @param offset
     * @param count
     * @return
     */
    public static int crc16_ccitt_xmodem(byte[] bytes, int offset, int count) {
        int crc = 0x0000; // initial value
        int polynomial = 0x1021; // poly value
        for (int index = offset; index < count; index++) {
            byte b = bytes[index];
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= polynomial;
            }
        }
        crc &= 0xffff;
        return crc;
    }

    /**
     * CRC16-XMODEM算法（两字节）
     *
     * @param bytes
     * @param offset
     * @param count
     * @return
     */
    public static short crc16_ccitt_xmodem_short(byte[] bytes, int offset, int count) {
        return (short) crc16_ccitt_xmodem(bytes, offset, count);
    }

    /**
     * CRC16-XMODEM算法（两字节）
     *
     * @param bytes
     */
    public static short crc16_ccitt_xmodem_short(byte[] bytes) {
        return crc16_ccitt_xmodem_short(bytes, 0, bytes.length);
    }

}
