package cn.rrg.rdv.models;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;

import cn.rrg.com.DevCallback;
import cn.rrg.com.DriverInterface;
import cn.rrg.rdv.driver.StandardDriver;
import cn.rrg.com.Device;
import cn.rrg.devices.EmptyDeivce;
import cn.rrg.rdv.javabean.DevBean;
import cn.rrg.rdv.callback.ConnectCallback;
import cn.rrg.rdv.callback.InitNfcCallback;
import cn.rrg.rdv.util.Commons;

public class StandardNFCDeviceModel extends AbstractDeviceModel<String, NfcAdapter> {
    @Override
    public DriverInterface<String, NfcAdapter> getDriverInterface() {
        return StandardDriver.get();
    }

    @Override
    public Device getDeviceInitImpl() {
        //直接返回一个空设备即可!
        return new EmptyDeivce();
    }

    @Override
    public DevCallback<String> getDevCallback() {
        return new DevCallback<String>() {
            @Override
            public void onAttach(String dev) {
                DevBean devBean = new DevBean(dev, "00:00:00:00:00:02");
                //添加设备进列表中!
                addDev2List(devBean);
                attachDispatcher(devBean);
            }

            @Override
            public void onDetach(String dev) {
                //自带的NFC设备是无法被移除的，只有一种可能，NFC设备被关闭!
                DevBean devBean = new DevBean(dev, "00:00:00:00:00:02");
                //再自己移除!
                Commons.removeDevByList(devBean, devAttachList);
                //如果设置了接口实现则需要通知接口移除
                detachDispatcher(devBean);
            }
        };
    }

    @Override
    public void discovery(Context context) {
        //寻找标准NFC设备!
        if (mDI != null) {
            NfcAdapter adapter = mDI.getAdapter();
            if (adapter != null) {
                //以发送广播的形式回调驱动寻找设备!
                context.sendBroadcast(new Intent("cn.rrg.devices.std_discovery"));
            }
        }
    }

    @Override
    public DevBean[] getHistory() {
        return new DevBean[0];
    }

    @Override
    public void connect(String address, ConnectCallback callback) {
        //TODO 链接标准的NFC设备!
        if (mDI == null) {
            callback.onConnectFail();
            return;
        }
        boolean ret = mDI.connect(address);
        if (ret) {
            callback.onConnectSucces();
        } else {
            callback.onConnectFail();
        }
    }

    @Override
    public void init(InitNfcCallback callback) {
        //直接返回true，不需要进行C层Com的任何初始化!
        callback.onInitSuccess();
    }

    @Override
    public void disconnect() {
        //直调调用驱动的实现!
        if (mDI != null)
            mDI.disconect();
    }
}
