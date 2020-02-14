package cn.rrg.rdv.callback;

public interface KeyFileCallbak {

    interface KeyFileReadCallbak {
        //密钥读取成功时的回调
        void onReadSuccess(String msg);

        //密钥读取失败时的回调
        void onReadFail();
    }

    interface KeyFileWriteCallbak {
        //密钥写入成功时的回调
        void onWriteSuccess(String msg);

        //密钥写入失败时的回调
        void onWriteFail();
    } 

    interface KeyFileCreateCallback {
        void onCreateSuccess();

        void onCreateFail();
    }

}
