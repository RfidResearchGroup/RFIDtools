package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.callback.FormatConvertCallback;
import com.rfidresearchgroup.models.FormatConvertModel;
import com.rfidresearchgroup.view.FormatConvertView;

public class FormatConvertPresenter extends BasePresenter<FormatConvertView> {

    private FormatConvertCallback.ConvertCallback mCpnvertCallback = new FormatConvertCallback.ConvertCallback() {
        @Override
        public void onConvertSuccess(byte[] result) {
            if (isViewAttach()) {
                view.onConvertSucess(result);
            }
        }

        @Override
        public void onConvertFail(String errorMsg) {
            if (isViewAttach()) {
                view.onConvertFail(errorMsg);
            }
        }
    };
    private FormatConvertCallback.SaveCallback mSaveCallback = new FormatConvertCallback.SaveCallback() {
        @Override
        public void onSaveSuccess() {
            if (isViewAttach()) {
                view.onSaveSuccess();
            }
        }

        @Override
        public void onSaveFail(String errorMsg) {
            if (isViewAttach()) {
                view.onSaveFail(errorMsg);
            }
        }
    };

    public void convert2Txt(String path) {
        FormatConvertModel.checkType(path, new FormatConvertCallback.TypeCheckCallback() {

            @Override
            public void isTxt() {
                if (isViewAttach()) {
                    view.onConvertFail("Now it's TXT");
                }
            }

            @Override
            public void isBin() {
                FormatConvertModel.bin2Txt(path, mCpnvertCallback);
            }

            @Override
            public void isNot() {
                if (isViewAttach()) {
                    //检测不出来数据的格式
                    view.onConvertFail("Can not check data format!");
                }
            }
        });
    }

    public void convert2Bin(String path) {
        FormatConvertModel.checkType(path, new FormatConvertCallback.TypeCheckCallback() {

            @Override
            public void isTxt() {
                FormatConvertModel.txt2Bin(path, mCpnvertCallback);
            }

            @Override
            public void isBin() {
                if (isViewAttach()) {
                    view.onConvertFail("Now it's BIN!");
                }
            }

            @Override
            public void isNot() {
                if (isViewAttach()) {
                    //检测不出来数据的格式
                    view.onConvertFail("Can not check data format!");
                }
            }
        });
    }

    public void save2TXT(byte[] result, String path, String name) {
        FormatConvertModel.save2Txt(path, name, result, mSaveCallback);
    }

    public void save2BIN(byte[] result, String path, String name) {
        FormatConvertModel.save2Bin(path, name, result, mSaveCallback);
    }
}
