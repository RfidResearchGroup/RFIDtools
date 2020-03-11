package cn.rrg.com;

import android.annotation.SuppressLint;
import android.app.Application;
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
import com.felhr.usbserial.UsbSerialInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cn.dxl.common.util.AppUtil;
import cn.dxl.common.util.HexUtil;

/*
 * Usb 2 uart Serial implements
 */
public class UsbSerialControl implements DriverInterface<String, UsbManager> {

    // Application context, is global.
    @SuppressLint("StaticFieldLeak")
    private final Application mContext = AppUtil.getInstance().getApp();
    //日志标签
    private static final String LOG_TAG = UsbSerialControl.class.getSimpleName();
    // ID
    private final int UNIQUE_ID = 0x04;
    //广播名称
    public static final String ACTION_BROADCAST = "com.rrg.devices.usb_attach_uart";
    //权限请求广播!
    private static final String ACTION_PERMISSION = "com.rrg.devices.usb_permission_uart";
    //设备名称!
    public static final String NAME_DRIVER_USB_UART = "OTGToUartSerial(OTG转串口)";
    //串口对象
    private static UsbSerialDevice mPort = null;
    //单例模式
    private static UsbSerialControl mThiz = null;
    //回调接口
    private DevCallback<String> mCallback = null;
    //广播接收，由于是单例，因此实际上广播接收也可以设置为单例!
    private BroadcastReceiver usbReceiver;
    //注册状态
    private boolean isRegister = false;
    //轮询队列
    private final Queue<Byte> recvBufQueue = new LinkedList<>();
    //广播过滤!
    private IntentFilter filter = new IntentFilter();

    /*私有化构造方法，懒汉单例模式*/
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
                        if (initUsbSerial(context)) {
                            //初始化成功则回调串口设备加入方法
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
                        if (mCallback != null) {
                            if (initUsbSerial(context)) {
                                //初始化成功则回调串口设备加入方法
                                mCallback.onAttach(NAME_DRIVER_USB_UART);
                            } else {
                                //不成则打印到LOG
                                //Log.e(LOG_TAG, "no usb permission!");
                            }
                        }
                    }

                    //在设备移除时应当释放USB设备
                    if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                        //判断并且释放USB串口
                        if (mThiz != null) {
                            try {
                                mThiz.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        //回调设备移除接口
                        if (mPort != null)
                            if (mCallback != null)
                                mCallback.onDetach(NAME_DRIVER_USB_UART);
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

    //串口备初始化函数
    private boolean initUsbSerial(Context context) {
        //得到Usb管理器
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
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
            PendingIntent intent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_PERMISSION), 0);
            //Log.d(LOG_TAG, "尝试获得USB权限!");
            usbManager.requestPermission(usbDevice, intent);
            //当没有权限的时候应当直接返回
            return false;
        }
        //一切正常返回true!
        return connect1("dxl");
    }

    @Override
    public int write(byte[] sendMsg, int offset, int length, int timeout) throws IOException {
        //TODO 注释防止外泄
        if (mPort == null) {
            //Log.e(LOG_TAG, "port is null");
            return -1;
        }
        //构建一个可用字节的缓冲区
        byte[] tmpBuf = new byte[length - offset];
        //将可用字节灌装到定义的缓冲区
        System.arraycopy(sendMsg, offset, tmpBuf, 0, tmpBuf.length);
        //在同步块中进行提交操作!
        mPort.write(tmpBuf);
       /*
        //Log.d(LOG_TAG, "发送的字节: " + HexUtil.toHexString(tmpBuf, 0, length - offset));*/
        /*
         * TODO CDC驱动类下PM3的消息发送和接收!
         * */
        //Log.d(LOG_TAG, "write: " + new String(sendMsg));
        return length - offset;
    }

    @Override
    public int read(byte[] recvMsg, int offset, int length, int timeout) throws IOException {
        if (mPort == null) {
            //Log.e(LOG_TAG, "port is null");
            return -1;
        }
        if (timeout == 0) {
            //超时为0可能不稳定
            //Log.d(LOG_TAG, "超时值为0，可能不稳定，自动优化!");
            timeout = 1000;
        }
        boolean isRerequest = false;
        //超时操作
        long startTime = System.currentTimeMillis();
        //堵塞函数，直到接收到完整的数据包或者超时
        //此处判断的是轮询队列中的元素个数
        int oldSize = recvBufQueue.size();
        //是否大于等于要接收到的数据长度，如果符合
        //则说明队列中的数据接收已经到了差不多完整的地步
        //Log.d(LOG_TAG, "超时值: " + timeout);
        while (!(oldSize >= (length - offset))) {
            int newSize = recvBufQueue.size();
            if (newSize > oldSize) {
                //有新的数据，延迟一下!
                //Log.d(LOG_TAG, "有数据进入,延迟500ms!!");
                //延长时间!
                timeout += 500;
                //更新队列计数值!
                oldSize = newSize;
            }
            if (System.currentTimeMillis() - startTime > timeout) {
                //Log.d(LOG_TAG, "超时,二次维稳启动，超时值自动增加500ms进行重新请求...");
                if (!isRerequest) {
                    timeout += 500;
                    isRerequest = true;
                } else {
                    //Log.d(LOG_TAG, "超时...");
                    return -1;
                }
                /*Log.d(LOG_TAG, "超时...");
                return -1;*/
            }
            //Log.d(LOG_TAG, "等待超时...");
        }
        //Log.d(LOG_TAG, "数据缓冲区内长度正常，开始拷贝...");
        int len = 0;
        //线程锁，实现队列操作保护
        synchronized (recvBufQueue) {
            //从轮询缓冲队列中取出对应长度的数据
            for (int i = offset; i < length; ++i) {
                //判断轮询缓冲区的元素是否可用
                if (recvBufQueue.peek() != null) {
                    Byte b = recvBufQueue.poll();
                    if (b != null) {
                        recvMsg[i] = b;
                        ++len;
                    }
                }
            }
        }
        //TODO 返回的是当前读取到的缓冲区的数据的长度(实际长度)!
        return len;
    }

    @Override
    public void flush() throws IOException {
        //don't support flush
    }

    @Override
    public void close() throws IOException {
        if (mPort == null) {
            //Log.e(LOG_TAG, "port is null");
            return;
        }
        mPort.close();

    }

    @Override
    public void register(Context context, DevCallback<String> callback) {
        mCallback = callback;
        register1();
    }

    private void register1() {
        if (!isRegister) {
            unRegister();
            try {
                mContext.registerReceiver(usbReceiver, filter);
                isRegister = true;
            } catch (Exception ignored) {
            }
        }
    }

    private void unRegister() {
        if (isRegister) {
            try {
                mContext.unregisterReceiver(usbReceiver);
                isRegister = false;
            } catch (Exception ignored) {
            }
        }
    }

    private boolean connect1(String addr) {
        if (mPort != null) {
            mPort.close();
            mPort = null;
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
        if (mPort.open()) {
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
            //新数据回调
            mPort.read(new UsbSerialInterface.UsbReadCallback() {
                @Override
                public void onReceivedData(byte[] bytes) {
                    //进行加锁，提高数据吞吐稳定性
                    for (byte b : bytes) {
                        synchronized (recvBufQueue) {
                            recvBufQueue.add(b);
                        }
                    }
                    // Log.d(LOG_TAG, "接收到的数据: " + HexUtil.toHexString(bytes));
                }
            });
            Log.d(LOG_TAG, "Usb链接成功，通信创建成功!!");
            return true;
        }
        return false;
    }

    @Override
    public boolean connect(String t) {
        return connect1(t);
    }

    @Override
    public UsbManager getAdapter() {
        return (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
    }

    @Override
    public String getDevice() {
        return mPort != null ? mPort.getClass().getSimpleName() : null;
    }

    @Override
    public void disconect() {
        //TODO 暂时不做处理
    }

    @Override
    public int getUniqueId() {
        return UNIQUE_ID;
    }

    @Override
    public void unregister(Context context) {
        //广播解注册
        unRegister();
    }
}
