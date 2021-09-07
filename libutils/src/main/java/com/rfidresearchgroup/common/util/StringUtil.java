package com.rfidresearchgroup.common.util;

public class StringUtil {

    //字符串数组转字符串，可选择性的添加分隔符!
    public static String arr2Str(String[] arr, String split, boolean addSplitInEnd) {
        StringBuilder sb = new StringBuilder();
        //进行迭代添加!
        for (int i = 0; i < arr.length; ++i) {
            //判断是否是在尾部
            if (i == arr.length - 1 && !addSplitInEnd) {
                //不添加换行符!
                sb.append(arr[i]);
            } else {
                sb.append(arr[i]).append(split);
            }
        }
        return sb.toString();
    }

    //是否是空串
    public static boolean isEmpty(String str) {
        return str.isEmpty() || str.equals(" ") || str.matches("\\s*");
    }

    //是否是十六进制字符串
    public static boolean isHexStr(String str) {
        return str.matches("[0-9a-fA-F]+");
    }

    //是否是数字
    public static boolean isNumStr(String str) {
        return str.matches("[0-9]+");
    }

    //是否是字母
    public static boolean isLetter(String str) {
        return str.matches("[A-Fa-f]+");
    }

    //是否是纯空格
    public static boolean isSpaces(String str) {
        return str.matches(" +");
    }

    //删除所有的空格和转为大写!
    public static String trimO2Upper(String content) {
        return content.replaceAll(" ", "").toUpperCase();
    }

    //删除所有的空格和转为小写!
    public static String trimO2Lower(String content) {
        return content.replaceAll(" ", "").toLowerCase();
    }

    //删除所有的空格和转为大写!
    public static void trimO2Upper(String[] datas) {
        for (int i = 0; i < datas.length; i++) {
            datas[i] = trimO2Upper(datas[i]);
        }
    }

    //删除所有的空格和转为小写!
    public static void trimO2Lower(String[] datas) {
        for (int i = 0; i < datas.length; i++) {
            datas[i] = trimO2Lower(datas[i]);
        }
    }
}
