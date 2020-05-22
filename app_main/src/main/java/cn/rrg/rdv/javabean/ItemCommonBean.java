package cn.rrg.rdv.javabean;

import android.view.View;

public class ItemCommonBean extends TitleBean {
    private String subTitle = "";
    private int iconResID;

    public ItemCommonBean(String title) {
        super(title);
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public int getIconResID() {
        return iconResID;
    }

    public void setIconResID(int iconResID) {
        this.iconResID = iconResID;
    }

    public void onClick(View view, int pos) {
    }

    public void onChange(View view, int pos, boolean checked) {

    }
}
