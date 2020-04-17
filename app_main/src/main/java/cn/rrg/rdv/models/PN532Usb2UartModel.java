package cn.rrg.rdv.models;

import com.iobridges.com.DeviceChecker;

import cn.rrg.devices.PN53X;

public class PN532Usb2UartModel extends AbsUsb2UartModel {
    @Override
    public DeviceChecker getDeviceInitImpl() {
        return new PN53X(PN53X.NAME.PN532, mDI);
    }
}
