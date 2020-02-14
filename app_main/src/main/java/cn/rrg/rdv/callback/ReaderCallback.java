package cn.rrg.rdv.callback;

public interface ReaderCallback<T, E> extends BaseCallback {
    //读取扇区成功后的结果回调
    void onSuccess(T t);

    void onTagAbnormal();
} 
