package cn.rrg.rdv.activities.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import cn.dxl.common.util.DisplayUtil;
import cn.dxl.common.util.HexUtil;
import cn.dxl.common.util.LanguageUtil;
import cn.dxl.common.util.LogUtils;
import cn.dxl.common.util.StatusBarUtil;
import cn.dxl.common.widget.ToastUtil;
import cn.dxl.common.util.VibratorUtils;
import cn.dxl.mifare.NfcTagListenUtils;
import cn.dxl.mifare.StdMifareIntent;
import cn.rrg.rdv.R;
import cn.rrg.rdv.util.Commons;

/**
 * Created by DXL on 2017/10/26.
 */
public abstract class BaseActivity
        extends AppCompatActivity implements NfcTagListenUtils.OnNewTagListener {

    protected String LOG_TAG = this.getClass().getSimpleName();
    protected Context context = this;
    protected Activity activity = this;

    private StdMifareIntent mStdMfUtil = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Act is create");
        mStdMfUtil = new StdMifareIntent(this);
        NfcTagListenUtils.addListener(this);

        LanguageUtil.setAppLanguage(this, Commons.getLanguage());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        setStatus(!DisplayUtil.isDarkModeStatus(this));
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setStatus(!DisplayUtil.isDarkModeStatus(this));
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        setStatus(!DisplayUtil.isDarkModeStatus(this));
    }

    public void setStatus(boolean darkMode) {
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        if (!StatusBarUtil.setStatusBarDarkTheme(this, darkMode)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0x55000000);
        }
        StatusBarUtil.setLightNavigationBar(this, darkMode);
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "Act is start");
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "Act is pause");
        super.onPause();
        mStdMfUtil.disableForegroundDispatch(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String language = Commons.getLanguage();
        if (language.equals("auto")) {
            super.attachBaseContext(newBase);
        } else {
            LogUtils.d("New app language: " + language);
            super.attachBaseContext(LanguageUtil.setAppLanguage(newBase, language));
        }
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "Act is resume");
        super.onResume();
        /*
         * 在这里注册各大前台事件
         * */
        mStdMfUtil.enableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle data = intent.getExtras();
        if (data != null) {
            Tag tag = data.getParcelable(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                NfcTagListenUtils.setTag(tag);
                ToastUtil.show(this, getString(R.string.tips_uid) + HexUtil.toHexString(tag.getId()), true);
                VibratorUtils.runOneAsDelay(context, 1000);
                NfcTagListenUtils.notifyOnNewTag(tag);
            }
        }
        super.onNewIntent(intent);
    }

    @Override
    protected void onStop() {
        //Log.d(LOG_TAG, "Act is stop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //Log.d(LOG_TAG, "Act is destory");
        super.onDestroy();
        NfcTagListenUtils.removeListener(this);
    }

    @Override
    public void onNewTag(Tag tag) {

    }
}
