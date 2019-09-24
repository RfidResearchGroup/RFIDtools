package cn.rrg.rdv.presenter;

import cn.rrg.rdv.callback.TagInformationsCallback;
import cn.rrg.rdv.models.PN53XInformationsModel;

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
