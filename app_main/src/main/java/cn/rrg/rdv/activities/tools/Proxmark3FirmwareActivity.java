package cn.rrg.rdv.activities.tools;

import android.os.Bundle;

import androidx.annotation.Nullable;

import cn.dxl.mifare.NfcTagListenUtils;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.BaseActivity;

/**
 * Provide users with an interactive PM3 firmware upgrade interface.
 * Using a professional firmware flasher
 * {@link com.proxgrind.pm3flasher.Proxmark3Flasher}
 *
 * @author DXL
 * @version 1.0
 */
public class Proxmark3FirmwareActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pm3_custom_flasher);
        NfcTagListenUtils.removeListener(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}