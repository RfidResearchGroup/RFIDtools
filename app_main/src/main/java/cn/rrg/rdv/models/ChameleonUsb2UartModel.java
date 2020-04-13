package cn.rrg.rdv.models;

import android.util.Log;

import cn.rrg.chameleon.defined.IChameleonExecutor;
import cn.rrg.chameleon.executor.ChameleonExecutor;
import cn.rrg.chameleon.executor.ExecutorImpl;
import cn.dxl.com.DeviceChecker;
import cn.rrg.devices.EmptyDeivce;
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
        // 我们不需要进行Com类的初始化，因为变色龙是纯JAVA编程实现通讯的!
        // super.connect(address, callback);
        if (mDI == null) {
            Log.d(TAG, "驱动为空!");
            callback.onConnectFail();
            return;
        }
        Log.d(TAG, "开始连接Chameleon Usb!");
        // TODO USB直接连接地址即可!
        if (Commons.isUsbDevice(address)) {
            boolean ret = mDI.connect(address);
            Log.d(TAG, "连接完成: " + ret);
            if (ret) {
                callback.onConnectSucces();
            } else {
                callback.onConnectFail();
            }
        }
    }

    @Override
    public void init(InitNfcCallback callback) {
        Log.d(TAG, "调用了初始化!");
        //TODO 最终是在这里实现的NFC设备的通信初始化和设备唤醒!
        if (isConnected()) {
            Log.d(TAG, "该驱动已经链接初始化成功，将使用该驱动进行初始化设备!");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //直接连接，有需要时可重写!
                    IChameleonExecutor executor = ChameleonExecutor.get();
                    if (executor.initExecutor(mDI)) {
                        // 初始化代理持有!
                        ExecutorImpl.setExecutor(executor);
                        callback.onInitSuccess();
                    } else {
                        callback.onInitFail();
                    }
                }
            }).start();
        } else {
            Log.d(TAG, "该驱动未链接，将禁用该驱动在接下来的初始化设备任务!");
        }
    }
}
