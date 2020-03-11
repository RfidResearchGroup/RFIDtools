package cn.rrg.rdv.activities.main;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.proxgrind.pm3flasher.Proxmark3Flasher;

import cn.dxl.com.Com;
import cn.dxl.common.util.LogUtils;
import cn.dxl.common.widget.ToastUtil;
import cn.rrg.com.ContextCallback;
import cn.rrg.com.DevCallback;
import cn.rrg.com.UsbSerialControl;
import cn.rrg.devices.EmptyDeivce;
import cn.rrg.rdv.R;

public class FlasherMainActivity extends BaseActivity {

    private UsbSerialControl control = UsbSerialControl.get();
    private Proxmark3Flasher flasher = Proxmark3Flasher.getFlasher();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pm3_flasher);

        Com.initCom(control, new EmptyDeivce());

        control.setContextCallback(new ContextCallback() {
            @Override
            public Context getContext() {
                return context;
            }
        });

        control.register(this, new DevCallback<String>() {
            @Override
            public void onAttach(String dev) {

            }

            @Override
            public void onDetach(String dev) {
                flasher.closeProxmark3();
            }
        });

        findViewById(R.id.btnStatusCheck).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        flasher.isBootloaderMode();
                    }
                }).start();
            }
        });

        findViewById(R.id.btnClientClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        flasher.closeProxmark3();
                    }
                }).start();
            }
        });

        findViewById(R.id.btnClientOpen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        flasher.openProxmark3();
                    }
                }).start();
            }
        });

        findViewById(R.id.btnDeviceConnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (control.connect(null)) {
                            ToastUtil.show(context, "连接成功", false);
                        } else {
                            ToastUtil.show(context, "连接失败", false);
                        }
                    }
                }).start();
            }
        });

        findViewById(R.id.btnFlashStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String bootrom = "/storage/emulated/0/Download/bootrom.elf";
                        String fullimg = "/storage/emulated/0/Download/fullimage.elf";
                        if (!flasher.isPM3Opened()) {
                            if (!flasher.openProxmark3()) {
                                LogUtils.d("PM3打开失败!");
                                return;
                            } else {
                                LogUtils.d("PM3打开成功!");
                            }
                        }
                        // 如果没有在Bootloader模式下，我们需要先重启设备使其进入Bootloader模式下!
                        if (flasher.isBootloaderMode()) {
                            LogUtils.d("当前处于Bootloader模式下，将会开始刷机!");
                            if (flasher.flashBootRom(bootrom)) {
                                LogUtils.d("PM3刷写BootRom成功!");
                            } else {
                                LogUtils.d("PM3刷写BootRom失败!");
                            }
                            if (flasher.flashFullImg(fullimg)) {
                                LogUtils.d("PM3刷写BootRom成功!");
                            } else {
                                LogUtils.d("PM3刷写BootRom失败!");
                            }
                            flasher.flashModeClose();
                            flasher.closeProxmark3();
                        } else {
                            if (!flasher.enterBootloader()) {
                                LogUtils.d("PM3进入刷写模式失败!");
                            }
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        control.unregister(this);
    }
}
