package cn.dxl.bulkio;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;

public class BulkOutputStream extends OutputStream {
    private int timeout;
    private UsbDeviceConnection connection;
    private UsbEndpoint endpoint;

    public BulkOutputStream(UsbDeviceConnection connection, UsbEndpoint endpoint) {
        this.connection = connection;
        this.endpoint = endpoint;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void write(int b) throws IOException {
        connection.bulkTransfer(endpoint, new byte[]{(byte) b}, 0, 1, timeout);
    }

    @Override
    public void write(@NonNull byte[] b, int off, int len) throws IOException {
        connection.bulkTransfer(endpoint, b, off, len, timeout);
    }

    @Override
    public void write(@NonNull byte[] b) throws IOException {
        connection.bulkTransfer(endpoint, b, 0, b.length, timeout);
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}
