package cn.rrg.rdv.presenter;

import cn.rrg.rdv.models.AbsTagReadModel;
import cn.rrg.rdv.models.Proxmark3Rdv4RRGTagReadModel;

public class Proxmark3Rdv4RRGTagReadPresenter extends AbsTagReadPresenter {
    @Override
    protected AbsTagReadModel getTagReadModel() {
        return new Proxmark3Rdv4RRGTagReadModel();
    }
}
