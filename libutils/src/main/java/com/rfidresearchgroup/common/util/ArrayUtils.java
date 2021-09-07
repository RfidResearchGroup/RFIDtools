package com.rfidresearchgroup.common.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/*
 * 一些数组，集合操作的封装类!
 * */
public class ArrayUtils {
    //将字符串链表或者数列转换为一般字符串数组
    public static <T> T[] list2Arr(List<T> list) {
        if (list == null) return null;
        if (list.size() == 0) return null;
        return list.toArray((T[]) Array.newInstance(list.get(0).getClass(), list.size()));
    }

    //obj数组去重
    public static <T> T[] unrepeat(T[] objs) {
        ArrayList<T> list = new ArrayList<>();
        for (int i = 0; i < objs.length; ++i) {
            if (list.indexOf(objs[i]) == -1) {
                //-1证明没有找到这个元素的存在，添加进去!
                list.add(objs[i]);
            }
        }
        return list.toArray((T[]) Array.newInstance(objs[0].getClass(), list.size()));
    }

    /**
     * 得到一个数组中的元素，如果越界则为空!
     *
     * @param array 需要被取出元素的数组!
     * @param index 需要取出的对应数组的元素的索引!
     * @return 如果不越界，则取出对应的值，否则直接返回null
     */
    public static <T> T getElement(T[] array, int index) {
        return index >= array.length ? null : array[index];
    }
}
