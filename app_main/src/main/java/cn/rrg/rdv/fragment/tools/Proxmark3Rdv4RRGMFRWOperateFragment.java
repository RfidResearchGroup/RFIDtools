package cn.rrg.rdv.fragment.tools;

import cn.rrg.rdv.presenter.AbsTagKeysCheckPresenter;
import cn.rrg.rdv.presenter.AbsTagReadPresenter;
import cn.rrg.rdv.presenter.AbsTagStatePresenter;
import cn.rrg.rdv.presenter.AbsTagWritePresenter;
import cn.rrg.rdv.presenter.Proxmark3Rdv4RRGTagKeysCheckPresenter;
import cn.rrg.rdv.presenter.Proxmark3Rdv4RRGTagReadPresenter;
import cn.rrg.rdv.presenter.Proxmark3Rdv4RRGTagStatePresenter;
import cn.rrg.rdv.presenter.Proxmark3Rdv4RRGTagWritePresenter;

public class Proxmark3Rdv4RRGMFRWOperateFragment extends AbsMfOperatesFragment {
    @Override
    protected AbsTagStatePresenter getTagStatePresenter() {
        return new Proxmark3Rdv4RRGTagStatePresenter();
    }

    @Override
    protected AbsTagKeysCheckPresenter getKeysCheckPresenter() {
        return new Proxmark3Rdv4RRGTagKeysCheckPresenter();
    }

    @Override
    protected AbsTagReadPresenter getTagReadPresenter() {
        return new Proxmark3Rdv4RRGTagReadPresenter();
    }

    @Override
    protected AbsTagWritePresenter getTagWritePresenter() {
        return new Proxmark3Rdv4RRGTagWritePresenter();
    }
}
