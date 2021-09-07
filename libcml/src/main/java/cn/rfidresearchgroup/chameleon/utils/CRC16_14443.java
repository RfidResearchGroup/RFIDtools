package cn.rfidresearchgroup.chameleon.utils;

public class CRC16_14443 {

    //private static String LOG = CRC16_14443.class.getSimpleName();

    public static final int CRC16_14443_A = 0x6363;
    public static final int CRC16_14443_B = 0xFFFF;

    public static class Out {
        byte first;
        byte second;
    }

    private static int updateCrc14443(byte b, int crc) {
        byte ch = (byte) (b ^ (byte) (crc & 0x00ff));
        ch = (byte) (ch ^ (ch << 4));
        return ((crc >> 8) ^ (ch << 8) ^ (ch << 3) ^ (ch >> 4));
    }

    public static void computeCrc14443(int crc_type, byte[] bytes, int len, Out out) {
        out.first = 0;
        out.second = 0;
        if (len < 2) return;
        byte b;
        int res = crc_type;
        for (int i = 0; i < len; i++) {
            b = bytes[i];
            res = updateCrc14443(b, res);
        }
        /* ISO/IEC 13239 (formerly ISO/IEC 3309) */
        if (crc_type == CRC16_14443_B) res = ~res;
        out.first = (byte) (res & 0xFF);
        out.second = (byte) ((res >> 8) & 0xFF);
    }

    public static boolean checkCrc14443(int crc_type, byte[] bytes, int len) {
        if (len < 3) return false;
        Out out = new Out();
        computeCrc14443(crc_type, bytes, len - 2, out);
        byte crc1 = bytes[len - 2];
        byte crc2 = bytes[len - 1];
        /*Log.d(LOG, "计算的值1: " + HexUtil.toHexString(out.first));
        Log.d(LOG, "计算的值2: " + HexUtil.toHexString(out.second));
        Log.d(LOG, "传入的值1: " + HexUtil.toHexString(crc1));
        Log.d(LOG, "传入的值2: " + HexUtil.toHexString(crc2));*/
        return out.first == crc1 && out.second == crc2;
    }
}