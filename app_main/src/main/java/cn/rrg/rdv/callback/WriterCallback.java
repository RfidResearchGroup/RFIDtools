package cn.rrg.rdv.callback;

public interface WriterCallback extends BaseCallback {
    void onFinish();

    void onDataInvalid();

    void onTagAbnormal();
}
 