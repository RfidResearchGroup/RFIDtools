package cn.rrg.rdv.javabean;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public class DeviceInfoBean {
    @NonNull
    public String name;
    @DrawableRes
    public int icon;

    public DeviceInfoBean(@NonNull String name, int icon) {
        this.name = name;
        this.icon = icon;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void onClick() {
    }
}