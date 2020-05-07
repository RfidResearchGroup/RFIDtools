package cn.rrg.rdv.activities.main;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iobridges.com.LocalComBridgeAdapter;

import cn.dxl.common.util.DisplayUtil;
import cn.dxl.common.util.FragmentUtil;
import cn.dxl.common.util.LogUtils;
import cn.proxgrind.com.UsbSerialControl;
import cn.rrg.rdv.R;
import cn.rrg.rdv.fragment.base.AppMainDevicesFragment;
import cn.rrg.rdv.fragment.tools.MainSettingsFragment;
import cn.rrg.rdv.fragment.tools.ToolsAccessFragment;

public class AppMain extends BaseActivity {

    private BottomNavigationView bnvMain;
    // main device function fragment!
    private AppMainDevicesFragment appMainDevicesFragment;
    // tools fragment
    private ToolsAccessFragment toolsAccessFragment;
    // settings fragment
    private MainSettingsFragment mainSettingsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_app_main);
        // set status color!
        setStatus(!DisplayUtil.isDarkModeStatus(this));

        appMainDevicesFragment = new AppMainDevicesFragment();
        toolsAccessFragment = new ToolsAccessFragment();
        mainSettingsFragment = new MainSettingsFragment();

        initViews();
        initActions();

        gotoFragment(appMainDevicesFragment);
    }

    private void initViews() {
        bnvMain = findViewById(R.id.bnvPageChange);
    }

    private void initActions() {
        bnvMain.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_main:
                        // goto main page!
                        gotoFragment(appMainDevicesFragment);
                        break;
                    case R.id.item_tools:
                        gotoFragment(toolsAccessFragment);
                        break;
                    case R.id.item_setting:
                        gotoFragment(mainSettingsFragment);
                        break;
                }
                return true;
            }
        });
    }

    private void gotoFragment(Fragment fragment) {
        FragmentUtil.hides(getSupportFragmentManager(), fragment);
        if (fragment.isAdded()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .show(fragment)
                    .commitAllowingStateLoss();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, fragment)
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setMessage("Closing...").show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                LocalComBridgeAdapter.getInstance().destroy();
                UsbSerialControl.get().disconect();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        // back event dispatch!
                        appMainDevicesFragment.onBackPressed();
                        AppMain.super.onBackPressed();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d("AppMain结束!");
        // System.exit(0);
    }
}
