package cn.rrg.rdv.models;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;

import cn.dxl.common.util.AppUtil;
import cn.proxgrind.com.DevCallback;
import cn.proxgrind.com.DriverInterface;
import cn.proxgrind.com.UsbSerialControl;
import cn.rrg.rdv.callback.ConnectCallback;
import cn.rrg.rdv.javabean.DevBean;
import cn.rrg.rdv.util.Commons;

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
