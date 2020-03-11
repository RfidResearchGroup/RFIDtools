package cn.rrg.rdv.presenter;

import cn.rrg.rdv.view.BaseMvpView;

public abstract class BasePresenter<V extends BaseMvpView> {

    protected String LOG_TAG = this.getClass().getSimpleName();

    //中介者持有视图层的对象
    public V view = null;

    //优化中介类 
    public void attachView(V view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public boolean isViewAttach() {
        return view != null;
    }

    public V getView() {
        return view;
    }
}
