package cn.rrg.chameleon.defined;

public interface ResultCallback<S, F> {
    void onSuccess(S s);

    void onFaild(F f);
}
