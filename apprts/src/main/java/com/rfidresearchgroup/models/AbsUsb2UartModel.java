package com.rfidresearchgroup.models;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;

import com.rfidresearchgroup.util.Commons;

import com.rfidresearchgroup.common.util.AppUtil;
import com.proxgrind.com.DevCallback;
import com.proxgrind.com.DriverInterface;
import com.proxgrind.com.UsbSerialControl;
import com.rfidresearchgroup.callback.ConnectCallback;
import com.rfidresearchgroup.javabean.DevBean;

public abstract class AbsUsb2UartModel extends AbstractDeviceModel<String, UsbManager> {

    @Override
    public DriverInterface<String, UsbManager> getDriverInterface() {
        return UsbSerialControl.get();
    }

    @Override
    public DevCallback<String> getDevCallback() {
        return new DevCallback<String>() {
            @Override
            public void onAttach(String dev) {
                DevBean devBean = new DevBean(dev, "00:00:00:00:00:00");
                addDev2List(devBean);
                attachDispatcher(devBean);
            }

            @Override
            public void onDetach(String dev) {
                DevBean devBean = new DevBean(dev, "00:00:00:00:00:00");
                Commons.removeDevByList(devBean, devAttachList);
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
                    AppUtil.getInstance().getApp().sendBroadcast(new Intent(UsbSerialControl.ACTION_BROADCAST));
                }
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
    public void disconnect() {
        if (mDI != null) {
            mDI.disconect();
        }
    }
}
