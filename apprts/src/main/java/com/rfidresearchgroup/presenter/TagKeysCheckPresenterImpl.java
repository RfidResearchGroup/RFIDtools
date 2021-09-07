package com.rfidresearchgroup.presenter;

import android.util.Log;

import com.rfidresearchgroup.callback.KeysAuthCallback;
import com.rfidresearchgroup.javabean.M1KeyBean;

public abstract class TagKeysCheckPresenterImpl
        extends AbsTagKeysCheckPresenter {

    private KeysAuthCallback callback = new KeysAuthCallback() {
        @Override
        public void onTagAbnormal() {
            if (isViewAttach())
                view.onTagAbnormal();
        }

        @Override
        public void onKeysInvalid() {
            if (isViewAttach())
                view.onKeysInvalid();
        }

        @Override
        public void onAuth(int sectorRemains) {
            if (isViewAttach())
                view.onAuth(sectorRemains);
        }

        @Override
        public void onKeys(String key) {
            if (isViewAttach())
                view.onKeys(key);
        }

        @Override
        public void onResults(M1KeyBean[] keyBeans) {
            if (isViewAttach())
                view.onResults(keyBeans);
        }
    };

    @Override
    public void startCheck() {
        if (isViewAttach()) {
            Log.d(LOG_TAG, "startCheck() 底层开始检测!");
            // 传递扇区大小，为进度条初始化做准备!
            view.onStart(checkModel.getSectorCount());
            // 开始检测秘钥!
            checkModel.checkAllByAllKeys(callback);
        }
    }

    @Override
    public void startCheck(int sector) {
        if (isViewAttach()) {
            checkModel.checkOneByAllKeys(sector, callback);
        }
    }

    @Override
    public void startCheck(int sector, M1KeyBean keyBean) {
        if (isViewAttach()) {
            checkModel.checkOneByCustomerKey(sector, keyBean, callback);
        }
    }

    @Override
    public void stopChecks() {
        if (isViewAttach()) {
            if (checkModel != null) {
                checkModel.stop();
            }
        }
    }
}
