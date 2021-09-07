package com.rfidresearchgroup.models;

import com.rfidresearchgroup.mifare.MifareAdapter;
import com.rfidresearchgroup.mifare.StdMifareImpl;

public class StandardTagKeysCheckModel extends AbsTagKeysCheckModel {

    public StandardTagKeysCheckModel(KeyFilesCallback callback) {
        super(callback);
    }

    @Override
    public MifareAdapter getTag() {
        return StdMifareImpl.getInstance();
    }
}
