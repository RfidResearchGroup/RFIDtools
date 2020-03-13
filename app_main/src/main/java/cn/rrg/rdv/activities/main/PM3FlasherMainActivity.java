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
import com.proxgrind.pm3flasher.Target;

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
    }

    private void initActions() {
        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = MODE.BOOT;
                if (control.connect(null)) {
                    flashDefaultFW();
                } else {
                    ToastUtil.show(context, getString(R.string.tips_device_no_found), false);
                }
            }
        });
    }

    private void flashDefaultFW() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!flasher.isStart(Target.CLIENT)) {
                    if (!flasher.start(Target.CLIENT)) {
                        return;
                    }
                }
                // 如果没有在Bootloader模式下，我们需要先重启设备使其进入Bootloader模式下!
                if (flasher.isStart(Target.BOOT)) {
                    switch (mode) {
                        case BOOT:
                            if (flasher.flashBootRom(Paths.PM3_IMAGE_BOOT_FILE)) {
                                ToastUtil.show(context, getString(R.string.tips_boot_flash_success), false);
                            } else {
                                ToastUtil.show(context, getString(R.string.tips_boot_flash_failed), false);
                            }
                            finishFlash();
                            // 开启下一轮!
                            mode = MODE.OS;
                            break;

                        case OS:
                            if (flasher.flashFullImg(Paths.PM3_IMAGE_OS_FILE)) {
                                ToastUtil.show(context, getString(R.string.tips_os_flash_success), false);
                            } else {
                                ToastUtil.show(context, getString(R.string.tips_os_flash_failed), false);
                            }
                            finishFlash();
                            ToastUtil.show(context, getString(R.string.finish), false);
                            mode = MODE.BOOT;
                            break;
                    }
                } else {
                    if (!flasher.start(Target.BOOT)) {
                        ToastUtil.show(context, getString(R.string.tips_pm3_enter_failed), false);
                    }
                }
            }
        }).start();
    }

    private void finishFlash() {
        flasher.close(Target.BOOT);
        flasher.close(Target.CLIENT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        control.unregister(this);
        flasher.close(Target.CLIENT);
    }

    @Override
    public void onAttach(String dev) {
        // 自动连接设备!
        control.connect(dev);
        flashDefaultFW();
    }

    @Override
    public void onDetach(String dev) {
        flasher.close(Target.CLIENT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // LogUtils.d("X: " + event.getRawX());
        // LogUtils.d("Y: " + event.getRawY());
        return super.onTouchEvent(event);
    }
}
