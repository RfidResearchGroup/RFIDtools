package com.rfidresearchgroup.models;

import com.rfidresearchgroup.mifare.MifareAdapter;
import com.rfidresearchgroup.natives.SpclMf;

public class PN53XTagWriteModel extends AbsTagWriteModel {
    @Override
    protected MifareAdapter getTag() {
        return SpclMf.get();
    }
}
