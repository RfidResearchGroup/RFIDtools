package com.rfidresearchgroup.common.util;

import android.util.SparseIntArray;
import android.widget.Spinner;

/*
 * 保存Spinner的加载状态!
 * */
public class SpinnerInitState {
    //利用键值对进行第一次加载的限制!
    private SparseIntArray spinnerFirstState = new SparseIntArray();

    public boolean isNotInitialized(Spinner spinner) {
        if (spinnerFirstState.get(spinner.getId(), -1) == -1) {
            //判断到没有初始化，则设置为初始化!
            setInitialized(spinner);
            return true;
        }
        return false;
    }

    public void setNotInitialized(Spinner spinner) {
        if (spinnerFirstState.get(spinner.getId(), -1) != -1) {
            spinnerFirstState.delete(spinner.getId());
        }
    }

    public void setInitialized(Spinner spinner) {
        if (spinnerFirstState.get(spinner.getId(), -1) == -1) {
            spinnerFirstState.put(spinner.getId(), spinner.getId());
        }
    }
}
