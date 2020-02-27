package cn.rrg.rdv.fragment.tools;

import cn.rrg.rdv.presenter.AbsTagInformationsPresenter;
import cn.rrg.rdv.presenter.Proxmark3Rdv4RRGTagInformationsPresenter;

public class Proxmark3Rdv4RRGShowInformationFragment extends AbsShowInformationFragment {
    @Override
    protected AbsTagInformationsPresenter getPresenter() {
        return new Proxmark3Rdv4RRGTagInformationsPresenter();
    }
}
