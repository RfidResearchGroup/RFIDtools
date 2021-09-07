package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.javabean.M1KeyBean;
import com.rfidresearchgroup.models.AbsTagKeysCheckModel;
import com.rfidresearchgroup.view.MfKeysCheckView;

import java.io.File;

public abstract class AbsTagKeysCheckPresenter
        extends BasePresenter<MfKeysCheckView> implements AbsTagKeysCheckModel.KeyFilesCallback {

    public AbsTagKeysCheckModel checkModel = getTagKeysCheckModel();

    // 初始化秘钥检测底层实现!
    public abstract AbsTagKeysCheckModel getTagKeysCheckModel();

    // 开始检索全部扇区全部秘钥
    public abstract void startCheck();

    // 使用所有秘钥检索一个扇区
    public abstract void startCheck(int sector);

    // 检索单个扇区自定义秘钥
    public abstract void startCheck(int sector, M1KeyBean keyBean);

    // 停止检索
    public abstract void stopChecks();

    @Override
    public File[] getKeyFiles() {
        if (isViewAttach()) {
            return view.getKeyFiles();
        }
        return null;
    }
}
