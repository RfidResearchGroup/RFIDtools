package com.rfidresearchgroup.view;

public interface DriverView
        extends BaseMvpView {
    //驱动切换，返回检测到的类型
    void onCheckDriver(String curDriver);

    //在切换成功后
    void onDriverChange(String after);
}
