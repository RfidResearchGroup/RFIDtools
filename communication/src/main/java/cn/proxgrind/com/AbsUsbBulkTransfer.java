package cn.proxgrind.com;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.iobridges.bulkio.BulkInputStream;
import com.iobridges.bulkio.BulkOutputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.proxgrind.utils.ContextContentProvider;

public abstract class AbsUsbBulkTransfer implements DriverInterface<String, UsbManager> {

    // Application context, is global. cant cache activity context!
    private Context mContext = ContextContentProvider.mContext;
    private static final String LOG_TAG = AbsUsbBulkTransfer.class.getSimpleName();
    private DevCallback<String> mCallback = null;
    private BroadcastReceiver mReceiver;
    private boolean isRegister = false;
    private IntentFilter filter = new IntentFilter();
    private UsbManager mUsbManger = null;
    private UsbDevice mDevice = null;
    private UsbDeviceConnection mCon = null;
    private UsbInterface mUi = null;
    private UsbEndpoint mEpIn = null;
    private UsbEndpoint mEpOut = null;
    private BulkOutputStream outputStream;
    private BulkInputStream inputStream;

    protected AbsUsbBulkTransfer() {
        final String act = getDeviceDiscoveryAction();
        final String name = getDeviceNameOnFound();
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(act);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String _action = intent.getAction();
                if (_action == null) return;
                if (_action.equals(act)) {
                    initAndCall(name);
                    return;
                }
                switch (_action) {
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        initAndCall(name);
                        break;

                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                        if (mCallback != null)
                            mCallback.onDetach(name);
                        break;
                }
            }
        };
    }

    private void initAndCall(String name) {
        if (init()) {
            if (mCallback != null)
                mCallback.onAttach(name);
        }
    }

    private void register1() {
        unRegister();
        try {
            mContext.registerReceiver(mReceiver, filter);
            isRegister = true;
        } catch (Exception ignored) {
        }
    }

    private void unRegister() {
        try {
            if (isRegister) {
                mContext.unregisterReceiver(mReceiver);
                isRegister = false;
            }
        } catch (Exception ignored) {
        }
    }

    public abstract boolean isRawDevice(int producetId, int ventorId);

    public abstract String getDeviceDiscoveryAction();

    public abstract String getDeviceNameOnFound();

    public UsbDevice getUsbDevice() {
        return mDevice;
    }

    //初始化
    private boolean init() {
        if (mUsbManger == null) {
            Log.d(LOG_TAG, "USB管理器为空!");
            return false;
        }
        HashMap<String, UsbDevice> _hmDevs = mUsbManger.getDeviceList();
        if (_hmDevs == null || _hmDevs.size() <= 0) return false;
        List<UsbDevice> _devs = new ArrayList<>(_hmDevs.values());
        if (_devs.size() <= 0) return false;
        mDevice = _devs.get(0);
        if (mDevice == null) return false;
        if (isRawDevice(mDevice.getProductId(), mDevice.getVendorId())) {
            if (!mUsbManger.hasPermission(mDevice)) {
                mCon = mUsbManger.openDevice(mDevice);
                if (mCon == null) {
                    PendingIntent intent = PendingIntent.getBroadcast(mContext, 0, new Intent(getDeviceDiscoveryAction()), 0);
                    Log.d(LOG_TAG, "trying get usb permission!");
                    mUsbManger.requestPermission(mDevice, intent);
                    return false;
                }
            } else {
                mCon = mUsbManger.openDevice(mDevice);
            }
        } else {
            Log.d(LOG_TAG, "RAW支持检测结果: false");
            return false;
        }
        return true;
    }

    @Override
    public void register(final DevCallback<String> callback) {
        mCallback = callback;
        mUsbManger = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        // register driver
        register1();
    }

    @Override
    public boolean connect(String t) {
        if (mDevice == null) {
            Log.e(LOG_TAG, "devices is null!");
            return false;
        }
        if (isRawDevice(mDevice.getProductId(), mDevice.getVendorId())) {
            for (int iIndex = 0; iIndex < mDevice.getInterfaceCount(); ++iIndex) {
                UsbInterface tmpUi = mDevice.getInterface(iIndex);
                if (tmpUi.getInterfaceClass() == UsbConstants.USB_CLASS_CSCID) {
                    // 3 Endpoints maximum: Interrupt In, Bulk In, Bulk Out
                    mUi = tmpUi;
                }
            }
            if (mUi == null) {
                Log.d(LOG_TAG, "usb interface is null!");
                return false;
            }
            if (mCon != null) {
                if (!mCon.claimInterface(mUi, true)) {
                    Log.e(LOG_TAG, "Cannot claim interface.");
                    return false;
                }
            }
            Log.d(LOG_TAG, "connect: Endpoint count: " + mUi.getEndpointCount());
            for (int i = 0; i < mUi.getEndpointCount(); ++i) {
                UsbEndpoint _ue = mUi.getEndpoint(i);
                if (_ue.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
                    Log.d(LOG_TAG, "connect: Found interrupt endpoint: " + i);
                    continue;
                }
                if (_ue.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK
                        && _ue.getDirection() == UsbConstants.USB_DIR_IN) {
                    Log.d(LOG_TAG, "connect: Found read endpoint " + i);
                    mEpIn = _ue;
                }
                if (_ue.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK
                        && _ue.getDirection() == UsbConstants.USB_DIR_OUT) {
                    Log.d(LOG_TAG, "connect: Found write endpoint " + i);
                    mEpOut = _ue;
                }
            }
            if (mEpIn == null || mEpOut == null) {
                Log.d(LOG_TAG, "Invalid endpoint!");
                return false;
            }
            inputStream = new BulkInputStream(mCon, mEpIn);
            outputStream = new BulkOutputStream(mCon, mEpOut);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public UsbManager getAdapter() {
        return mUsbManger;
    }

    @Override
    public String getDevice() {
        return getDeviceNameOnFound();
    }

    @Override
    public void disconect() {
        if (mCon != null && mUi != null) {
            mCon.releaseInterface(mUi);
        }
    }

    @Override
    public void unregister() {
        unRegister();
    }

    @Override
    public OutputStream getOutput() {
        return outputStream;
    }

    @Override
    public InputStream getInput() {
        return inputStream;
    }
}
