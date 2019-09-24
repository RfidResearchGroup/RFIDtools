package cn.rrg.rdv.presenter;

import cn.rrg.rdv.callback.TagInformationsCallback;
import cn.rrg.rdv.models.Proxmark3Rdv4RRGTagInformationsModel;

public class Proxmark3Rdv4RRGTagInformationsPresenter extends AbsTagInformationsPresenter {
    @Override
    public void show() {
        if (isViewAttach()) {
            new Proxmark3Rdv4RRGTagInformationsModel().collect(new TagInformationsCallback<CharSequence>() {
                @Override
                public void onInformationsShow(CharSequence charSequence) {
                    view.onInformationsShow(charSequence);
                }
            });
        }
    }
}
