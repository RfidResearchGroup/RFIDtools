package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.models.AbsTagWriteModel;
import com.rfidresearchgroup.models.StandardTagWriteModel;

public class StandardTagWritePresenter extends AbsTagWritePresenter {
    @Override
    protected AbsTagWriteModel getWriteModel() {
        return new StandardTagWriteModel();
    }
}
