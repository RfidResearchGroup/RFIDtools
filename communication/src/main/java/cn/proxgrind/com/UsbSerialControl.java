package cn.proxgrind.com;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import cn.proxgrind.utils.ContextContentProvider;

/*
 * Usb 2 uart Serial implements
 */
public class UsbSerialControl implements DriverInterface<String, UsbManager> {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext = ContextContentProvider.mContext;
    //日志标签
    private static final String LOG_TAG = UsbSerialControl.class.getSimpleName();
    //广播名称
    public static final String ACTION_BROADCAST = "com.rrg.devices.usb_attach_uart";
    //权限请求广播!
    private static final String ACTION_PERMISSION = "com.rrg.devices.usb_permission_uart";
    //设备名称!
    public static final String NAME_DRIVER_USB_UART = "OTGToUartSerial(OTG转串口)";
    //串口对象
    private volatile UsbSerialDevice mPort = null;
    //单例模式
    private static UsbSerialControl mThiz = null;
    //回调接口
    private DevCallback<String> mCallback = null;
    //广播接收，由于是单例，因此实际上广播接收也可以设置为单例!
    private BroadcastReceiver usbReceiver;
    //注册状态
    private boolean isRegister = false;
    //广播过滤!
    private IntentFilter filter = new IntentFilter();

    private UsbSerialControl() {
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_BROADCAST);
        filter.addAction(ACTION_PERMISSION);
        usbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //取出广播的意图
                String action = intent.getAction();
                if (action != null) {
                    //在申请权限的时候如果成功那么应当进行设备的初始化
                    if (action.equals(ACTION_PERMISSION)) {
                        //get permission success
                        if (init1()) {
                            //初始化成功则回调串口设备加入方法
                            if (mCallback != null)
                                mCallback.onAttach(NAME_DRIVER_USB_UART);
                        } else {
                            //不成则打印到LOG
                            Log.e(LOG_TAG, "NAME_DRIVER_USB_UART: no usb permission!");
                        }
                    }

                    //对比意图，根据意图做出回调选择
                    if (action.equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED) ||
                            action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED) ||
                            action.equals(ACTION_BROADCAST)) {
                        Log.d(LOG_TAG, "收到UsbSerial设备寻找的广播!");
                        if (init1()) {
                            if (mCallback != null) {
                                //初始化成功则回调串口设备加入方法
                                mCallback.onAttach(NAME_DRIVER_USB_UART);
                            }
                        }
                    }

                    if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                        if (mThiz != null) {
                            mThiz.close();
                        }
                        if (mCallback != null) mCallback.onDetach(NAME_DRIVER_USB_UART);
                    }
                }
            }
        };
    }

    public static UsbSerialControl get() {
        synchronized (LOG_TAG) {
            if (mThiz != null) {
                return mThiz;
            } else {
                mThiz = new UsbSerialControl();
            }
            return mThiz;
        }
    }

    @Override
    public void register(DevCallback<String> callback) {
        mCallback = callback;
        register1();
    }

    @Override
    public boolean connect(String t) {
        return connect1();
    }

    @Override
    public UsbManager getAdapter() {
        return (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
    }

    @Override
    public String getDevice() {
        return mPort != null ? mPort.getClass().getSimpleName() : NAME_DRIVER_USB_UART;
    }

    private void close() {
        if (mPort == null) {
            //Log.e(LOG_TAG, "port is null");
            return;
        }
        mPort.close();
        mPort = null;
    }

    @Override
    public void disconect() {
        close();
    }

    @Override
    public void unregister() {
        unregister1();
    }

    @Override
    public OutputStream getOutput() {
        if (mPort != null) {
            return mPort.getOutputStream();
        }
        return null;
    }

    @Override
    public InputStream getInput() {
        if (mPort != null) {
            return mPort.getInputStream();
        }
        return null;
    }

    private boolean init1() {
        //得到Usb管理器
        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        if (usbManager == null) return false;
        //尝试取出所有可用的列表
        if (usbManager.getDeviceList() == null || usbManager.getDeviceList().size() <= 0) {
            Log.d(LOG_TAG, "initUsbSerial() 未发现设备!");
            return false;
        }
        //迭代集合里面的设备对象
        List<UsbDevice> devList = new ArrayList<>(usbManager.getDeviceList().values());
        //取出第一个USB对象
        UsbDevice usbDevice = devList.get(0);
        //判断设备是否支持
        if (!UsbSerialDevice.isSupported(usbDevice)) {
            Log.d(LOG_TAG, "UsbSerial支持检测结果: false");
            return false;
        }
        //如果对于这个设备没有权限!
        if (!usbManager.hasPermission(usbDevice)) {
            //发送广播申请权限
            PendingIntent intent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_PERMISSION), 0);
            //Log.d(LOG_TAG, "尝试获得USB权限!");
            usbManager.requestPermission(usbDevice, intent);
            //当没有权限的时候应当直接返回
            return false;
        }
        //一切正常返回true!
        return connect1();
    }

    private void register1() {
        if (!isRegister) {
            unregister1();
            try {
                mContext.registerReceiver(usbReceiver, filter);
                isRegister = true;
            } catch (Exception ignored) {
            }
        }
    }

    private void unregister1() {
        if (isRegister) {
            try {
                mContext.unregisterReceiver(usbReceiver);
                isRegister = false;
            } catch (Exception ignored) {
            }
        }
    }

    private boolean connect1() {
        if (mPort != null) {
            return true;
        }
        //得到Usb管理器
        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        if (usbManager == null) return false;
        //迭代集合里面的设备对象
        List<UsbDevice> devList = new ArrayList<>(usbManager.getDeviceList().values());
        if (devList.size() == 0) return false;
        //取出第一个USB对象
        UsbDevice usbDevice = devList.get(0);
        //USB链接!
        UsbDeviceConnection connection = usbManager.openDevice(usbDevice);
        //判断是否非空，为空证明没有权限
        if (connection == null) {
            //发送广播申请权限
            PendingIntent intent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_PERMISSION), 0);
            //Log.d(LOG_TAG, "尝试获得USB权限!");
            usbManager.requestPermission(usbDevice, intent);
            //当没有权限的时候应当直接返回
            return false;
        }
        //得到串口端口对象
        mPort = UsbSerialDevice.createUsbSerialDevice(usbDevice, connection);
        //尝试打开串口
        if (mPort.syncOpen()) {
            //设置波特率
            mPort.setBaudRate(115200);
            //设置数据位
            mPort.setDataBits(UsbSerialDevice.DATA_BITS_8);
            //设置停止位
            mPort.setStopBits(UsbSerialDevice.STOP_BITS_1);
            //奇偶校验值
            mPort.setParity(UsbSerialDevice.PARITY_NONE);
            //数据流控制
            mPort.setFlowControl(UsbSerialDevice.FLOW_CONTROL_OFF);
            Log.d(LOG_TAG, "Usb链接成功，通信创建成功!!");
            return true;
        }
        return false;
    }
}
