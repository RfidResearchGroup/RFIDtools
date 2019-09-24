package cn.rrg.rdv.presenter;

import cn.rrg.rdv.callback.ReaderCallback;
import cn.rrg.rdv.javabean.M1Bean;
import cn.rrg.rdv.models.AbsTagReadModel;
import cn.rrg.rdv.view.TagReadView;

public abstract class AbsTagReadPresenter
        extends BasePresenter<TagReadView> {

    ReaderCallback<M1Bean[], String> callback = new ReaderCallback<M1Bean[], String>() {
        @Override
        public void onSuccess(M1Bean[] m1Beans) {
            if (isViewAttach()) {
                view.onReadFinish(m1Beans);
            }
        }

        @Override
        public void onTagAbnormal() {
            if (isViewAttach()) {
                view.onTagAbnormal();
            }
        }
    };

    private AbsTagReadModel tagReadModel;

    public void readNormallAll() {
        if (isViewAttach()) {
            tagReadModel = getTagReadModel();
            tagReadModel.readSectors(view.getKeyBeanForAll(), callback);
        }
    }

    public void readNormallOne() {
        if (isViewAttach()) {
            tagReadModel = getTagReadModel();
            tagReadModel.readSectors(view.getKeyBeanForOne(), callback);
        }
    }

    public void readSpecialAll() {
        if (isViewAttach()) {
            tagReadModel = getTagReadModel();
            tagReadModel.readSpecialTag(callback);
        }

    }

    public void readSpecialOne() {
        if (isViewAttach()) {
            int[] sectors = view.getReadeSectorSelected();
            if (sectors != null && sectors.length > 0) {
                tagReadModel = getTagReadModel();
                tagReadModel.readSpecialTag(sectors[0], callback);
            }
        }
    }

    protected abstract AbsTagReadModel getTagReadModel();
}