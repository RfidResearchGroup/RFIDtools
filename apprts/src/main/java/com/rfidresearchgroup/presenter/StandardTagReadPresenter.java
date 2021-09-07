package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.models.AbsTagReadModel;
import com.rfidresearchgroup.models.StandardTagReadModel;

public class StandardTagReadPresenter
        extends AbsTagReadPresenter {
    @Override
    public AbsTagReadModel getTagReadModel() {
        return new StandardTagReadModel();
    }
}
