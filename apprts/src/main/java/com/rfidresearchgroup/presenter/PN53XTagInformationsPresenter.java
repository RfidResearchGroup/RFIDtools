package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.callback.TagInformationsCallback;
import com.rfidresearchgroup.models.PN53XInformationsModel;

public class PN53XTagInformationsPresenter extends AbsTagInformationsPresenter {
    @Override
    public void show() {
        if (isViewAttach()) {
            new PN53XInformationsModel().collect(new TagInformationsCallback<CharSequence>() {
                @Override
                public void onInformationsShow(CharSequence charSequence) {
                    view.onInformationsShow(charSequence);
                }
            });
        }
    }
}
