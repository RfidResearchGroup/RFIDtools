package cn.rrg.devices;

import java.io.IOException;

import cn.dxl.com.Communication;
import cn.dxl.com.DeviceChecker;

public class EmptyDeivce extends DeviceChecker {

    public EmptyDeivce(Communication communication) {
        super(communication);
    }

    @Override
    protected boolean checkDevice() throws IOException {
        return true;
    }


    @Override
    public void close() throws IOException {
    }
}
