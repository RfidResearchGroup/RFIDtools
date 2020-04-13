package cn.rrg.rdv.models;

import cn.dxl.com.DeviceChecker;
import cn.rrg.devices.Proxmark3RRGRdv4;

public class Proxmark3Rdv4UsbModel extends AbsUsb2UartModel {
    @Override
    public DeviceChecker getDeviceInitImpl() {
        return new Proxmark3RRGRdv4(mDI);
    }
}
