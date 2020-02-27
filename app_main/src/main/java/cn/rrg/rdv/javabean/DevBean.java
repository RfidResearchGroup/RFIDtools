package cn.rrg.rdv.javabean;

import java.io.Serializable;

/**
 * Created by DXL on 2017/11/14.
 */
public class DevBean implements Serializable {

    private String devName;
    private String macAddress;

    public DevBean(String name, String addr) {
        this.devName = name;
        this.macAddress = addr;
    }

    public String getDevName() {
        return devName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    @Override
    public String toString() {
        return "DevBean{" +
                "devName='" + devName + '\'' +
                ", macAddress='" + macAddress + '\'' +
                '}';
    }
}
