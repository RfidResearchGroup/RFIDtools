package cn.rrg.rdv.models;

import cn.rrg.com.Device;
import cn.rrg.devices.Proxmark3RRGRdv4;

public class Proxmark3Rdv4UsbModel extends AbsUsb2UartModel {
    @Override
    public Device getDeviceInitImpl() {
        return new Proxmark3RRGRdv4();
    }
}
