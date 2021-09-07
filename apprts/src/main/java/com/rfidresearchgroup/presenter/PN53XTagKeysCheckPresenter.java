package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.models.AbsTagKeysCheckModel;

public class PN53XTagKeysCheckPresenter
        extends TagKeysCheckPresenterImpl {
    @Override
    public AbsTagKeysCheckModel getTagKeysCheckModel() {

        return new PN53XTagKeysCheckModel(this);
    }
}
