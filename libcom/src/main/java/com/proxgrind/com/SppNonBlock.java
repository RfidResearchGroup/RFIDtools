package com.proxgrind.com;

import android.annotation.SuppressLint;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by DXL on 2017/8/21.
 */
public class SppNonBlock extends AbsBluetoothSpp {
    private static final Object LOCK = new Object();
    @SuppressLint("StaticFieldLeak")
    private static SppNonBlock bluetoothSpp = null;

    //单例模式因此需要将此构造方法私有化
    private SppNonBlock() { /*can't using this constructor*/ }

    public static SppNonBlock get() {
        synchronized (LOCK) {
            if (bluetoothSpp == null) {
                //在同步块里建立实例
                bluetoothSpp = new SppNonBlock();
            }
        }
        return bluetoothSpp;
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