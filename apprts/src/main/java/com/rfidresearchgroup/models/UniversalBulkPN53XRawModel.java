package com.rfidresearchgroup.models;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.rfidresearchgroup.util.Commons;

import com.proxgrind.com.DeviceChecker;

import com.rfidresearchgroup.common.util.AppUtil;
import com.proxgrind.com.DevCallback;
import com.proxgrind.com.DriverInterface;
import com.proxgrind.com.UsbBulkTransfer;
import com.rfidresearchgroup.devices.PN53X;
import com.rfidresearchgroup.callback.ConnectCallback;
import com.rfidresearchgroup.javabean.DevBean;

public class UniversalBulkPN53XRawModel extends AbstractDeviceModel<String, UsbManager> {

    private String name;

    public UniversalBulkPN53XRawModel(String name) {
        super();
        this.name = name;
    }

    @Override
    public DriverInterface<String, UsbManager> getDriverInterface() {
        return UsbBulkTransfer.getTransfer();
    }

    @Override
    public DeviceChecker getDeviceInitImpl() {
        if (name == null) return null;
        return new PN53X(name, mDI);
    }

    @Override
    public DevCallback<String> getDevCallback() {
        return new DevCallback<String>() {
            @Override
            public void onAttach(String dev) {
                //添加设备进列表中!
                DevBean devBean = new DevBean(dev, "00:00:00:00:00:01");
                addDev2List(devBean);
                attachDispatcher(devBean);
            }

            @Override
            public void onDetach(String dev) {
                DevBean devBean = new DevBean(dev, "00:00:00:00:00:01");
                //再自己移除!
                Commons.removeDevByList(devBean, devAttachList);
                //如果设置了接口实现则需要通知接口移除
                detachDispatcher(devBean);
            }
        };
    }

    @Override
    public void discovery(Context context) {
        if (mDI != null) {
            UsbManager manager = mDI.getAdapter();
            if (manager != null)
                if (manager.getDeviceList().size() > 0) {
                    Log.d(TAG, "发送广播成功！");
                    AppUtil.getInstance()
                            .getApp()
                            .sendBroadcast(
                                    new Intent(UsbBulkTransfer.getTransfer().getDeviceDiscoveryAction())
                            );
                } else Log.d(TAG, "startDiscovery: 找不到任何一个USB设备!");
        }
    }

    @Override
    public DevBean[] getHistory() {
        return new DevBean[0];
    }

    @Override
    public void connect(String address, ConnectCallback callback) {
        if (mDI == null) {
            callback.onConnectFail();
            return;
        }
        boolean ret = mDI.connect(name);
        if (ret) {
            callback.onConnectSucces();
        } else {
            callback.onConnectFail();
        }
    }

    @Override
    public void disconnect() {
        if (mDI != null) {
            mDI.disconect();
        }
    }
}
