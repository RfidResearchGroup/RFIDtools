package cn.rrg.devices;

import java.io.IOException;

import cn.dxl.com.Communication;
import cn.dxl.com.DeviceChecker;

public class PN53X extends DeviceChecker {

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

    public PN53X(String name, Communication communication) {
        super(communication);
        this.name = name;
    }

    static {
        System.loadLibrary("pn53x");
    }

    @Override
    public boolean check() throws IOException {
        super.check();
        return testPN53x(name);
    }

    @Override
    public void close() throws IOException {
        closePN53x();
    }

    //关闭设备函数!
    private native boolean closePN53x() throws IOException;

    //设备初始化测试函数
    private native boolean testPN53x(String name) throws IOException;
}