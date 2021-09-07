package com.rfidresearchgroup.models;

import com.rfidresearchgroup.mifare.MifareAdapter;
import com.rfidresearchgroup.natives.SpclMf;

public class PN53XTagReadModel extends AbsTagReadModel {
    @Override
    protected MifareAdapter getTag() {
        return SpclMf.get();
    }
}
