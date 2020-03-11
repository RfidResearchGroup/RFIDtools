package cn.rrg.rdv.settings;

import java.io.File;
import java.io.IOException;

import cn.dxl.common.util.DiskKVUtil;
import cn.rrg.rdv.util.Paths;
import cn.rrg.rdv.application.Properties;

public class LanguageSettings implements BaseSetting {
    @Override
    public File getSettingsFile() {
        return new File(Paths.SETTINGS_FILE);
    }

    @Override
    public String onQuerySetting() {
        return Properties.k_app_language;
    }

    @Override
    public void onNotFound(String key) {
        //没有语言选项，我们插入默认的en
        try {
            String lan = Properties.v_app_language;
            //插入设置!
            DiskKVUtil.insertKV(Properties.k_app_language, lan, getSettingsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNormal(String[] value) {
        Properties.v_app_language = value[0];
    }
}
