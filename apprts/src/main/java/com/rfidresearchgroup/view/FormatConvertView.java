package com.rfidresearchgroup.view;

public interface FormatConvertView extends BaseMvpView {
    void onConvertSucess(byte[] result); 

    void onConvertFail(String errorMsg);

    void onSaveSuccess();

    void onSaveFail(String errorMsg);
}
