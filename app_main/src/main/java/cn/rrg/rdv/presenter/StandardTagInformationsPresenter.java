package cn.rrg.rdv.presenter;

import cn.rrg.rdv.callback.TagInformationsCallback;
import cn.rrg.rdv.models.StdNfcInformationsModel;

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
