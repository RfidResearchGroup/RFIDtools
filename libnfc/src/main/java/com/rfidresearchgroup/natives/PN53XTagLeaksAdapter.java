package com.rfidresearchgroup.natives;

import com.rfidresearchgroup.mifare.TagLeaksAdapter;

public class PN53XTagLeaksAdapter implements TagLeaksAdapter {

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
