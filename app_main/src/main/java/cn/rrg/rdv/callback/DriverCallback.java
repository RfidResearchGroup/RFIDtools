package cn.rrg.rdv.callback;

public interface DriverCallback {
    interface DriverCheckCallback {
        void onCheckCur(String curDriver);
    }

    interface DriverChangeCallback {
        void onChangeSuccess(String newDriver);

        void onChangeFail(String msg);
    }
}
