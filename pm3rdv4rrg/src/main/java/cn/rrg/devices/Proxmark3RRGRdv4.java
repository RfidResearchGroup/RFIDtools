package cn.rrg.devices;

import com.iobridges.com.Communication;
import com.iobridges.com.DeviceChecker;

import java.io.IOException;

import cn.dxl.common.util.LogUtils;

public class Proxmark3RRGRdv4 extends DeviceChecker {

    static {
        System.loadLibrary("pm3rrg_rdv4");
    }

    public Proxmark3RRGRdv4(Communication communication) {
        super(communication);
    }

    @Override
    public boolean checkDevice() throws IOException {
        LogUtils.d("测试顺序");
        return testPm3();
    }

    @Override
    public void close() throws IOException {
        closePm3();
    }

    private native boolean testPm3() throws IOException;

    private native void closePm3();
}
