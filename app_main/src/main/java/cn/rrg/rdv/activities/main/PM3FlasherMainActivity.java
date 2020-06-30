package cn.rrg.rdv.activities.main;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.iobridges.com.LocalComBridgeAdapter;
import com.proxgrind.pm3flasher.Proxmark3Flasher;
import com.proxgrind.pm3flasher.Target;

import cn.dxl.common.widget.ToastUtil;
import cn.proxgrind.com.DevCallback;
import cn.proxgrind.com.UsbSerialControl;
import cn.rrg.rdv.R;
import cn.rrg.rdv.util.Commons;
import cn.rrg.rdv.util.Paths;
import cn.rrg.rdv.util.Proxmark3Installer;

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
        control.register(this);

        // start communication forward!
        LocalComBridgeAdapter.getInstance()
                .startServer(LocalComBridgeAdapter.NAMESPACE_DEFAULT);

        initViews();
        initActions();

        setStatus(true);
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
                    LocalComBridgeAdapter.getInstance()
                            .setInputStream(control.getInput())
                            .setOutputStream(control.getOutput());
                    flashDefaultFW();
                } else {
                    ToastUtil.show(context, getString(R.string.tips_device_no_found), false);
                }
            }
        });
    }

    private void flashDefaultFW() {
        if (!mDialogWorkingState.isShowing())
            mDialogWorkingState.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!flasher.isStart(Target.CLIENT)) {
                    if (!flasher.start(Target.CLIENT)) {
                        ToastUtil.show(context, "Client open failed!", false);
                        dismissWorkingDialog();
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
                            finish();
                            break;
                    }
                } else {
                    if (!flasher.start(Target.BOOT)) {
                        ToastUtil.show(context, getString(R.string.tips_pm3_enter_failed), false);
                        dismissWorkingDialog();
                    }
                    // 记得关闭客户端
                    flasher.close(Target.CLIENT);
                }
            }
        }).start();
    }

    private void dismissWorkingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDialogWorkingState.dismiss();
            }
        });
    }

    private void finishFlash() {
        flasher.close(Target.BOOT);
        flasher.close(Target.CLIENT);
    }

    private void closeClient() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                flasher.close(Target.CLIENT);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDialogWorkingState.dismiss();
        control.unregister();
        closeClient();
        LocalComBridgeAdapter.getInstance()
                .stopClient();
    }

    @Override
    public void onAttach(String dev) {
        // 自动连接设备!
        if (control.connect(dev)) {
            // 更新流引用!
            LocalComBridgeAdapter.getInstance()
                    .setInputStream(control.getInput())
                    .setOutputStream(control.getOutput());
            flashDefaultFW();
        } else {
            ToastUtil.show(context, "Device connect failed!", false);
        }
    }

    @Override
    public void onDetach(String dev) {

    }
}
