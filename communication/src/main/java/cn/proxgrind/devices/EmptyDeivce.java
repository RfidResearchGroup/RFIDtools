package cn.proxgrind.devices;

import com.iobridges.com.Communication;
import com.iobridges.com.DeviceChecker;

import java.io.IOException;

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
