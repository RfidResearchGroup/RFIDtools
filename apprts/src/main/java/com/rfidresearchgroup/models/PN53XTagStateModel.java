package com.rfidresearchgroup.models;

import com.rfidresearchgroup.natives.SpclMf;

public class PN53XTagStateModel extends AbsTagStateModel {
    private SpclMf spclMf = SpclMf.get();

    @Override
    protected boolean checkTagState() {
        if (spclMf.scanning()) {
            return spclMf.connect();
        } else {
            return false;
        }
    }

    @Override
    protected boolean checkTagMagic() {
        if (!spclMf.scanning()) return false;
        if (spclMf.connect()) return spclMf.unlock();
        return false;
    }
}
