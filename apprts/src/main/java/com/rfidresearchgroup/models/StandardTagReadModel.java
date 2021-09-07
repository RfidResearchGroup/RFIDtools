package com.rfidresearchgroup.models;

import com.rfidresearchgroup.mifare.MifareAdapter;
import com.rfidresearchgroup.mifare.StdMifareImpl;

public class StandardTagReadModel
        extends AbsTagReadModel {
    @Override
    protected MifareAdapter getTag() {
        return StdMifareImpl.getInstance();
    }
}
