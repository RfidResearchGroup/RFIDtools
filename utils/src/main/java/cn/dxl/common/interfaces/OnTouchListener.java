package cn.dxl.common.interfaces;

import android.view.MotionEvent;

/*
 * 在触发时的回调!
 * */
public interface OnTouchListener {
    boolean onTouch(MotionEvent event);
}