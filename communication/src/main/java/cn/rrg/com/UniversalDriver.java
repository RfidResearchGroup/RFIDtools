package cn.rrg.com;

import java.io.IOException;

import cn.dxl.common.posixio.Communication;

/**
 * This is a universal driver
 * base is a buffer
 * all data from external
 * and new data always call listener impl from dev
 *
 * @author DXL
 * @version 1.0
 */
public class UniversalDriver implements Communication {

    private OnWriteDataListener onWriteDataListener;


    @Override
    public int write(byte[] sendMsg, int offset, int length, int timeout) throws IOException {
        return onWriteDataListener != null ?
                onWriteDataListener.onWriteData(sendMsg, offset, length, timeout)
                : -1;
    }

    @Override
    public int read(byte[] recvMsg, int offset, int length, int timeout) throws IOException {
        return 0;
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }

    public OnWriteDataListener getOnWriteDataListener() {
        return onWriteDataListener;
    }

    public void setOnWriteDataListener(OnWriteDataListener onWriteDataListener) {
        this.onWriteDataListener = onWriteDataListener;
    }

    /**
     * put some bytes to the buffer, which will be used by the read function
     *
     * @param data some data, only read function can use.
     */
    public void putData(byte[] data) {

    }

    public abstract static class OnWriteDataListener {
        /**
         * some data from {@link #write(byte[], int, int, int)}
         *
         * @param data the new data from write fun
         * @return your process ret
         */
        public int onWriteData(byte[] data, int offset, int length, int timeout) {
            return data.length;
        }
    }
}
