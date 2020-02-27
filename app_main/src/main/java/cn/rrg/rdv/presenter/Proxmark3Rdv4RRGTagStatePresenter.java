package cn.rrg.rdv.presenter;

import cn.rrg.rdv.models.AbsTagStateModel;
import cn.rrg.rdv.models.Proxmark3Rdv4RRGTagStateModel;

public class Proxmark3Rdv4RRGTagStatePresenter extends AbsTagStatePresenter {
    @Override
    protected AbsTagStateModel getModel() {
        return new Proxmark3Rdv4RRGTagStateModel();
    }
}
