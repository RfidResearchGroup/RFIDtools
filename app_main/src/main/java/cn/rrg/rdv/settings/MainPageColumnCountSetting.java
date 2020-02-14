package cn.rrg.rdv.settings;

import java.io.File;
import java.io.IOException;

import cn.rrg.rdv.util.Paths;
import cn.rrg.rdv.application.Properties;
import cn.dxl.common.util.DiskKVUtil;

public class MainPageColumnCountSetting implements BaseSetting {
    @Override
    public File getSettingsFile() {
        return new File(Paths.SETTINGS_FILE);
    }

    @Override
    public String onQuerySetting() {
        return Properties.k_view_main_page_column_count;
    }

    @Override
    public void onNotFound(String key) {
        //否则创建!
        try {
            DiskKVUtil.insertKV(Properties.k_view_main_page_column_count, "4", getSettingsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNormal(String[] value) {
        Properties.v_view_main_page_column_count = DiskKVUtil.toInt(value[0], 4);
    }
}
