package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.callback.TagInformationsCallback;
import com.rfidresearchgroup.models.StdNfcInformationsModel;

public class StandardTagInformationsPresenter
        extends AbsTagInformationsPresenter {
    @Override
    public void show() {
        new StdNfcInformationsModel().collect(new TagInformationsCallback<CharSequence>() {
            @Override
            public void onInformationsShow(CharSequence s) {
                if (isViewAttach())
                    view.onInformationsShow(s);
            }
        });
    }
}
