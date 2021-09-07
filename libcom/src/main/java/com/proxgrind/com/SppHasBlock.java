package com.proxgrind.com;

import android.annotation.SuppressLint;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by DXL on 2017/8/21.
 */
public class SppHasBlock extends AbsBluetoothSpp {
    private static final Object LOCK = new Object();
    @SuppressLint("StaticFieldLeak")
    private static SppHasBlock sppCacheTransfer = null;

    //单例模式因此需要将此构造方法私有化
    private SppHasBlock() { /*can't using this constructor*/ }

    public static SppHasBlock get() {
        synchronized (LOCK) {
            if (sppCacheTransfer == null) {
                //在同步块里建立实例
                sppCacheTransfer = new SppHasBlock();
            }
        }
        return sppCacheTransfer;
    }

    @Override
    public OutputStream getOutput() {
        return outputStream;
    }

    @Override
    public InputStream getInput() {
        // return new InternalIn();
        return inputStream;
    }

    /*class InternalIn extends InputStream {

        @Override
        public int read() throws IOException {
            if (inputStream == null) return -1;
            return inputStream.read();
        }

        @Override
        public int read(@NonNull byte[] b, int off, int len) throws IOException {
            if (inputStream == null) return -1;
            int lenRecv = inputStream.available();
            if (lenRecv <= 0) return -1;
            Log.d("***", "长度：" + lenRecv);
            return inputStream.read(b, off, lenRecv);
            //Log.d("****", "接收: " + HexUtil.toHexString(recvMsg, offset, length));
        }
    }*/
}