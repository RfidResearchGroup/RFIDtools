package cn.rrg.com;

import android.bluetooth.BluetoothAdapter;

import java.io.IOException;

/**
 * Created by DXL on 2017/8/21.
 */
public class SppNonBlock extends AbsBluetoothSpp {
    private static final Object LOCK = new Object();
    private static final int UNIQUE_ID = 0x01;
    private static SppNonBlock bluetoothSpp = null;

    //单例模式因此需要将此构造方法私有化
    private SppNonBlock() { /*can't using this constructor*/ }

    public static SppNonBlock get() {
        synchronized (LOCK) {
            if (bluetoothSpp == null) {
                //得到蓝牙适配器
                btAdapter = BluetoothAdapter.getDefaultAdapter();
                //在同步块里建立实例
                bluetoothSpp = new SppNonBlock();
            }
        }
        return bluetoothSpp;
    }

    @Override
    public int write(byte[] sendMsg, int offset, int length, int timeout) throws IOException {
        int res = 0;
        for (int i = offset; i < length; i++) {
            /*outputStream.write(sendMsg[i]);
            outputStream.flush();*/
            res += 1;
        }
        //减去偏移值才是真正的长度!
        //return res;
        // TODO 优化速度，全部往内核写缓存再通知CPU发送，避免CPU切换影响速度
        outputStream.write(sendMsg, offset, length - offset);
        outputStream.flush();
        return res;
    }

    /**
     * 实现了SPP的接收
     *
     * @param recvMsg 接收缓冲区!
     * @param offset  偏移值
     * @param length  长度
     * @param timeout 超时值
     * @return 返回接收的实际长度
     * @throws IOException 在出现IO异常时抛出!
     */
    @Override
    public int read(byte[] recvMsg, int offset, int length, int timeout) throws IOException {
        long start = System.currentTimeMillis();
        while (inputStream.available() <= length - offset) {
            if (inputStream.available() >= length - offset) break;
            if (inputStream.available() == 0)
                if (System.currentTimeMillis() - start > timeout) return 0;
        }
        return inputStream.read(recvMsg, offset, length - offset);
    }

    @Override
    public int getUniqueId() {
        return UNIQUE_ID;
    }
}