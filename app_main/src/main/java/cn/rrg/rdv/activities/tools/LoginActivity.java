package cn.rrg.rdv.activities.tools;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import cn.dxl.common.application.App;
import cn.dxl.common.util.AppUtil;
import cn.dxl.common.util.LanguageUtil;
import cn.rrg.rdv.R;

import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.fragment.init.InitFragment;
import cn.rrg.rdv.fragment.init.LoginFragment;
import cn.dxl.common.implement.PermissionCallback;
import cn.dxl.common.util.FragmentUtil;
import cn.dxl.common.util.PermissionUtil;
import cn.rrg.rdv.application.Properties;
import cn.rrg.rdv.util.Commons;

/*
 * 登陆界面，，用于登陆用户系统，初始化环境，对于用户的验证，都放在这里实现
 */
public class LoginActivity
        extends BaseActivity {

    private PermissionUtil permissionUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 多语言适配!
        App app = AppUtil.getInstance().getApp();
        app.setCallback(new App.ApplicationCallback() {
            @Override
            public Context onAttachBaseContext(Context context) {
                String language = Commons.getLanguage();
                if (language.equals("auto")) {
                    //如果value = auto，则设置为跟随系统!
                    return context;
                } else {
                    //否则国际化!
                    return LanguageUtil.setAppLanguage(context, language);
                }
            }
        });

        //权限请求!
        permissionUtil = new PermissionUtil(this);
        //开始检查需要的泉权限，以做后期的初始化
        permissionUtil.setCallback(new PermissionCallback() {
            @Override
            public void onPermissionLose(PermissionUtil util) {
                //权限丢失时的回调，我们需要做出准备，开始进行权限的请求!
                //显示轮播界面!
                Fragment fragment = new LoginFragment();
                Bundle data = new Bundle();
                //存入丢失的权限列表!
                data.putStringArray("losePer", permissionUtil.getPermissionLose());
                fragment.setArguments(data);
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.container, fragment)
                        .commit();
                Log.d(LOG_TAG, "有权限丢失!");
            }

            @Override
            public void onPermissionNormal(PermissionUtil util) {
                Fragment fragment = new InitFragment();
                FragmentUtil.hides(getSupportFragmentManager(), fragment);
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.container, fragment)
                        .commit();
                Log.d(LOG_TAG, "权限正常!");
            }
        });

        String[] permissionArray = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        };
        permissionUtil.setPermissions(permissionArray);
        permissionUtil.checks();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean result = true;
        for (int i : grantResults) {
            if (i != PackageManager.PERMISSION_GRANTED) {
                result = false;
            }
            //在这里再次检查权限，如果所有的权限都通过的话则可以直接进入!
            permissionUtil.checks();
        }
        //如果所有的权限都有才能让他初始化
        if (!result) {
            Toast.makeText(this, R.string.tips_permission_request_failed, Toast.LENGTH_SHORT).show();
            //执行finish，结束当前act，直接退出初始化!!!
            finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
