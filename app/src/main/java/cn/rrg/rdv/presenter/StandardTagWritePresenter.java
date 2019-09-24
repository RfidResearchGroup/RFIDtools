package cn.rrg.rdv.presenter;

import cn.rrg.rdv.models.AbsTagWriteModel;
import cn.rrg.rdv.models.StandardTagWriteModel;

public class StandardTagWritePresenter extends AbsTagWritePresenter {
    @Override
    protected AbsTagWriteModel getWriteModel() {
        return new StandardTagWriteModel();
    }
}
