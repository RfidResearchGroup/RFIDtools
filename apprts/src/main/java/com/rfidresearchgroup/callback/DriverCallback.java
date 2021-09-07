package com.rfidresearchgroup.callback;

public interface DriverCallback {
    interface DriverCheckCallback {
        void onCheckCur(String curDriver);
    }

    interface DriverChangeCallback {
        void onChangeSuccess(String newDriver);

        void onChangeFail(String msg);
    }
}
