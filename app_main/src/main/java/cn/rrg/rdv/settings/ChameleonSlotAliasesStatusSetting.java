package cn.rrg.rdv.settings;

import java.io.File;
import java.io.IOException;

import cn.rrg.rdv.util.Paths;
import cn.rrg.rdv.application.Properties;
import cn.dxl.common.util.DiskKVUtil;

public class ChameleonSlotAliasesStatusSetting
        implements BaseSetting {
    @Override
    public File getSettingsFile() {
        return new File(Paths.SETTINGS_FILE);
    }

    @Override
    public String onQuerySetting() {
        return Properties.k_chameleon_aliases_status;
    }

    @Override
    public void onNotFound(String key) {
        try {
            DiskKVUtil.insertKV(Properties.k_chameleon_aliases_status, "false", getSettingsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNormal(String[] value) {
        Properties.v_chameleon_aliases_status = Boolean.valueOf(value[0]);
    }
}
