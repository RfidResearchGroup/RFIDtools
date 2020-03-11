package cn.rrg.rdv.callback;

import android.app.Activity;

import java.io.Serializable;

public interface ConnectFailedCtxCallback extends Serializable {
    void onFailed(Activity context);
}
