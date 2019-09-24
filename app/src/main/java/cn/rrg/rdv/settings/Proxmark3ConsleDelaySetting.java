package cn.rrg.rdv.settings;

import java.io.File;
import java.io.IOException;

import cn.rrg.rdv.util.Paths;
import cn.rrg.rdv.application.Properties;
import cn.dxl.common.util.DiskKVUtil;

public class Proxmark3ConsleDelaySetting implements BaseSetting {
    @Override
    public File getSettingsFile() {
        return new File(Paths.SETTINGS_FILE);
    }

    @Override
    public String onQuerySetting() {
        return Properties.k_pm3_delayTime;
    }

    @Override
    public void onNotFound(String key) {
        //否则创建!
        try {
            DiskKVUtil.insertKV(key, "50", getSettingsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNormal(String[] value) {
        Properties.v_pm3_delayTime = DiskKVUtil.toInt(value[0], 50);
    }
}
