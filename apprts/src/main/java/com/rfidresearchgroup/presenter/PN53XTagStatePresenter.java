package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.models.AbsTagStateModel;
import com.rfidresearchgroup.models.PN53XTagStateModel;

public class PN53XTagStatePresenter extends AbsTagStatePresenter {

    @Override
    protected AbsTagStateModel getModel() {
        return new PN53XTagStateModel();
    }
}
