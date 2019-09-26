package cn.dxl.mifare;

import java.io.Serializable;

public interface TestTaskAdapter extends Serializable {
    // start work.
    void start();

    // call at start finished.
    void onStart();

    // on test progress
    void progress(int max, int now);

    // stop task and recycle source
    void stop();

    // call at stop finished.
    void onStop(int status);

    // return a key if exists.
    byte[] getKeys();
}
