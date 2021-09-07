package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.callback.KeyFileCallbak;
import com.rfidresearchgroup.models.KeyFileModel;
import com.rfidresearchgroup.view.KeyFileView;

/*
 * 中介类
 *用于将MVP中的V与M进行解耦通信
 */
public class KeyFilePresenter extends BasePresenter<KeyFileView> {

    //中介与视图的通信接口
    public void showKeyList(String file) {
        KeyFileModel.getKeyString(file, new KeyFileCallbak.KeyFileReadCallbak() {
            @Override
            public void onReadSuccess(String msg) {
                if (isViewAttach())
                    view.showKeyList(msg);
            }

            @Override
            public void onReadFail() {
                if (isViewAttach()) {
                    view.showKeyError();
                }
            }

        });
    }

    public void writeKeyList(String keyStr, String file) {
        KeyFileModel.setKeyString(keyStr, file, new KeyFileCallbak.KeyFileWriteCallbak() {
            @Override
            public void onWriteSuccess(String msg) {
                if (isViewAttach()) {
                    view.showToast(msg);
                    view.onKeysModifySuccess();
                }
            }

            @Override
            public void onWriteFail() {
                if (isViewAttach())
                    view.showKeyError();
            }
        });
    }

    public void createKeyFile(String name) {
        KeyFileModel.createKeyFile(name, new KeyFileCallbak.KeyFileCreateCallback() {
            @Override
            public void onCreateSuccess() {
                if (isViewAttach())
                    view.onCreateFileSuccess();
            }

            @Override
            public void onCreateFail() {
                if (isViewAttach())
                    view.onCreateFileFailed();
            }
        });
    }

}
