package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.view.TagInformationsView;

public abstract class AbsTagInformationsPresenter
        extends BasePresenter<TagInformationsView<CharSequence>> {

    public abstract void show();
}
