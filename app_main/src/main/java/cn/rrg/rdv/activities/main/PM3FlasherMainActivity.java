package cn.rrg.rdv.activities.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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

    private AlertDialog usbConnectWaitDialog;
    //在程序执行读卡，写卡操作时的程序后台交互性提醒对话框
    private AlertDialog mDialogWorkingState = null;

    private Button btnFlash;

    private volatile boolean startLabel = false;
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

        if (!control.connect(null)) {
            usbConnectWaitDialog.show();
        }
    }

    private void initViews() {
        usbConnectWaitDialog = new AlertDialog.Builder(context, R.style.CustomerDialogStyle)
                .setView(View.inflate(context, R.layout.item_usb_device_connecting, null))
                .create();
        usbConnectWaitDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ImageView imgUsbConnect = usbConnectWaitDialog.findViewById(R.id.imgUsbConnect);
                Glide.with(context).load(R.drawable.usb_connect).into(imgUsbConnect);
            }
        });
        usbConnectWaitDialog.setCancelable(false);

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
                btnFlash.setEnabled(false);
                startLabel = true;
                mode = MODE.BOOT;
                if (control.connect(null)) {
                    flashDefaultFW();
                } else {
                    ToastUtil.show(context, getString(R.string.tips_device_no_found), false);
                    usbConnectWaitDialog.show();
                }
            }
        });
    }

    private void showWorkingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDialogWorkingState.show();
            }
        });
    }

    private void dissmissWorkingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDialogWorkingState.dismiss();
            }
        });
    }

    private void flashDefaultFW() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!flasher.isPM3Opened()) {
                    if (!flasher.openProxmark3()) {
                        LogUtils.d("PM3打开失败!");
                        return;
                    } else {
                        LogUtils.d("PM3打开成功!");
                    }
                }
                LogUtils.d("开始刷机!");
                showWorkingDialog();
                // 如果没有在Bootloader模式下，我们需要先重启设备使其进入Bootloader模式下!
                if (flasher.isBootloaderMode()) {
                    LogUtils.d("当前处于Bootloader模式下，将会开始刷机!");
                    switch (mode) {
                        case BOOT:
                            if (flasher.flashBootRom(Paths.PM3_IMAGE_BOOT_FILE)) {
                                LogUtils.d("PM3刷写BootRom成功!");
                            } else {
                                LogUtils.d("PM3刷写BootRom失败!");
                            }
                            finishFlash();
                            // 开启下一轮!
                            mode = MODE.OS;
                            break;
                        case OS:
                            if (flasher.flashFullImg(Paths.PM3_IMAGE_OS_FILE)) {
                                LogUtils.d("PM3刷写BootRom成功!");
                            } else {
                                LogUtils.d("PM3刷写BootRom失败!");
                            }
                            finishFlash();
                            startLabel = false;
                            ToastUtil.show(context, getString(R.string.finish), false);
                            break;
                    }
                    dissmissWorkingDialog();
                } else {
                    if (!flasher.enterBootloader()) {
                        LogUtils.d("PM3进入刷写模式失败!");
                    }
                }
                dissmissWorkingDialog();
            }
        }).start();
    }

    private void finishFlash() {
        flasher.flashModeClose();
        flasher.closeProxmark3();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        control.unregister(this);
        usbConnectWaitDialog.dismiss();
    }

    @Override
    public void onAttach(String dev) {
        // 自动连接设备!
        control.connect(dev);
        usbConnectWaitDialog.dismiss();
        if (startLabel) {
            flashDefaultFW();
        }
    }

    @Override
    public void onDetach(String dev) {
        flasher.closeProxmark3();
        usbConnectWaitDialog.show();
    }
}
