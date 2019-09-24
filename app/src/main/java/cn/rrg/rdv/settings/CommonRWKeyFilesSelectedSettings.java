package cn.rrg.rdv.settings;

import java.io.File;
import java.io.IOException;

import cn.dxl.common.util.DiskKVUtil;
import cn.rrg.rdv.application.Properties;
import cn.rrg.rdv.util.Paths;

public class CommonRWKeyFilesSelectedSettings implements BaseSetting {
    @Override
    public File getSettingsFile() {
        return new File(Paths.SETTINGS_FILE);
    }

    @Override
    public String onQuerySetting() {
        return Properties.k_common_rw_keyfile_selected;
    }

    @Override
    public void onNotFound(String key) {
        try {
            DiskKVUtil.insertKV(Properties.k_common_rw_keyfile_selected, "", getSettingsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNormal(String[] value) {
        // 判断一下值是否有效!
        for (String v : value) {
            File file = new File(v);
            if (!file.exists()) {
                // 参数所指文件不存在，删除这个键值对!
                try {
                    DiskKVUtil.deleteKV(Properties.k_common_rw_keyfile_selected, v, getSettingsFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Properties.v_common_rw_keyfile_selected = value;
    }
}
