package cn.rrg.rdv.activities.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.proxgrind.pm3flasher.Proxmark3Flasher;

import cn.dxl.com.Com;
import cn.dxl.common.util.LogUtils;
import cn.dxl.common.widget.ToastUtil;
import cn.rrg.com.DevCallback;
import cn.rrg.com.UsbSerialControl;
import cn.rrg.devices.EmptyDeivce;
import cn.rrg.rdv.R;
import cn.rrg.rdv.util.Paths;

public class PM3FlasherMainActivity extends BaseActivity implements DevCallback<String> {

    private enum MODE {
        BOOT, OS
    }

    private UsbSerialControl control = UsbSerialControl.get();
    private Proxmark3Flasher flasher = Proxmark3Flasher.getFlasher();

    //在程序执行读卡，写卡操作时的程序后台交互性提醒对话框
    private AlertDialog mDialogWorkingState = null;

    private TextView txtShowLog;
    private Button btnFlash;

    private MODE mode = MODE.BOOT;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pm3_flasher);

        // init and try to connect.
        control.register(this, this);
        Com.initCom(control, new EmptyDeivce());

        initViews();
        initActions();
    }

    private void initViews() {
        mDialogWorkingState = new AlertDialog.Builder(context).create();
        View _workingStateMsgView = View.inflate(context, R.layout.dialog_working_msg, null);
        mDialogWorkingState.setView(_workingStateMsgView);
        mDialogWorkingState.setTitle(R.string.tips);
        mDialogWorkingState.setCancelable(false);

        btnFlash = findViewById(R.id.btnFlash);
        txtShowLog = findViewById(R.id.txtShowLog);
    }

    private void initActions() {
        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!flasher.isPM3Opened()) {
                    if (!flasher.openProxmark3()) {
                        return;
                    }
                }
                mode = MODE.BOOT;
                if (control.connect(null)) {
                    flashDefaultFW();
                } else {
                    ToastUtil.show(context, getString(R.string.tips_device_no_found), false);
                }
            }
        });
    }

    private void appendLog(String log) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtShowLog.append("\n");
                txtShowLog.append(log);
            }
        });
    }

    private void flashDefaultFW() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 如果没有在Bootloader模式下，我们需要先重启设备使其进入Bootloader模式下!
                appendLog("开始判断当前是否是BOOTROM模式");
                if (flasher.isBootloaderMode()) {
                    appendLog("当前是BOOTROM模式");
                    switch (mode) {
                        case BOOT:
                            if (flasher.flashBootRom(Paths.PM3_IMAGE_BOOT_FILE)) {
                                // ToastUtil.show(context, getString(R.string.tips_boot_flash_success), false);
                                appendLog(getString(R.string.tips_boot_flash_success));
                            } else {
                                // ToastUtil.show(context, getString(R.string.tips_boot_flash_failed), false);
                                appendLog(getString(R.string.tips_boot_flash_failed));
                            }
                            finishFlash();
                            // 开启下一轮!
                            mode = MODE.OS;
                            break;

                        case OS:
                            if (flasher.flashFullImg(Paths.PM3_IMAGE_OS_FILE)) {
                                // ToastUtil.show(context, getString(R.string.tips_os_flash_success), false);
                                appendLog(getString(R.string.tips_os_flash_success));
                            } else {
                                // ToastUtil.show(context, getString(R.string.tips_os_flash_failed), false);
                                appendLog(getString(R.string.tips_os_flash_failed));
                            }
                            finishFlash();
                            ToastUtil.show(context, getString(R.string.finish), false);
                            mode = MODE.BOOT;
                            break;
                    }
                } else {
                    appendLog("当前不是BOOTROM模式，将会重启设备进入BOOTROM模式");
                    if (!flasher.enterBootloader()) {
                        appendLog("PM3进入刷写模式失败!");
                    }
                }
            }
        }).start();
    }

    private void finishFlash() {
        flasher.flashModeClose();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        control.unregister(this);
        flasher.closeProxmark3();
    }

    @Override
    public void onAttach(String dev) {
        // 自动连接设备!
        control.connect(dev);
        flashDefaultFW();
    }

    @Override
    public void onDetach(String dev) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // LogUtils.d("X: " + event.getRawX());
        // LogUtils.d("Y: " + event.getRawY());
        return super.onTouchEvent(event);
    }
}
