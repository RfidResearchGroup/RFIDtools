package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.models.AbsTagKeysCheckModel;

import com.rfidresearchgroup.mifare.MifareAdapter;
import com.rfidresearchgroup.natives.SpclMf;

public class PN53XTagKeysCheckModel extends AbsTagKeysCheckModel {
    public PN53XTagKeysCheckModel(KeyFilesCallback callback) {
        super(callback);
    }

    @Override
    public MifareAdapter getTag() {
        return SpclMf.get();
    }
}
