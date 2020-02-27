package cn.rrg.rdv.callback;

/**
 * 标签状态回调视图
 */
public interface TagStateCallback {
    // 标签异常
    void onTagAbnormal();

    // 标签普通
    void onTagOrdinary();

    // 标签特殊
    void onTagSpecial();
}
