package cn.rrg.rdv.presenter;

import cn.rrg.rdv.models.AbsTagStateModel;
import cn.rrg.rdv.models.StandardNFCTagStateModel;

public class StandardTagStatePresenter extends AbsTagStatePresenter {

    @Override
    protected AbsTagStateModel getModel() {
        return new StandardNFCTagStateModel();
    }
}
