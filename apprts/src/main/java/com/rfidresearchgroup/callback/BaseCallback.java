package com.rfidresearchgroup.callback;

public interface BaseCallback {

    interface ErrorCallback<T> {
        void onError(T e);
    }
}
