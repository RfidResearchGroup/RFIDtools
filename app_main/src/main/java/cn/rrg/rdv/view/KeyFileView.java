package cn.rrg.rdv.view;


/*
 *视图操作回调
 */
public interface KeyFileView extends BaseMvpView {

    //显示密钥到界面
    void showKeyList(String key); 

    //显示错误信息
    void showKeyError();

    //在修改成功后的回调
    void onKeysModifySuccess();

    //创建成功后的回调
    void onCreateFileSuccess();

    void onCreateFileFailed();
}
