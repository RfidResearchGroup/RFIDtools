package cn.rrg.devices;

import java.io.IOException;

import cn.rrg.com.Device;

public class EmptyDeivce implements Device {
    @Override
    public boolean working() throws IOException {
        return true;
    }

    @Override
    public boolean close() throws IOException {
        return true;
    }
}
