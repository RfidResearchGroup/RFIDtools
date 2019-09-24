package cn.rrg.rdv.presenter;

import cn.rrg.rdv.view.TagInformationsView;

public abstract class AbsTagInformationsPresenter
        extends BasePresenter<TagInformationsView<CharSequence>> {

    public abstract void show();
}
