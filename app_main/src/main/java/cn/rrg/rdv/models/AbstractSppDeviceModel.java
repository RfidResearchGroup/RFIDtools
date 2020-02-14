package cn.rrg.rdv.models;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import cn.rrg.com.DevCallback;
import cn.rrg.rdv.javabean.DevBean;
import cn.rrg.rdv.callback.ConnectCallback;
import cn.rrg.rdv.util.Commons;

public abstract class AbstractSppDeviceModel
        extends AbstractDeviceModel<BluetoothDevice, BluetoothAdapter> {
    @Override
    public DevCallback<BluetoothDevice> getDevCallback() {
        return new DevCallback<BluetoothDevice>() {
            @Override
            public void onAttach(BluetoothDevice dev) {
                //添加设备进列表中!
                DevBean devBean = new DevBean(dev.getName(), dev.getAddress());
                addDev2List(devBean);
                attachDispatcher(devBean);
            }

            @Override
            public void onDetach(BluetoothDevice dev) {
                DevBean devBean = new DevBean(dev.getName(), dev.getAddress());
                //再自己移除!
                Commons.removeDevByList(devBean, devAttachList);
                //如果设置了接口实现则需要通知接口移除
                detachDispatcher(devBean);
                Log.d(TAG, "将会移除该设备: " + devBean);
            }
        };
    }

    @Override
    public void discovery(Context context) {
        //由于是蓝牙，因此直接发送调用API搜索即可!
        if (mDI != null && mDI.getAdapter() != null)
            mDI.getAdapter().startDiscovery();
    }

    @Override
    public DevBean[] getHistory() {
        if (mDI != null && mDI.getAdapter() != null) {
            return Commons.getDevsFromBTAdapter(mDI.getAdapter());
        } else {
            return new DevBean[0];
        }
    }

    @Override
    public void connect(String address, ConnectCallback callback) {
        //先判断地址，地址不正常就不可以连接，地址是其他种类的驱动的也要直接跳过!
        if (!Commons.isUsbDevice(address)) {
            BluetoothDevice device = mDI.getAdapter().getRemoteDevice(address);
            boolean ret = mDI.connect(device);
            if (ret) {
                callback.onConnectSucces();
            } else {
                callback.onConnectFail();
            }
        }
    }

    @Override
    public void disconnect() {
        if (mDI != null)
            mDI.disconect();
    }
}
