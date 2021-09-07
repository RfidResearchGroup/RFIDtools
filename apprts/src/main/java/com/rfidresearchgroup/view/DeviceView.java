package com.rfidresearchgroup.view;

import com.rfidresearchgroup.javabean.DevBean;
import com.rfidresearchgroup.callback.BaseCallback;

public interface DeviceView extends BaseMvpView, BaseCallback.ErrorCallback<String> {

    //驱动没有初始化时的异常回调
    void onRegisterError(String name);

    //设备移除时的回调
    void devDetach(DevBean devBean);

    //连接失败时的操作
    void onConnectFail();

    //连接成功的操作
    void onConnectSuccess();

    //Nfc设备初始化成功时的回调
    void onInitNfcAdapterSuccess();

    //Nfc设备初始化失败时的回调
    void onInitNfcAdapterFail();
}
