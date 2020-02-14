package cn.rrg.rdv.callback;

public interface BaseCallback {

    interface ErrorCallback<T> {
        void onError(T e);
    }
}
