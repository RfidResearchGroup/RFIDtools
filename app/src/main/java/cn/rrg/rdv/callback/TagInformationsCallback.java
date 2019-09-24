package cn.rrg.rdv.callback;

public interface TagInformationsCallback<T> {
    //显示信息是以什么格式传入到上层的!
    void onInformationsShow(T t);
}