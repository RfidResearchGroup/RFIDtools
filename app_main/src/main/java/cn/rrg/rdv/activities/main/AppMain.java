package cn.rrg.rdv.activities.main;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import cn.dxl.common.util.DisplayUtil;
import cn.dxl.common.util.FragmentUtil;
import cn.rrg.rdv.R;
import cn.rrg.rdv.fragment.base.AppMainDevicesFragment;
import cn.rrg.rdv.fragment.tools.ToolsAccessFragment;

public class AppMain extends BaseActivity {

    private BottomNavigationView bnvMain;
    // main device function fragment!
    private AppMainDevicesFragment appMainDevicesFragment;
    // tools fragment
    private ToolsAccessFragment toolsAccessFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_app_main);
        // set status color!
        setStatus(!DisplayUtil.isDarkModeStatus(this));

        appMainDevicesFragment = new AppMainDevicesFragment();
        toolsAccessFragment = new ToolsAccessFragment();

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
                        break;
                }
                return true;
            }
        });
    }

    private void gotoFragment(Fragment fragment) {
        FragmentUtil.hides(getSupportFragmentManager(), fragment);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // back event dispatch!
        appMainDevicesFragment.onBackPressed();
    }
}
