package cn.rrg.com;

import android.content.Context;

public class ContextHandle implements ContextCallback {
    private ContextCallback callback;

    public void setContextCallback(ContextCallback callback) {
        this.callback = callback;
    }

    @Override
    public Context getContext() {
        return callback.getContext();
    }
}
