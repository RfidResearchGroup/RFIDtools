package cn.rrg.rdv.models;

import cn.rrg.rdv.callback.TagStateCallback;

public abstract class AbsTagStateModel {
    public void check(TagStateCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (checkTagState()) {
                    if (checkTagMagic())
                        callback.onTagSpecial();    //标签特殊
                    else
                        callback.onTagOrdinary();   //标签普通
                } else
                    callback.onTagAbnormal();   //标签异常
            }
        }).start();
    }

    protected abstract boolean checkTagState();

    protected abstract boolean checkTagMagic();
}
