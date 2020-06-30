package cn.rrg.rdv.activities.tools;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.iobridges.com.LocalComBridgeAdapter;
import com.proxgrind.pm3flasher.Proxmark3Flasher;
import com.proxgrind.pm3flasher.Target;

import java.io.File;

import cn.dxl.common.util.FileUtils;
import cn.dxl.common.widget.ToastUtil;
import cn.dxl.mifare.NfcTagListenUtils;
import cn.proxgrind.com.DevCallback;
import cn.proxgrind.com.UsbSerialControl;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.util.Commons;
import cn.rrg.rdv.util.Paths;
import cn.rrg.rdv.util.Proxmark3Installer;
import cn.rrg.rdv.widget.MaterialAlertDialog;
import cn.rrg.rdv.widget.ProDialog1;

/**
 * Provide users with an interactive PM3 firmware upgrade interface.
 * Using a professional firmware flasher
 * {@link com.proxgrind.pm3flasher.Proxmark3Flasher}
 *
 * @author DXL
 * @version 1.0
 */
public class Proxmark3FirmwareActivity extends BaseActivity implements DevCallback<String> {

    private TextView txtShowClientVersion;

    private RadioGroup rdoGroupImageSource;
    private RadioButton rdoBtnFromApp;
    private RadioButton rdoBtnFromUser;

    private EditText edtShowBootRomName;
    private EditText edtShowFullImageName;

    private Button btnSelectBootRom;
    private Button btnSelectFullImage;

    private Button btnFlash;

    private View layout_image_select;

    private enum MODE {
        BOOT, OS
    }

    private MODE selectImage = MODE.BOOT;
    private MODE flashImage = MODE.BOOT;

    private UsbSerialControl control = UsbSerialControl.get();
    private Proxmark3Flasher flasher = Proxmark3Flasher.getFlasher();

    private ProDialog1 proDialog1;

    private boolean isFlashing = false;

    // The image path for bt & fi.
    private String bootRomImagePath;
    private String fullImagePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pm3_custom_flasher);
        NfcTagListenUtils.removeListener(this);

        // init and try to connect.
        control.register(this);

        // start communication forward!
        LocalComBridgeAdapter.getInstance()
                .startServer(LocalComBridgeAdapter.NAMESPACE_DEFAULT);

        proDialog1 = new ProDialog1(context);

        if (!Commons.isElfDecompressed()) {
            Proxmark3Installer.installIfNeed(this, null);
        }

        initViews();
        initActions();
    }

    private void initViews() {
        txtShowClientVersion = findViewById(R.id.txtShowClientVersion);
        txtShowClientVersion.setText(Commons.PM3_CLIENT_VERSION);

        rdoGroupImageSource = findViewById(R.id.rdoGroupImageSource);
        rdoBtnFromApp = findViewById(R.id.rdoBtnFromApp);
        rdoBtnFromUser = findViewById(R.id.rdoBtnFromUser);

        edtShowBootRomName = findViewById(R.id.edtShowBootRomName);
        edtShowFullImageName = findViewById(R.id.edtShowFullImageName);

        btnSelectBootRom = findViewById(R.id.btnSelectBootRom);
        btnSelectFullImage = findViewById(R.id.btnSelectFullImage);

        btnFlash = findViewById(R.id.btnFlash);

        layout_image_select = findViewById(R.id.layout_image_select);
    }

    private void initActions() {
        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rdoGroupImageSource.getCheckedRadioButtonId() == R.id.rdoBtnFromApp) {
                    flashImage = MODE.BOOT;
                    // init path
                    bootRomImagePath = Paths.PM3_IMAGE_BOOT_FILE;
                    fullImagePath = Paths.PM3_IMAGE_OS_FILE;
                    // init status
                    isFlashing = true;
                    connectAndFlash();
                } else {
                    bootRomImagePath = edtShowBootRomName.getText().toString();
                    fullImagePath = edtShowFullImageName.getText().toString();
                    if (bootRomImagePath.length() > 0 || fullImagePath.length() > 0) {
                        // init status
                        isFlashing = true;
                        if (bootRomImagePath.length() <= 0) {
                            flashImage = MODE.OS;
                        } else {
                            flashImage = MODE.BOOT;
                        }
                        connectAndFlash();
                    } else {
                        new MaterialAlertDialog.Builder(context)
                                .setMessage(getString(R.string.tips_image_select_bt_fi))
                                .show();
                    }
                }
            }
        });

        btnSelectBootRom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage = MODE.BOOT;
                selectImage();
            }
        });

        btnSelectFullImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage = MODE.OS;
                selectImage();
            }
        });

        rdoGroupImageSource.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rdoBtnFromApp:
                        layout_image_select.setVisibility(View.GONE);
                        break;
                    case R.id.rdoBtnFromUser:
                        layout_image_select.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
        rdoGroupImageSource.check(R.id.rdoBtnFromApp);
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, 0x77);
    }

    private void connectAndFlash() {
        // connect device
        if (control.connect(null)) {
            LocalComBridgeAdapter.getInstance()
                    .setInputStream(control.getInput())
                    .setOutputStream(control.getOutput());
            flashDefaultFW();
        } else {
            ToastUtil.show(context, getString(R.string.tips_device_no_found), false);
            isFlashing = false;
        }
    }

    private void flashDefaultFW() {
        if (!proDialog1.isShowing())
            proDialog1.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!flasher.isStart(Target.CLIENT)) {
                    if (!flasher.start(Target.CLIENT)) {
                        ToastUtil.show(context, "Client open failed!", false);
                        proDialog1.dismiss();
                        return;
                    }
                }
                // 如果没有在Bootloader模式下，我们需要先重启设备使其进入Bootloader模式下!
                if (flasher.isStart(Target.BOOT)) {
                    switch (flashImage) {
                        case BOOT:
                            if (bootRomImagePath != null && FileUtils.isFile(bootRomImagePath)) {
                                if (flasher.flashBootRom(bootRomImagePath)) {
                                    ToastUtil.show(context, getString(R.string.tips_boot_flash_success), false);
                                } else {
                                    ToastUtil.show(context, getString(R.string.tips_boot_flash_failed), false);
                                }
                                finishFlash();
                                if (fullImagePath != null && FileUtils.isFile(fullImagePath)) {
                                    // 开启下一轮!
                                    flashImage = MODE.OS;
                                    proDialog1.setTips(getString(R.string.tips_bootrom_flash_finish));
                                } else {
                                    isFlashing = false;
                                    proDialog1.dismiss();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            new MaterialAlertDialog.Builder(context)
                                                    .setMessage(getString(R.string.tips_bootrom_flash_successfully))
                                                    .show();
                                        }
                                    });
                                }
                            }
                            break;

                        case OS:
                            if (fullImagePath != null && FileUtils.isFile(fullImagePath)) {
                                proDialog1.setTips(getString(R.string.tips_fullimage_flashing));
                                String msg;
                                if (flasher.flashFullImg(fullImagePath)) {
                                    msg = getString(R.string.tips_os_flash_success);
                                } else {
                                    msg = getString(R.string.tips_os_flash_failed);
                                }
                                finishFlash();
                                isFlashing = false;
                                proDialog1.dismiss();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new MaterialAlertDialog.Builder(context)
                                                .setMessage(msg)
                                                .show();
                                    }
                                });
                            }
                            break;
                    }
                } else {
                    if (!flasher.start(Target.BOOT)) {
                        ToastUtil.show(context, getString(R.string.tips_pm3_enter_failed), false);
                        proDialog1.dismiss();
                        isFlashing = false;
                    }
                    // 记得关闭客户端
                    flasher.close(Target.CLIENT);
                }
            }
        }).start();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x77) {
            if (data != null) {
                Uri uri = data.getData();
                String name = FileUtils.getFileNameByUri(context, uri, false);
                if (resultCode == RESULT_OK) {
                    // create a temp file.
                    File tmpImage = Commons.createTmpFile(name);
                    // copy content to temp file
                    FileUtils.copy(uri, tmpImage);
                    // set path to view!
                    if (selectImage == MODE.BOOT) {
                        edtShowBootRomName.setText(tmpImage.getPath());
                    } else {
                        edtShowFullImageName.setText(tmpImage.getPath());
                    }
                }
                if (resultCode == RESULT_CANCELED) {
                    ToastUtil.show(context, getString(R.string.cancel), false);
                }
            }
        }
    }

    @Override
    public void onAttach(String dev) {
        // 自动连接设备!
        if (control.connect(dev)) {
            // 更新流引用!
            LocalComBridgeAdapter.getInstance()
                    .setInputStream(control.getInput())
                    .setOutputStream(control.getOutput());
            if (isFlashing) {
                flashDefaultFW();
            }
        } else {
            ToastUtil.show(context, "Device connect failed!", false);
        }
    }

    @Override
    public void onDetach(String dev) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        proDialog1.dismiss();
        control.unregister();
        closeClient();
        LocalComBridgeAdapter.getInstance()
                .stopClient();
    }
}