package cn.rrg.rdv.util;

import java.util.ArrayList;

/*
 * 联动工具类
 * 负责进行符号特征添加，与解析!
 * */
public class UnionAction {
    /* 
     * 维护一个key数列
     * */
    private static final ArrayList<String> mKeyGobalList = new ArrayList<>(512);

    /*
     * 维护一个data数列
     * */
    private static final ArrayList<String> mDataGobalList = new ArrayList<>(512);

    /*
     * 可以对密钥数列进行添加操作的接口，同步操作，避免出现IndexOutOfBoundsException
     * */
    public static synchronized void addKey(String keyHex) {
        if (!mKeyGobalList.contains(keyHex)) {
            mKeyGobalList.add(keyHex);
        }
    }

    public static synchronized void addKey(String[] keysHex) {
        for (String key : keysHex) {
            addKey(key);
        }
    }

    /*
     * 可以对密钥进行移除
     * */
    public static synchronized void removeKey(String keyHex) {
        mKeyGobalList.remove(keyHex);
    }

    public static synchronized void removeKey() {
        mKeyGobalList.clear();
    }

    /*
     * 得到密钥组
     * */
    public static synchronized String[] getKeys() {
        return mKeyGobalList.toArray(new String[0]);
    }

    /*
     * 进行数据添加!
     * */
    public static synchronized void addData(String dataLine) {
        mDataGobalList.add(dataLine);
    }

    public static synchronized void addData(String[] dataLines) {
        for (String data : dataLines) {
            addData(data);
        }
    }

    /*
     * 进行数据移除
     * */
    public static synchronized void removeData(String data) {
        mDataGobalList.remove(data);
    }

    public static synchronized void removeData() {
        mDataGobalList.clear();
    }

    /*
     * 进行数据排序，倒转!
     * */
    public static synchronized void reverse() {
        String[] datas = getDatas();
        if (datas.length < 2) return;
        String[] tmp = new String[datas.length];
        for (int i = datas.length - 1, j = 0; i >= 0; i--, j++) {
            tmp[j] = datas[i];
        }
        removeData();
        addData(tmp);
    }

    /*
     * 得到数据组
     * */
    public static synchronized String[] getDatas() {
        return mDataGobalList.toArray(new String[0]);
    }

}
