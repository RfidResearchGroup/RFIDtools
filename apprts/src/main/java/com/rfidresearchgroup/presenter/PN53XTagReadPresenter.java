package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.models.AbsTagReadModel;
import com.rfidresearchgroup.models.PN53XTagReadModel;

public class PN53XTagReadPresenter extends AbsTagReadPresenter {
    @Override
    protected AbsTagReadModel getTagReadModel() {
        return new PN53XTagReadModel();
    }
}
