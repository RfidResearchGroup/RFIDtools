package cn.rrg.com;

import android.bluetooth.BluetoothAdapter;

import java.io.IOException;

/**
 * Created by DXL on 2017/8/21.
 */
public class SppHasBlock extends AbsBluetoothSpp {
    private static final Object LOCK = new Object();
    private static final int UNIQUE_ID = 0x02;
    private static SppHasBlock sppCacheTransfer = null;
    private static final int TIMEOUT = 5000;

    //单例模式因此需要将此构造方法私有化
    private SppHasBlock() { /*can't using this constructor*/ }

    public static SppHasBlock get() {
        synchronized (LOCK) {
            if (sppCacheTransfer == null) {
                //得到蓝牙适配器
                btAdapter = BluetoothAdapter.getDefaultAdapter();
                //在同步块里建立实例
                sppCacheTransfer = new SppHasBlock();
            }
        }
        return sppCacheTransfer;
    }

    @Override
    public int write(byte[] sendMsg, int offset, int length, int timeout) throws IOException {
        setThreadPriority();
        if (outputStream == null) return -1;
        outputStream.write(sendMsg, offset, length - offset);
        outputStream.flush();
        return length;
    }

    @Override
    public int read(byte[] recvMsg, int offset, int length, int timeout) throws IOException {
        setThreadPriority();
        if (inputStream == null) return -1;
        long start = System.currentTimeMillis();
        while (inputStream.available() < (length - offset)) {
            if ((System.currentTimeMillis() - start) > TIMEOUT) {
                // 已经超时!
                return -1;
            }
        }
        for (int i = offset; i < length; i++) {
            recvMsg[i] = (byte) inputStream.read();
        }
        //Log.d("****", "接收: " + HexUtil.toHexString(recvMsg, offset, length));
        return length;
    }

    @Override
    public int getUniqueId() {
        return UNIQUE_ID;
    }

    private void setThreadPriority() {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    }
}