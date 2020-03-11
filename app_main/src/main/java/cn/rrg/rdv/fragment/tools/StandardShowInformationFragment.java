package cn.rrg.rdv.fragment.tools;

import cn.rrg.rdv.presenter.AbsTagInformationsPresenter;
import cn.rrg.rdv.presenter.StandardTagInformationsPresenter;

public class StandardShowInformationFragment extends AbsShowInformationFragment {
    @Override
    protected AbsTagInformationsPresenter getPresenter() {
        //使用的是标准的设备获得到的标签信息!
        return new StandardTagInformationsPresenter();
    }
}
