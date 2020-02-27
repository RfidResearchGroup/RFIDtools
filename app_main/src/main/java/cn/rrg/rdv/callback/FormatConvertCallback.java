package cn.rrg.rdv.callback;

public interface FormatConvertCallback extends BaseCallback {

    //转换回调
    interface ConvertCallback {
        //转换成功后以字节返回
        void onConvertSuccess(byte[] result);

        //转换失败应当有消息
        void onConvertFail(String errorMsg);
    } 

    //保存回调
    interface SaveCallback {
        //转换成功后以字节返回
        void onSaveSuccess();

        //转换失败应当有消息
        void onSaveFail(String errorMsg);
    }

    //类型检测回调
    interface TypeCheckCallback {

        void isTxt();

        void isBin();

        void isNot();
    }
}
