package com.rfidresearchgroup.models;

import com.rfidresearchgroup.util.Commons;

import com.proxgrind.com.DeviceChecker;

import cn.rfidresearchgroup.chameleon.defined.IChameleonExecutor;
import cn.rfidresearchgroup.chameleon.executor.ChameleonExecutor;
import cn.rfidresearchgroup.chameleon.executor.ExecutorImpl;
import com.proxgrind.devices.EmptyDeivce;
import com.rfidresearchgroup.callback.ConnectCallback;
import com.rfidresearchgroup.callback.InitNfcCallback;

public class ChameleonUsb2UartModel extends AbsUsb2UartModel {
    @Override
    public DeviceChecker getDeviceInitImpl() {
        return new EmptyDeivce(null);
    }

    @Override
    public void connect(String address, ConnectCallback callback) {
        // super.connect(address, callback);
        if (mDI == null) {
            callback.onConnectFail();
            return;
        }
        if (Commons.isUsbDevice(address)) {
            boolean ret = mDI.connect(address);
            if (ret) {
                callback.onConnectSucces();
            } else {
                callback.onConnectFail();
            }
        }
    }

    @Override
    public void init(InitNfcCallback callback) {
        if (isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    IChameleonExecutor executor = ChameleonExecutor.get();
                    if (executor.initExecutor(mDI)) {
                        ExecutorImpl.setExecutor(executor);
                        callback.onInitSuccess();
                    } else {
                        callback.onInitFail();
                    }
                }
            }).start();
        }
    }
}
