package cn.rrg.rdv.models;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.io.File;

import cn.dxl.common.util.FileUtil;
import cn.rrg.com.DevCallback;
import cn.rrg.com.Device;
import cn.rrg.com.DriverInterface;
import cn.rrg.devices.PN53X;
import cn.rrg.rdv.callback.ConnectCallback;
import cn.rrg.rdv.javabean.DevBean;
import cn.rrg.rdv.util.Commons;
import cn.rrg.rdv.util.Paths;

public class Acr122uUsbRawModel extends AbstractDeviceModel<String, UsbManager> {
    @Override
    public DriverInterface<String, UsbManager> getDriverInterface() {
        return DriverSource.driverMap.get(0x03);
    }

    @Override
    public Device getDeviceInitImpl() {
        return new PN53X();
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
        //寻找USB_122设备
        if (mDI != null) {
            UsbManager manager = mDI.getAdapter();
            if (manager != null)
                if (manager.getDeviceList().size() > 0) {
                    context.sendBroadcast(new Intent("cn.coderboy.nfc.usb_attach_acr"));
                } else Log.d(TAG, "startDiscovery: 找不到任何一个USB设备!");
        }
    }

    @Override
    public DevBean[] getHistory() {
        return new DevBean[0];
    }

    @Override
    public void connect(String address, ConnectCallback callback) {
        //TODO 连接USB_122U
        if (mDI == null) {
            callback.onConnectFail();
            return;
        }
        // TODO 谨记更换底层配置!
        FileUtil.writeString(new File(Paths.PN53X_CONF_FILE), "ACR122", false);
        boolean ret = mDI.connect(null);
        if (ret) {
            Log.d(TAG, "Acr122连接成功!");
            callback.onConnectSucces();
        } else {
            Log.d(TAG, "Acr122连接失败!");
            callback.onConnectFail();
        }
    }

    @Override
    public void disconnect() {
        if (mDI != null)
            mDI.disconect();
    }
}