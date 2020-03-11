package cn.rrg.natives;

import cn.dxl.mifare.TagLeaksAdapter;

public class PN53XTagLeaksAdapter implements TagLeaksAdapter {
    /*
     * 加载动态库
     * */
    static {
        System.loadLibrary("pn53x_prng_check");
    }

    @Override
    public native boolean isDarksideSupported();

    @Override
    public native boolean isNestedSupported();

    @Override
    public native boolean isHardnestedSupported();
}
