package cn.rrg.rdv.callback;

import cn.rrg.rdv.javabean.M1KeyBean;

public interface KeysAuthCallback {
    // 标签异常
    void onTagAbnormal();

    // 输入秘钥无效!
    void onKeysInvalid();

    // 在验证的时候
    void onAuth(int sectorRemains);

    // 在秘钥轮询的时候
    void onKeys(String key);

    // 在验证完成时的结果回调!
    void onResults(M1KeyBean[] keyBeans);
}
