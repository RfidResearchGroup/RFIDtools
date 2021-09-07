package cn.rfidresearchgroup.chameleon.utils;

public class ChameleonUtils {

    public static String stringJoin(String delimiter, String[] strArray) {
        if (strArray == null)
            return null;
        else if (strArray.length == 0)
            return "";
        StringBuilder retStr = new StringBuilder(strArray[0]);
        for (int s = 1; s < strArray.length; s++) {
            retStr.append(delimiter).append(strArray[s]);
        }
        return retStr.toString();
    }

    /**
     * 原加密源码!
     * void ComPass(char *toBeEncFileName, int key, int len)
     * {
     * char newFileName[275] = { 0 };
     * memcpy(newFileName, toBeEncFileName, len);
     * int i, s, t, size = len;
     * for (i = 0; i < size; i++)
     * {
     * s = newFileName[i];
     * t = (size + key + i - size / key) ^ s;
     * toBeEncFileName[i] = t;
     * }
     * }
     *
     * @param datas 数据源，被加密过的
     * @param key   加密用的密钥,一个整形变量!
     * @param size  数据长度，数据长度本身也被用来计算了!
     */
    public static void decryptData(byte[] datas, int key, int size) {
        byte[] tmp = new byte[275];
        System.arraycopy(datas, 0, tmp, 0, size);
        int i, s, t;
        for (i = 0; i < size; i++) {
            s = tmp[i];
            t = (size + key + i - size / key) ^ s;
            datas[i] = (byte) t;
        }
    }
}
