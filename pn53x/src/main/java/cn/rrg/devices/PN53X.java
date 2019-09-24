package cn.rrg.devices;

import java.io.IOException;

import cn.rrg.com.Device;

public class PN53X implements Device {

    static {
        System.loadLibrary("pn53x");
    }

    @Override
    public boolean working() throws IOException {
        return testPN53x();
    }

    @Override
    public boolean close() throws IOException {
        return closePN53x();
    }

    //关闭设备函数!
    private native boolean closePN53x() throws IOException;

    //设备初始化测试函数
    private native boolean testPN53x() throws IOException;
}