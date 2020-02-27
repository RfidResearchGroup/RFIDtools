package cn.rrg.rdv.fragment.tools;

import cn.rrg.rdv.presenter.AbsTagKeysCheckPresenter;
import cn.rrg.rdv.presenter.AbsTagReadPresenter;
import cn.rrg.rdv.presenter.AbsTagStatePresenter;
import cn.rrg.rdv.presenter.AbsTagWritePresenter;
import cn.rrg.rdv.presenter.StandardTagKeysCheckPresenter;
import cn.rrg.rdv.presenter.StandardTagReadPresenter;
import cn.rrg.rdv.presenter.StandardTagStatePresenter;
import cn.rrg.rdv.presenter.StandardTagWritePresenter;

public class StandardMFRWOperateFragment
        extends AbsMfOperatesFragment {

    @Override
    protected AbsTagStatePresenter getTagStatePresenter() {
        return new StandardTagStatePresenter();
    }

    @Override
    protected AbsTagKeysCheckPresenter getKeysCheckPresenter() {
        return new StandardTagKeysCheckPresenter();
    }

    @Override
    protected AbsTagReadPresenter getTagReadPresenter() {

        return new StandardTagReadPresenter();
    }

    @Override
    protected AbsTagWritePresenter getTagWritePresenter() {
        return new StandardTagWritePresenter();
    }
}