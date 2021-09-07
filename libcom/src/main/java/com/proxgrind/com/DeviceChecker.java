package com.proxgrind.com;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Abstract device check base class, can judge whether the device and communication are normal!
 *
 * @author DXL
 * @version 1.1
 */
public abstract class DeviceChecker implements Serializable, Closeable {
    protected Communication communication;

    public DeviceChecker(Communication communication) {
        this.communication = communication;
    }

    /**
     * Test a device is can work!
     *
     * @return if can work return true, if no, return false
     */
    public final boolean check() throws IOException {
        if (communication == null) {
            throw new IOException("The communication must be nonnull.");
        }
        InputStream is = communication.getInput();
        OutputStream os = communication.getOutput();
        if (is == null || os == null) {
            throw new IOException("The inputStream and outputStream must be nonnull.");
        }
        // Auto init communication to mapper.
        LocalComBridgeAdapter.getInstance().setInputStream(is).setOutputStream(os)
                .startServer(LocalComBridgeAdapter.NAMESPACE_DEFAULT);
        if (!checkDevice()) {
            // if check failed, we must to close client!
            close();
            // and close client communication!
            LocalComBridgeAdapter.getInstance().stopClient();
            return false;
        }
        return true;
    }

    protected abstract boolean checkDevice() throws IOException;

    /**
     * Close the device(Should no close communication. only close device!)
     */
    public abstract void close() throws IOException;
}
