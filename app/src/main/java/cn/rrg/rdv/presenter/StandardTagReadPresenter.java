package cn.rrg.rdv.presenter;

import cn.rrg.rdv.models.AbsTagReadModel;
import cn.rrg.rdv.models.StandardTagReadModel;

public class StandardTagReadPresenter
        extends AbsTagReadPresenter {
    @Override
    public AbsTagReadModel getTagReadModel() {
        return new StandardTagReadModel();
    }
}
