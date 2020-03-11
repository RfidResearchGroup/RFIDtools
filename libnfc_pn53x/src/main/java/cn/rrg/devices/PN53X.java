package cn.rrg.devices;

import java.io.IOException;

import cn.rrg.com.Device;

public class PN53X implements Device {

    public interface NAME {
        String ACR122 = "ACR122";
        String ASK_LOGO = "ASK_LOGO";
        String PN532 = "PN532";
        String NXP_PN533 = "NXP_PN533";
        String NXP_PN531 = "NXP_PN531";
        String SONY_PN531 = "SONY_PN531";
        String SCM_SCL3711 = "SCM_SCL3711";
        String SCM_SCL3712 = "SCM_SCL3712";
        String SONY_RCS360 = "SONY_RCS360";
        String UNKNOWN = "UNKNOWN";
    }

    private String name;

    public PN53X(String name) {
        this.name = name;
    }

    static {
        System.loadLibrary("pn53x");
    }

    @Override
    public boolean working() throws IOException {
        return testPN53x(name);
    }

    @Override
    public boolean close() throws IOException {
        return closePN53x();
    }

    //关闭设备函数!
    private native boolean closePN53x() throws IOException;

    //设备初始化测试函数
    private native boolean testPN53x(String name) throws IOException;
}