package cn.rrg.rdv.fragment.init;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.dxl.common.widget.ToastUtil;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.AppMain;
import cn.rrg.rdv.fragment.base.BaseFragment;
import cn.rrg.rdv.util.InitUtil;

public class InitFragment
        extends BaseFragment
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.act_login_init, container, false);
        //开始初始化
        new InitThread().start();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showToast(String msg) {
        if (getActivity() != null)
            ToastUtil.show(getActivity(), msg, false);
    }

    @Override
    public void showDialog(String title, String msg) {

    }

    @Override
    public void hideDialog() {

    }

    class InitThread extends Thread {
        @Override
        public void run() {
            try {
                //初始化程序所需要的资源
                InitUtil.initApplicationResource(getContext());
                //开启日志打印!
                // InitUtil.startLogcat(true);
                //进行一些设置的读取初始化!
                InitUtil.initSettings();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //跳转到主页面处理!
            go2MainAct();
        }
    }

    private void go2MainAct() {
        Activity activity = getActivity();
        if (activity != null) {
            //销毁自己!
            if (getActivity() != null)
                getActivity().finish();
            startActivity(new Intent(getActivity(), AppMain.class));
        }
    }
}
