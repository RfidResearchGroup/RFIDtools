package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.callback.TagStateCallback;
import com.rfidresearchgroup.models.AbsTagStateModel;
import com.rfidresearchgroup.view.TagStateView;

/**
 * 抽象的标签状态中介!
 */
public abstract class AbsTagStatePresenter
        extends BasePresenter<TagStateView> {

    private AbsTagStateModel model;

    {
        // 在当前对象实例化时顺便初始化model!
        model = getModel();
    }

    public void check() {
        if (isViewAttach()) {
            // check tag status..
            model.check(new TagStateCallback() {
                @Override
                public void onTagAbnormal() {
                    view.onTagAbnormal();
                }

                @Override
                public void onTagOrdinary() {
                    view.onTagOrdinary();
                }

                @Override
                public void onTagSpecial() {
                    view.onTagSpecial();
                }
            });
        }
    }

    protected abstract AbsTagStateModel getModel();
}
