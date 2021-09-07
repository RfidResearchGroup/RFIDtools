package com.rfidresearchgroup.fragment.connect;

import android.app.Activity;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.widget.TextView;

import com.rfidresearchgroup.callback.ConnectFailedCtxCallback;
import com.rfidresearchgroup.fragment.base.AppMainDevicesFragment;
import com.rfidresearchgroup.models.AbstractDeviceModel;
import com.rfidresearchgroup.rfidtools.R;
import com.rfidresearchgroup.view.DeviceView;

import java.util.ArrayList;

import com.rfidresearchgroup.activities.tools.DeviceConnectActivity;
import com.rfidresearchgroup.presenter.DevicePresenter;

public abstract class DeviceConnectFragment
        extends DeviceConnectBaseFragment {

    //持有中介者,这个中介者只负责连接，不负责数据
    private ArrayList<DevicePresenter<DeviceView>> presenters = new ArrayList<>();
    AbstractDeviceModel[] models;
    private Class target;
    //链接失败的回调，这个地方失败一般是设备或者设备使用的固件有问题，才会被回调!
    private ConnectFailedCtxCallback connectCallback;

    @Override
    protected void initResource() {
        Activity activity = getActivity();
        if (activity == null) return;
        if (activity instanceof DeviceConnectActivity) {
            this.models = ((DeviceConnectActivity) activity).models;
            if (models != null) {
                this.target = ((DeviceConnectActivity) activity).getTarget();
                this.connectCallback = ((DeviceConnectActivity) activity).getCallback();
                for (AbstractDeviceModel m : models) {
                    //初始化中介者并且绑定视图!
                    DevicePresenter<DeviceView> presenter = new DevicePresenter<>(m);
                    presenter.attachSubView(this);
                    presenter.attachView(this);
                    presenters.add(presenter);
                }
                String msg = ((DeviceConnectActivity) activity).getConnectingMsg();
                if (msg != null) {
                    ((TextView) (msgView.findViewById(R.id.text1))).setText(msg);
                }
            } else {
                throw new RuntimeException("Models not init exception!");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //销毁视图绑定!
        for (DevicePresenter presenter : presenters) {
            presenter.detachSubView();
        }
    }

    protected void showOrDismissEmptyView() {
        //判断视图是否为空!
        if (arrayAdapter.getCount() == 0) {
            showEmptyView();
        } else {
            dismissEmptyView();
        }
    }

    @Override
    protected void onConnectDev(String address) {
        Log.d(LOG_TAG, "开始进行设备连接!");
        for (DevicePresenter presenter : presenters) {
            Log.d(LOG_TAG, "使用" + presenter.getClass() + "进行设备连接!");
            presenter.connect(address);
        }
    }

    @Override
    protected void onDisconnect() {
        for (DevicePresenter presenter : presenters) {
            presenter.disconnect();
        }
    }

    @Override
    protected void onInitNfcDev() {
        //TODO 此处调用所有的通信中介者进行初始化，如果处理不好这是有极大的问题的!
        for (DevicePresenter presenter : presenters) {
            presenter.initNfcAdapter();
        }
    }

    //TODO 不允许数据回传被重写!
    @Override
    protected final void onInitSuccess() {
        showToast(getString(R.string.device_init_success));
        //TODO 此次做出连接结果返回的处理!
        Activity activity = getActivity();
        if (activity != null) {
            sendConnectResultBroadcast(activity, true);
            //跳转到目标act!
            startActivity(new Intent(getActivity(), target));
            //结束自身!
            activity.finish();
        }
    }

    private void sendConnectResultBroadcast(Activity act, boolean result) {
        if (act == null) return;
        // 发送广播告诉主页面连接成功了!
        LocalBroadcastManager.getInstance(act).sendBroadcast(
                new Intent(AppMainDevicesFragment.ACTION_CONNECTION_STATE_UPDATE)
                        .putExtra(AppMainDevicesFragment.EXTRA_CONNECTION_STATE, result)
        );
    }

    @Override
    public void onError(String e) {
        showToast(e);
    }

    @Override
    public void onInitNfcAdapterFail() {
        super.onInitNfcAdapterFail();
        Activity activity = getActivity();
        // 如果回调不为空，则进行回调!
        if (connectCallback != null) {
            connectCallback.onFailed(activity);
        }
        sendConnectResultBroadcast(activity, false);
    }
}
