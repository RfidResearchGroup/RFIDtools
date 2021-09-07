package com.rfidresearchgroup.javabean;

public class FileBean {
    private boolean isFile;
    private boolean showIcon = true;
    private String name;
    private String info;
    private String path;

    public FileBean() {
    }

    public FileBean(boolean isFile, String name, String path, String info, boolean showIcon) {
        this.isFile = isFile;
        this.name = name;
        this.info = info;
        this.path = path;
        this.showIcon = showIcon;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isShowIcon() {
        return showIcon;
    }

    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
    }

    @Override
    public String toString() {
        return "FileBean{" +
                "isFile=" + isFile +
                ", name='" + name + '\'' +
                ", info='" + info + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

    // 回调!
    public void onClick() {
    }

    public boolean onLongClick() {
        return false;
    }
}
