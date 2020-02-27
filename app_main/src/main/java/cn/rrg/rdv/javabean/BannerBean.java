package cn.rrg.rdv.javabean;

public class BannerBean {
    private BannerBean[] subs;
    private String imgRes;

    public BannerBean() {
    }

    public BannerBean(BannerBean[] subs, String imgRes) {
        this.subs = subs;
        this.imgRes = imgRes;
    }

    public BannerBean(BannerBean[] subs) {
        this.subs = subs;
    }

    public BannerBean(String imgRes) {
        this.imgRes = imgRes;
    }

    public BannerBean[] getSubs() {
        return subs;
    }

    public void setSubs(BannerBean[] subs) {
        this.subs = subs;
    }

    public String getImgRes() {
        return imgRes;
    }

    public void setImgRes(String imgRes) {
        this.imgRes = imgRes;
    }

    public void onClick() {
    }
}
