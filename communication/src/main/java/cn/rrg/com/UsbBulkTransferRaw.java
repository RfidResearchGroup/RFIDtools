package cn.rrg.com;

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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.dxl.utils.ContextContentProvider;
import cn.rrg.bulkio.BulkInputStream;
import cn.rrg.bulkio.BulkOutputStream;

public abstract class UsbBulkTransferRaw implements DriverInterface<String, UsbManager> {

    // Application context, is global. cant cache activity context!
    private Context mContext = ContextContentProvider.mContext;
    //日志特征
    private static final String LOG_TAG = UsbBulkTransferRaw.class.getSimpleName();
    private DevCallback<String> mCallback = null;
    //广播接收器,在设备插入和移除时使用
    private BroadcastReceiver mReceiver;
    //主页状态!
    private boolean isRegister = false;
    //广播过滤器!
    private IntentFilter filter = new IntentFilter();
    //缓存设备管理器
    private UsbManager mUsbManger = null;
    //缓存插入的设备
    private UsbDevice mDevice = null;
    //缓存设备链接（实际上等同于串口port）
    private UsbDeviceConnection mCon = null;
    //USB接口
    private UsbInterface mUi = null;
    //USB读端点
    private UsbEndpoint mEpIn = null;
    //USB写端点
    private UsbEndpoint mEpOut = null;

    //私有构造方法，避免被直接调用
    protected UsbBulkTransferRaw() {
        final String act = getDeviceDiscoveryAction();
        final String name = getDeviceNameOnFound();
        //建立意图过滤数组
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
                    //在设备插入的时候初始化
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        initAndCall(name);
                        break;

                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                        mCallback.onDetach(name);
                        break;
                }
            }
        };
    }

    private void initAndCall(String name) {
        if (init()) {
            mCallback.onAttach(name);
        }
    }

    private void register1() {
        unRegister();
        try {
            //注册广播事件
            mContext.registerReceiver(mReceiver, filter);
            isRegister = true;
            Log.d(LOG_TAG, "注册广播成功!");
        } catch (Exception ignored) {
        }
    }

    private void unRegister() {
        try {
            if (isRegister) {
                //注册广播事件
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
        //得到设备集
        HashMap<String, UsbDevice> _hmDevs = mUsbManger.getDeviceList();
        if (_hmDevs == null || _hmDevs.size() <= 0) return false;
        List<UsbDevice> _devs = new ArrayList<>(_hmDevs.values());
        //判断插入的设备集是否存在设备
        if (_devs.size() <= 0) return false;
        //判断是否是ACR122
        mDevice = _devs.get(0);
        if (mDevice == null) return false;
        //判断设备厂商和设备型号
        if (isRawDevice(mDevice.getProductId(), mDevice.getVendorId())) {
            if (!mUsbManger.hasPermission(mDevice)) {
                //如果没有权限，则需要申请!
                mCon = mUsbManger.openDevice(mDevice);
                //空链接，可能需要申请权限!
                if (mCon == null) {
                    //发送广播申请权限
                    PendingIntent intent = PendingIntent.getBroadcast(mContext, 0, new Intent(getDeviceDiscoveryAction()), 0);
                    Log.d(LOG_TAG, "trying get usb permission!");
                    mUsbManger.requestPermission(mDevice, intent);
                    //当没有权限的时候应当直接返回
                    return false;
                }
            } else {
                //如果有权限，可以直接打开！
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
        //初始化USB管理器资源
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
        //再一次判断设备正确性
        if (isRawDevice(mDevice.getProductId(), mDevice.getVendorId())) {
            for (int iIndex = 0; iIndex < mDevice.getInterfaceCount(); ++iIndex) {
                UsbInterface tmpUi = mDevice.getInterface(iIndex);
                int tmpClass = tmpUi.getInterfaceClass();
                if (tmpClass == 11) {
                    // 3 Endpoints maximum: Interrupt In, Bulk In, Bulk Out
                    mUi = tmpUi;
                }
                Log.d(LOG_TAG, "interface class: " + tmpClass);
            }
            if (mUi == null) {
                Log.d(LOG_TAG, "usb interface is null!");
                return false;
            }
            if (mCon != null) {
                if (!mCon.claimInterface(mUi, false) && !mCon.claimInterface(mUi, true)) {
                    throw new IllegalArgumentException("Cannot claim interface.");
                }
            }
            //端点初始化
            Log.d(LOG_TAG, "connect: Endpoint count: " + mUi.getEndpointCount());
            for (int i = 0; i < mUi.getEndpointCount(); ++i) {
                UsbEndpoint _ue = mUi.getEndpoint(i);
                //判断端点的类型
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
            //全部链接初始化成功，返回TRUE告诉调用者可以开始尝试通信
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
        //TODO don't need
    }

    @Override
    public void unregister() {
        unRegister();
    }

    @Override
    public OutputStream getOutput() {
        return new BulkOutputStream(mCon, mEpOut);
    }

    @Override
    public InputStream getInput() {
        return new BulkInputStream(mCon, mEpIn);
    }
}
