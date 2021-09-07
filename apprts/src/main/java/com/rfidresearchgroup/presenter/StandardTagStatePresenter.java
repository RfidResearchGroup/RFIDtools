package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.models.AbsTagStateModel;
import com.rfidresearchgroup.models.StandardNFCTagStateModel;

public class StandardTagStatePresenter extends AbsTagStatePresenter {

    @Override
    protected AbsTagStateModel getModel() {
        return new StandardNFCTagStateModel();
    }
}
