package cn.dxl.bulkio;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

public class BulkInputStream extends InputStream {
    private int timeout;
    private UsbDeviceConnection connection;
    private UsbEndpoint endpoint;

    public BulkInputStream(UsbDeviceConnection connection, UsbEndpoint endpoint) {
        this.connection = connection;
        this.endpoint = endpoint;
    }

    @Override
    public int read() throws IOException {
        byte[] bs = new byte[1];
        int len = connection.bulkTransfer(endpoint, bs, 1, timeout);
        if (len > 0) {
            return bs[0];
        }
        if (len < 0) {
            return -1;
        }
        return 0;
    }

    @Override
    public int read(@NonNull byte[] b) throws IOException {
        return connection.bulkTransfer(endpoint, b, b.length, timeout);
    }

    @Override
    public int read(@NonNull byte[] b, int off, int len) throws IOException {
        return connection.bulkTransfer(endpoint, b, off, len, timeout);
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}
