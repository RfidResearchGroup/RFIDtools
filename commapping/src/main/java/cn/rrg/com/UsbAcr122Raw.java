package cn.rrg.com;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.hardware.usb.UsbConstants;

public class UsbAcr122Raw implements DriverInterface<String, UsbManager> {

    //日志特征
    private static final String LOG_TAG = UsbAcr122Raw.class.getSimpleName();
    private static final int UNIQUE_ID = 0x03;
    //广播接收器,在设备插入和移除时使用
    private static BroadcastReceiver mReceiver = null;
    //控制广播注册状态
    private boolean isRegister = false;
    //缓存设备管理器
    private static UsbManager mUsbManger = null;
    //缓存插入的设备
    private static UsbDevice mDevice = null;
    //缓存设备链接（实际上等同于串口port）
    private static UsbDeviceConnection mCon = null;
    //USB接口
    private static UsbInterface mUi = null;
    //USB读端点
    private static UsbEndpoint mEpIn = null;
    //USB写端点
    private static UsbEndpoint mEpOut = null;
    //单例模式
    private static UsbAcr122Raw mUsbRaw = null;
    //设备码
    private static final int[] VP_ID = new int[]{
            120566529, 120566532, 120554240, 120554242,
            120554247, 120554241, 120557568, 120557775,
            120557772, 120557784, 120566016, 120566017,
            120566018, 120566019, 120566020, 120566028,
            120565760, 120557778, 120528913, 120555776,
            120555777, 120555778, 120525317, 120525316,
            120525318, 120529408, 120529454, 120529463,
            120529457, 120529428, 120525440, 120529415,
            120529451, 120529414, 120529445, 120529411,
            120529434, 120529449, 120529432, 120529435,
            120529458, 120529474, 120529464, 120529487,
            120529467, 120529470, 120529476, 120529497,
            120529471, 120529465, 120529425, 120529490,
            120529152, 120529444, 120529423, 120529443,
            120529416, 120523009, 120529418, 120529429,
            120529440, 120529459, 120529460, 120529461,
            120529462, 120529427, 120529452, 120529496,
            120529482, 120553985, 120553990, 120557574,
            120557787, 120566272, 120566022
    };

    //私有构造方法，避免被直接调用
    private UsbAcr122Raw() { /*can't using default constructor*/ }

    /*
     * 实例获取
     * */
    public static UsbAcr122Raw get() {
        synchronized (LOG_TAG) {
            if (mUsbRaw == null) {
                mUsbRaw = new UsbAcr122Raw();
            }
        }
        return mUsbRaw;
    }

    //初始化
    private boolean init(Context context) {
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
        if (isAcr122(mDevice.getProductId(), mDevice.getVendorId())) {
            if (!mUsbManger.hasPermission(mDevice)) {
                //如果没有权限，则需要申请!
                mCon = mUsbManger.openDevice(mDevice);
                //空链接，可能需要申请权限!
                if (mCon == null) {
                    //发送广播申请权限
                    PendingIntent intent =
                            PendingIntent.getBroadcast(context,
                                    0, new Intent("cn.rrg.devices.usb_attach_acr"), 0);
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

    private boolean isAcr122(int producetId, int ventorId) {
        boolean ret = false;
        int var3 = ventorId << 16 | producetId;
        int count = 0;

        while (true) {
            if (count >= 75) {
                break;
            }
            if (VP_ID[count] == var3) {
                ret = true;
                break;
            }
            ++count;
        }
        return ret;
    }

    @Override
    public void register(Context context, final DevCallback<String> callback) {
        //初始化USB管理器资源
        mUsbManger = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        //只注册一次广播
        if (isRegister) return;
        //建立意图过滤数组
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction("cn.rrg.devices.usb_attach_acr");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String _action = intent.getAction();
                if (_action == null) return;
                switch (_action) {
                    //在设备插入的时候初始化
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    case "cn.rrg.devices.usb_attach_acr": {
                        if (init(context)) {
                            callback.onAttach("ACR_122");
                        } else {
                            Log.d(LOG_TAG, "init failed!");
                        }
                    }
                    break;

                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                        callback.onDetach("ACR_122");
                        break;
                }
            }
        };
        try {
            //注册广播事件
            context.registerReceiver(mReceiver, filter);
        } catch (Exception ignored) {
        }
        //将标志置为true，避免重复注册!
        isRegister = true;
    }

    @Override
    public boolean connect(String t) {
        if (mDevice == null) {
            Log.e(LOG_TAG, "devices is null!");
            return false;
        }
        //再一次判断设备正确性
        if (isAcr122(mDevice.getProductId(), mDevice.getVendorId())) {
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
        return "Acr122";
    }

    @Override
    public void disconect() {
        //TODO don't need
    }

    @Override
    public int getUniqueId() {
        return UNIQUE_ID;
    }

    @Override
    public void unregister(Context context) {
        if (isRegister) {
            try {
                context.unregisterReceiver(mReceiver);
                isRegister = false;
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public int write(byte[] sendMsg, int offset, int length, int timeout) throws IOException {
        //Log.d(LOG_TAG, "write: " + HexUtil.toHexString(sendMsg, offset, length));
        //Log.d(LOG_TAG, "write: " + length);
        //if (len < 0) throw new IOException("write timeout");
        //Log.d(LOG_TAG, "write timeout value: " + timeout);
        //Log.d(LOG_TAG, "write length result: " + len);
        return mCon.bulkTransfer(mEpOut, sendMsg, offset, length, timeout);
    }

    @Override
    public int read(byte[] recvMsg, int offset, int length, int timeout) throws IOException {
        //Log.d(LOG_TAG, "read: " + HexUtil.toHexString(recvMsg, offset, length));
        //Log.d(LOG_TAG, "read: 要接收的字节数 " + length);
        /*do {
            len = mCon.bulkTransfer(mEpIn, recvMsg, offset, length, 50);
            Log.d(LOG_TAG, "read: 接收到的的字节数 " + len);
            if (len == -1) return;
        } while (len != length);
        */
        //Log.d(LOG_TAG, "read timeout value: " + timeout);
        //Log.d(LOG_TAG, "read length result: " + len);
        return mCon.bulkTransfer(mEpIn, recvMsg, offset, length, timeout);
        //Log.d(LOG_TAG, "read: " + len);
        //if (len < 0) throw new IOException("recv timeout");
    }

    @Override
    public void flush() throws IOException {
        //TODO don't need
    }

    @Override
    public void close() throws IOException {
        mCon.close();
    }
}
