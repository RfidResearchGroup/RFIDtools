package cn.rrg.rdv.models;

import cn.rrg.com.Device;
import cn.rrg.devices.PN53X;

public class PN532Usb2UartModel extends AbsUsb2UartModel {
    @Override
    public Device getDeviceInitImpl() {
        return new PN53X(PN53X.NAME.PN532);
    }
}
