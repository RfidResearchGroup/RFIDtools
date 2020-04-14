package cn.dxl.com;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;

/**
 * Abstract device check base class, can judge whether the device and communication are normal!
 *
 * @author DXL
 * @version 1.0
 */
public abstract class DeviceChecker implements Serializable, Closeable {
    protected Communication communication;

    public DeviceChecker(Communication communication) {
        this.communication = communication;
        initCom();
    }

    private void initCom(){
        // Auto init communication to mapper.
        ComBridgeAdapter.getInstance()
                .setInputStream(communication.getInput())
                .setOutputStream(communication.getOutput());
    }

    /**
     * Test a device is can work!
     *
     * @return if can work return true, if no, return false
     */
    public boolean check() throws IOException {
        initCom();
        return false;
    }

    /**
     * Close the device(Should no close communication. only close device!)
     */
    public abstract void close() throws IOException;
}
