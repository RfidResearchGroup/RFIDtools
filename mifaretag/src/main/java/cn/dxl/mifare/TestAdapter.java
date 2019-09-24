package cn.dxl.mifare;

import java.io.Serializable;

public interface TestAdapter extends Serializable {
    boolean isPRNGSupported();

    boolean isNestedSupported();

    boolean isHardnestedSupported();
}