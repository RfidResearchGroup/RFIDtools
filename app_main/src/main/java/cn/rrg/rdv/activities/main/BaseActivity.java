package cn.rrg.rdv.activities.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;

import cn.dxl.common.interfaces.OnTouchListener;
import cn.dxl.common.util.HexUtil;
import cn.dxl.common.util.LanguageUtil;
import cn.dxl.common.widget.ToastUtil;
import cn.dxl.common.util.VibratorUtils;
import cn.dxl.mifare.GlobalTag;
import cn.dxl.mifare.StdMifareIntent;
import cn.rrg.rdv.R;
import cn.rrg.rdv.application.Properties;

/**
 * Created by DXL on 2017/10/26.
 */
public abstract class BaseActivity
        extends AppCompatActivity implements GlobalTag.OnNewTagListener {

    private boolean isTagListener = false;

    protected String LOG_TAG = this.getClass().getSimpleName();
    protected Context context = this;
    protected Activity activity = this;

    private StdMifareIntent mStdMfUtil = null;

    private ArrayList<OnTouchListener> onTouchListeners;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Act is create");
        //实例化一个标准NFC设备工具对象!
        mStdMfUtil = new StdMifareIntent(this);
        //添加全局的标签状态监听!
        GlobalTag.addListener(this);
        //建立事件观察数列!
        onTouchListeners = new ArrayList<>();

        // 在onCreate()也要设置一下语言，有可能attachBaseContext()不生效。
        LanguageUtil.setAppLanguage(this, Properties.v_app_language);
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
        /*
         * 在这里解注册各大前台事件!
         * */
        mStdMfUtil.disableForegroundDispatch(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        if (Properties.v_app_language.equals("auto")) {
            //如果value = auto，则设置为跟随系统!
            super.attachBaseContext(newBase);
        } else {
            //否则国际化!
            super.attachBaseContext(LanguageUtil.setAppLanguage(newBase, Properties.v_app_language));
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
        //有新的意图时会启动
        Bundle data = intent.getExtras();
        if (data != null) {
            Tag tag = data.getParcelable(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                //存入全局操作域!
                GlobalTag.setTag(tag);
                //显示 UID！
                ToastUtil.show(this, getString(R.string.tips_uid) + HexUtil.toHexString(tag.getId()), true);
                //震动一下!
                VibratorUtils.runOneAsDelay(context, 1000);
                GlobalTag.notifyOnNewTag(tag);
            }
        }
        super.onNewIntent(intent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //Log.d(LOG_TAG, "观察者数量: " + onTouchListeners.size());
        //在act获得了event的时候回调!
        if (onTouchListeners.size() == 0) return super.dispatchTouchEvent(ev);
        for (OnTouchListener listener : onTouchListeners) {
            // 进行相关的事件下发!
            if (listener != null) {
                //Log.d(LOG_TAG, "观察者: " + listener.toString());
                try {
                    listener.onTouch(ev);
                } catch (Exception ignored) {
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void addTouchListener(OnTouchListener listener) {
        //Log.d(LOG_TAG, "添加观察者: " + listener.toString());
        onTouchListeners.add(listener);
    }

    public void removeTouchListener(OnTouchListener listener) {
        //Log.d(LOG_TAG, "移除观察者: " + listener.toString());
        onTouchListeners.remove(listener);
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
        GlobalTag.removeListener(this);
    }

    @Override
    public void onNewTag(Tag tag) {

    }
}
