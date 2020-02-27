package cn.rrg.rdv.settings;

import java.io.File;

/*
 * 基础设置接口!
 * 定义了加载设置的具体步骤!
 * */
public interface BaseSetting {

    /**
     * 定义设置配置文件!
     */
    File getSettingsFile();

    /**
     * 需要查询的设置!
     *
     * @return 设置的key
     */
    String onQuerySetting();

    /**
     * 在设置未发现时!
     *
     * @param key 设置的key
     */
    void onNotFound(String key);

    /**
     * 在设置正常的时候!
     *
     * @param value 设置的值
     */
    void onNormal(String[] value);
}
