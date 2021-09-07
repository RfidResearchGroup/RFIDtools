package com.rfidresearchgroup.javabean;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public class DeviceInfoBean {
    @NonNull
    private String name;
    @DrawableRes
    private int icon;
    private boolean enable = true;

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

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void onClick() {
    }
}