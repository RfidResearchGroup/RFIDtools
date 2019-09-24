package cn.rrg.rdv.models;

import java.io.File;

import cn.dxl.common.util.FileUtil;
import cn.rrg.com.Device;
import cn.rrg.devices.PN53X;
import cn.rrg.rdv.callback.ConnectCallback;
import cn.rrg.rdv.util.Paths;

public class PN532Usb2UartModel extends AbsUsb2UartModel {
    @Override
    public Device getDeviceInitImpl() {
        return new PN53X();
    }

    @Override
    public void connect(String address, ConnectCallback callback) {
        // TODO 我们先切换设备为PN532!
        FileUtil.writeString(new File(Paths.PN53X_CONF_FILE), "PN532", false);
        super.connect(address, callback);
    }
}
