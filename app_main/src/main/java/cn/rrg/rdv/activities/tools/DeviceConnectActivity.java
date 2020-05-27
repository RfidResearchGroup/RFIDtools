package cn.rrg.rdv.activities.tools;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.MenuItem;

import java.util.Arrays;

import cn.dxl.common.util.DisplayUtil;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.adapter.FragmentListPagerAdapter;
import cn.rrg.rdv.callback.ConnectFailedCtxCallback;
import cn.rrg.rdv.fragment.connect.DeviceConnectAllFragment;
import cn.rrg.rdv.fragment.connect.DeviceConnectNewFragment;
import cn.rrg.rdv.models.AbstractDeviceModel;
import cn.dxl.common.implement.OnPageChangeListenerImpl;

/*
 * 专供设备链接!
 * */
public abstract class DeviceConnectActivity
        extends BaseActivity implements ConnectFailedCtxCallback {

    //抽象方法，子类需要实现数据源的初始化动作!
    public abstract AbstractDeviceModel[] getModels();

    //抽象方法，获得连接成功后指向的目标act!
    public abstract Class getTarget();

    //抽象方法，获得连接时的显示的消息!
    public abstract String getConnectingMsg();

    //抽象方法，获得链接失败后需要做的事情!
    public abstract ConnectFailedCtxCallback getCallback();

    //数据源承载数组!
    public AbstractDeviceModel[] models;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_device_connect);

        BottomNavigationView bnvSwitch = findViewById(R.id.bnvDeviceConnectSwitch);
        ViewPager vpContainer = findViewById(R.id.vpDeviceConnectPager);

        models = getModels();
        //在onCreate中注册数据源!
        for (AbstractDeviceModel model : models) {
            model.register();
        }

        Fragment newDevFragment = new DeviceConnectNewFragment();
        Fragment allDevFragment = new DeviceConnectAllFragment();

        //初始化分页适配器！
        FragmentPagerAdapter mAdapter =
                new FragmentListPagerAdapter(getSupportFragmentManager(),
                        Arrays.asList(newDevFragment, allDevFragment)
                );

        vpContainer.setAdapter(mAdapter);

        //fragment切换时底部导航栏也需要切换！
        vpContainer.addOnPageChangeListener(new OnPageChangeListenerImpl() {
            @Override
            public void onPageSelected(int i) {
                bnvSwitch.setSelectedItemId(bnvSwitch.getMenu().getItem(i).getItemId());
            }
        });
        //底部导航栏变动时页面也需要跟着变动!
        bnvSwitch.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_newDev:
                        vpContainer.setCurrentItem(0);
                        break;
                    case R.id.menu_existDev:
                        vpContainer.setCurrentItem(1);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (AbstractDeviceModel model : models) {
            model.unregister();
        }
    }

    @Override
    public void onFailed(Activity context) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isDestroyed()) {
                    new AlertDialog.Builder(context)
                            .setTitle(getString(R.string.connect_faild))
                            .setMessage(getString(R.string.msg_connect_err_common))
                            .show();
                }
            }
        });
    }
}
