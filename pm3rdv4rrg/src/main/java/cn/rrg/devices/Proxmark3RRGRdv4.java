package cn.rrg.devices;

import java.io.IOException;

import cn.rrg.com.Device;

public class Proxmark3RRGRdv4 implements Device {
    
    static {
        System.loadLibrary("pm3rrg_rdv4");
    }

    @Override
    public boolean working() throws IOException {
        return testPm3();
    }

    @Override
    public boolean close() throws IOException {
        closePm3();
        return true;
    }

    private native boolean testPm3() throws IOException;

    private native void closePm3();
}
