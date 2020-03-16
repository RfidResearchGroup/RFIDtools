package cn.rrg.rdv.models;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;

import cn.dxl.common.util.AppUtil;
import cn.rrg.com.ContextCallback;
import cn.rrg.com.ContextHandle;
import cn.rrg.com.DevCallback;
import cn.rrg.com.DriverInterface;
import cn.rrg.com.UsbSerialControl;
import cn.rrg.rdv.callback.ConnectCallback;
import cn.rrg.rdv.javabean.DevBean;
import cn.rrg.rdv.util.Commons;

public abstract class AbsUsb2UartModel extends AbstractDeviceModel<String, UsbManager> {

    @Override
    public DriverInterface<String, UsbManager> getDriverInterface() {
        return DriverSource.driverMap.get(0x04);
    }

    @Override
    public DevCallback<String> getDevCallback() {
        return new DevCallback<String>() {
            @Override
            public void onAttach(String dev) {
                //添加设备进列表中!
                DevBean devBean = new DevBean(dev, "00:00:00:00:00:00");
                addDev2List(devBean);
                attachDispatcher(devBean);
            }

            @Override
            public void onDetach(String dev) {
                DevBean devBean = new DevBean(dev, "00:00:00:00:00:00");
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
                    AppUtil.getInstance().getApp().sendBroadcast(new Intent(UsbSerialControl.ACTION_BROADCAST));
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
            Log.d(TAG, "驱动为空!");
            callback.onConnectFail();
            return;
        }
        Log.d(TAG, "开始连接Rdv4 Usb!");
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
    public void disconnect() {
        if (mDI != null) {
            mDI.disconect();
        }
    }
}
