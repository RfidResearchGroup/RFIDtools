package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.models.AbsTagWriteModel;
import com.rfidresearchgroup.models.PN53XTagWriteModel;

public class PN53XTagWritePresenter extends AbsTagWritePresenter {
    @Override
    protected AbsTagWriteModel getWriteModel() {
        return new PN53XTagWriteModel();
    }
}
