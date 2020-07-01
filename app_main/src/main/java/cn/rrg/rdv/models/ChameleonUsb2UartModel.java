package cn.rrg.rdv.models;

import com.iobridges.com.DeviceChecker;

import cn.rrg.chameleon.defined.IChameleonExecutor;
import cn.rrg.chameleon.executor.ChameleonExecutor;
import cn.rrg.chameleon.executor.ExecutorImpl;
import cn.proxgrind.devices.EmptyDeivce;
import cn.rrg.rdv.callback.ConnectCallback;
import cn.rrg.rdv.callback.InitNfcCallback;
import cn.rrg.rdv.util.Commons;

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
