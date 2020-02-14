package cn.rrg.rdv.callback;

public interface ActionCallback<S, F> {
    void onSuccess(S s);

    void onFail(F f);
}
