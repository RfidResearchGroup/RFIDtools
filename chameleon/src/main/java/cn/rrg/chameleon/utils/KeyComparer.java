package cn.rrg.chameleon.utils;

import android.os.Build;

import java.util.Comparator;

import cn.rrg.chameleon.javabean.DetectionDatas;

public class KeyComparer implements Comparator<DetectionDatas> {
    @Override
    public int compare(DetectionDatas x, DetectionDatas y) {
        //simply
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Integer.compare(x.getBlock(), y.getBlock());
        } else {
            return Integer.valueOf(x.getBlock()).compareTo(y.getBlock());
        }
    }
}