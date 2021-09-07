package com.rfidresearchgroup.settings;

import android.util.Log;

import com.rfidresearchgroup.util.Paths;

import java.io.File;
import java.io.IOException;

import com.rfidresearchgroup.application.Properties;
import com.rfidresearchgroup.common.util.DiskKVUtil;

public class ChameleonSlotAliasesSetting implements BaseSetting {
    @Override
    public File getSettingsFile() {
        return new File(Paths.SETTINGS_FILE);
    }

    @Override
    public String onQuerySetting() {
        return Properties.k_chameleon_aliases;
    }

    @Override
    public void onNotFound(String key) {
        try {
            String[] aliases = Properties.v_chameleon_aliases;
            for (String alias : aliases) {
                //插入设置!
                DiskKVUtil.insertKV(Properties.k_chameleon_aliases, alias, getSettingsFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNormal(String[] value) {
        //判断是否足够可用!
        if (value.length < 8) {
            Log.d("onNormal", "卡槽别名参数不足!");
        } else {
            //更新到全局的参数中!
            Properties.v_chameleon_aliases = value;
        }
    }
}
