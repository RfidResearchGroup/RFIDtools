package com.rfidresearchgroup.view;

import java.io.File;

import com.rfidresearchgroup.javabean.M1KeyBean;


// 因为这个视图是基于MF标签的秘钥检查回调，因此需要扩展标签状态检测的回调。
public interface MfKeysCheckView extends BaseMvpView, TagStateView {
    // 提供一个接口，用于获取相关的秘钥文件!
    File[] getKeyFiles();

    // 在秘钥验证开始时的回调!
    void onStart(int sectorCount);

    void onKeysInvalid();

    // 在验证的时候
    void onAuth(int sectorRemains);

    // 在秘钥轮训的时候
    void onKeys(String key);

    // 在验证完成时的结果回调!
    void onResults(M1KeyBean[] keyBeans);
}
