package cn.dxl.common.util;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author DXL
 * @Date 2019/4/12
 */
public class DiskKVUtil {

    private static String LOG_TAG = "DiskKVUtil";

    /**
     * 封装由设置写到持久化层的实现
     *
     * @param key   键
     * @param value 值
     * @param file  最终写入的文件
     */
    public static void update2Disk(String key, String value, File file) throws IOException {
        //由键值对到底层持久化的实现
        byte[] dataBuf = FileUtils.readBytes(file);
        String[] dats = new String(dataBuf).split("\n");
        //迭代判断行!
        for (int i = 0; i < dats.length; i++) {
            String kvLine = dats[i];
            //如果是注释则跳过处理
            if (kvLine.startsWith("#")) continue;
            //否则判断键值对!
            if (kvLine.matches(".* : .*")) {
                if (kvLine.startsWith(key)) {
                    //判断键是否对应行开头，更新具体内容!
                    String newKVLine = warp2KvLine(key, value);
                    dats[i] = newKVLine;
                }
            }
        }
        String result = StringUtil.arr2Str(dats, "\n", true);
        //重新写入到磁盘!
        FileUtils.writeBytes(result.getBytes(), file, false);
    }

    /**
     * 封装由设置写到持久化层的实现
     *
     * @param key   键
     * @param value 值，数组形式，表示一键多值!
     * @param file  最终写入的文件
     */
    public static void update2Disk(String key, String[] value, File file) throws IOException {
        //由键值对到底层持久化的实现
        byte[] dataBuf = FileUtils.readBytes(file);
        String[] dats = new String(dataBuf).split("\n");
        //迭代判断行!
        for (int i = 0, j = 0; i < dats.length; i++) {
            String kvLine = dats[i];
            //如果是注释则跳过处理
            if (kvLine.startsWith("#")) continue;
            //否则判断键值对!
            if (kvLine.matches(".* : .*")) {
                if (kvLine.startsWith(key)) {
                    if (j >= value.length) {
                        //可能会越界!
                        Log.d(LOG_TAG, "update2Disk 有越界风险!");
                    } else {
                        //判断键是否对应行开头，更新具体内容!
                        String newKVLine = warp2KvLine(key, value[j]);
                        j++;
                        dats[i] = newKVLine;
                    }
                }
            }
        }
        String result = StringUtil.arr2Str(dats, "\n", true);
        //重新写入到磁盘!
        FileUtils.writeBytes(result.getBytes(), file, false);
    }

    /**
     * 查询键值对!
     *
     * @param key  键
     * @param file 欲操作的文件
     * @return 查询的结果集(值)!
     * @throws IOException 操作出现的各种问题!
     */
    public static String[] queryKVLine(String key, File file) throws IOException {
        byte[] dataBuf = FileUtils.readBytes(file);
        String[] dats = new String(dataBuf).split("\n");
        ArrayList<String> valueList = new ArrayList<>(16);
        for (String line : dats) {
            //如果是注释则跳过处理
            if (line.startsWith("#")) continue;
            if (line.startsWith(key)) {
                valueList.add(getValue(line));
            }
        }
        return valueList.toArray(new String[0]);
    }

    /**
     * 查询键是否存在!
     *
     * @param key  键
     * @param file 查询的文件!
     * @return 对应的键是否存在于配置文件中!
     */
    public static boolean isKVExists(String key, File file) throws IOException {
        byte[] dataBuf = FileUtils.readBytes(file);
        String[] dats = new String(dataBuf).split("\n");
        for (String line : dats) {
            //如果是注释则跳过处理
            if (line.startsWith("#")) continue;
            if (line.startsWith(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查询键是否存在!
     *
     * @param key  键
     * @param file 查询的文件!
     * @return 对应的键是否存在于配置文件中!
     */
    public static boolean isKVExists(String key, String value, File file) throws IOException {
        byte[] dataBuf = FileUtils.readBytes(file);
        String[] dats = new String(dataBuf).split("\n");
        for (String line : dats) {
            //如果是注释则跳过处理
            if (line.startsWith("#")) continue;
            if (line.startsWith(key) && getValue(line).equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param key   键
     * @param value 值
     * @param file  欲操作的文件!
     */
    public static void insertKV(String key, String value, File file) throws IOException {
        String line = warp2KvLine(key, value) + "\n";
        FileUtils.writeBytes(line.getBytes(), file, true);
    }

    /**
     * 凡是匹配到的键信息行，全部略过，然后将区间外的行写回去!
     *
     * @param key  键
     * @param file 操作的文件
     */
    public static void deleteKV(String key, File file) throws IOException {
        byte[] dataBuf = FileUtils.readBytes(file);
        String[] dats = new String(dataBuf).split("\n");
        ArrayList<String> valueList = new ArrayList<>(16);
        for (String line : dats) {
            if (!line.startsWith(key)) {
                valueList.add(line);
            }
        }
        String ret = StringUtil.arr2Str(valueList.toArray(new String[0]), "\n", true);
        FileUtils.writeBytes(ret.getBytes(), file, false);
    }

    /**
     * 凡是匹配到的键信息行，全部略过，然后将区间外的行写回去!
     *
     * @param key   键
     * @param value 值
     * @param file  操作的文件
     */
    public static void deleteKV(String key, String value, File file) throws IOException {
        byte[] dataBuf = FileUtils.readBytes(file);
        String[] dats = new String(dataBuf).split("\n");
        ArrayList<String> valueList = new ArrayList<>(16);
        for (String line : dats) {
            if (!(line.startsWith(key) && getValue(line).equals(value))) {
                Log.d(LOG_TAG, "添加了行: " + line);
                valueList.add(line);
            }
        }
        String ret = StringUtil.arr2Str(valueList.toArray(new String[0]), "\n", true);
        FileUtils.writeBytes(ret.getBytes(), file, false);
    }

    public static char toChar(String v, char defaultVar) {
        try {
            return v.toCharArray()[0];
        } catch (Exception e) {
            return defaultVar;
        }
    }

    public static short toShort(String v, short defaultVar) {
        try {
            return Short.valueOf(v);
        } catch (Exception e) {
            return defaultVar;
        }
    }

    public static int toInt(String v, int defaultVar) {
        try {
            return Integer.valueOf(v);
        } catch (Exception e) {
            return defaultVar;
        }
    }

    public static long toLong(String v, long defaultVar) {
        try {
            return Long.valueOf(v);
        } catch (Exception e) {
            return defaultVar;
        }
    }

    public static float toFloat(String v, float defaultVar) {
        try {
            return Float.valueOf(v);
        } catch (Exception e) {
            return defaultVar;
        }
    }

    public static double toDouble(String v, double defaultVar) {
        try {
            return Double.valueOf(v);
        } catch (Exception e) {
            return defaultVar;
        }
    }

    public static boolean toBoolean(String v, boolean defaultVar) {
        try {
            return Boolean.valueOf(v);
        } catch (Exception e) {
            return defaultVar;
        }
    }

    private static String getValue(String line) {
        //pm3DelayTime :
        return RegexGroupUtil.matcherGroup(line, ".* : (.*)", 1, 0);
    }

    private static String warp2KvLine(String key, String value) {
        return key + " : " + value;
    }
}
