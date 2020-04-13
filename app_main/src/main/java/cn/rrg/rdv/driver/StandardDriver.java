package cn.rrg.rdv.driver;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.util.Log;

import java.io.IOException;

import cn.dxl.common.util.AppUtil;
import cn.dxl.mifare.StdMifareIntent;
import cn.rrg.com.DevCallback;
import cn.rrg.com.DriverInterface;

public class StandardDriver implements DriverInterface<String, NfcAdapter> {

    private Application context = AppUtil.getInstance().getApp();
    private static final String LOG_TAG = StandardDriver.class.getSimpleName();
    private static final int UNIQUE_ID = 0x05;
    private DevCallback<String> callback = null;
    private StdMifareIntent mMftools = null;
    private volatile static boolean isRegister = false;
    private static StandardDriver mThiz;

    static {
        mThiz = new StandardDriver();
    }

    /*
     * 请求发现设备的广播!
     * */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "收到了设备寻找广播!");
            if (getAdapter() != null) {
                if (getAdapter().isEnabled()) {
                    callback.onAttach(getDevice());
                } else {
                    callback.onDetach(getDevice());
                }
            } else {
                Log.d(LOG_TAG, "当前设备不支持标准NFC设备!");
            }
        }
    };

    private StandardDriver() {
        //单例实现
    }

    public static StandardDriver get() {
        return mThiz;
    }

    @Override
    public void register(DevCallback<String> callback) {
        if (!isRegister) {
            //实例化工具持有
            mMftools = new StdMifareIntent(context);
            //缓存回调
            this.callback = callback;
            //标志位更改
            isRegister = true;
            //注册设备发现广播!
            context.registerReceiver(mReceiver, new IntentFilter("cn.rrg.devices.std_discovery"));
        }
    }

    @Override
    public boolean connect(String t) {
        //对于设备的连接直接返回设备当前的开关状态即可!
        if (getAdapter() != null)
            return getAdapter().isEnabled();
        return false;
    }

    @Override
    public NfcAdapter getAdapter() {
        return mMftools.getAdapter();
    }

    @Override
    public String getDevice() {
        return "标准NFC设备";
    }

    @Override
    public void disconect() {
        //TODO 待实现!
        if (callback != null)
            callback.onDetach(getDevice());
    }

    @Override
    public int getUniqueId() {
        return UNIQUE_ID;
    }

    @Override
    public void unregister() {
        //解注册广播监听事件!
        context.unregisterReceiver(mReceiver);
        isRegister = false;
    }

    @Override
    public int write(byte[] sendMsg, int offset, int length, int timeout) throws IOException {
        throw new IOException("can't invoke");
    }

    @Override
    public int read(byte[] recvMsg, int offset, int length, int timeout) throws IOException {
        throw new IOException("can't invoke");
    }

    @Override
    public void flush() throws IOException {
        throw new IOException("can't invoke");
    }

    @Override
    public void close() throws IOException {
        throw new IOException("can't invoke");
    }
}
