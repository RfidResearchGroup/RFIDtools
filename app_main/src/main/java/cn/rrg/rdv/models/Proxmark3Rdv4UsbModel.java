package cn.rrg.rdv.models;

import com.iobridges.com.DeviceChecker;

import java.io.IOException;

public class Proxmark3Rdv4UsbModel extends AbsUsb2UartModel {
    @Override
    public DeviceChecker getDeviceInitImpl() {
        return new DeviceChecker(mDI) {
            @Override
            protected boolean checkDevice() throws IOException {
                return true;
            }

            @Override
            public void close() throws IOException {

            }
        };
    }
}
