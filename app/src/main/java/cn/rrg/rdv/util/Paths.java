package cn.rrg.rdv.util;

import android.os.Environment;

public interface Paths {

    //常用的相对路径文件夹 | 文件名定义!
    String EXTERNAL_STORAGE_DIRECTORY = Environment.getExternalStorageDirectory().getPath();
    String TOOLS_PATH = "NfcTools";
    String SETTINGS_PATH = "Settings";
    String MCT_PATH = "MifareClassicTool";
    String MTools_PATH = "MTools";
    String HARDNESTED_PATH = "hardnested";
    String TABLES_PATH = "tables";
    String LOG_PATH = "log";
    String PM3_PATH = "pm3";
    String COMMON_PATH = "common";
    String PN53X_PATH = "pn53x";
    String DRIVER_PATH = "driver";
    String TENCENT_PATH = "tencent";
    String WECAT_PATH = "MicroMsg/Download";
    String QQ_PATH = "QQfile_recv";
    String KEY_PATH = "keyFile";
    String DUMP_PATH = "dumpFile";
    String DEFAULT_KEYS_NAME = "default_keys.txt";
    String DEFAULT_DUMP_NAME = "空白数据.dump";
    String DEFAULT_CMD_NAME = "cmd.json";
    String DEFAULT_PN53X_CONF = "libnfc.conf";

    //绝对路径
    String TOOLS_DIRECTORY = EXTERNAL_STORAGE_DIRECTORY + "/" + TOOLS_PATH;
    String KEY_DIRECTORY = TOOLS_DIRECTORY + "/" + KEY_PATH;
    String LOG_DIRECTORY = TOOLS_DIRECTORY + "/" + LOG_PATH;

    String MCT_DIRECTORY = EXTERNAL_STORAGE_DIRECTORY + "/" + MCT_PATH;
    String MCT_DUMP_DIRECTORY = MCT_DIRECTORY + "/" + "dump-files";
    String MCT_KEYS_DIRECTORY = MCT_DIRECTORY + "/" + "key-files";

    String MTools_DIRECTORY = EXTERNAL_STORAGE_DIRECTORY + "/" + MTools_PATH;
    String MTools_DUMP_DIRECTORY = MTools_DIRECTORY + "/" + "dump";
    String MTools_KEYS_DIRECTORY = MTools_DIRECTORY + "/" + "key";

    String COMMON_DIRECTORY = TOOLS_DIRECTORY + "/" + COMMON_PATH;
    String DUMP_DIRECTORY = TOOLS_DIRECTORY + "/" + DUMP_PATH;

    String PM3_DIRECTORY = TOOLS_DIRECTORY + "/" + PM3_PATH;
    String PM3_HARDNESTED_DIRECTORY = PM3_DIRECTORY + "/" + HARDNESTED_PATH;
    String PM3_HARDNESTED_TABLES_DIRECTORY = PM3_HARDNESTED_DIRECTORY + "/" + TABLES_PATH;
    String PN53X_DIRRECTORY = TOOLS_DIRECTORY + "/" + PN53X_PATH;

    String WECAT_DIRECTORY = EXTERNAL_STORAGE_DIRECTORY + "/" + TENCENT_PATH + "/" + WECAT_PATH;
    String QQ_DIRECTORY = EXTERNAL_STORAGE_DIRECTORY + "/" + TENCENT_PATH + "/" + QQ_PATH;

    String SETTINGS_DIRECTORY = TOOLS_DIRECTORY + "/" + SETTINGS_PATH;
    String SETTINGS_FILE = SETTINGS_DIRECTORY + "/" + "set.dat";

    //一些重定向文件
    String PM3_FORWARD_O = PM3_DIRECTORY + "/" + "pm3_forward_o.txt";
    String PM3_FORWARD_E = PM3_DIRECTORY + "/" + "pm3_forward_e.txt";
    //PM3的RDV4动态按钮
    String PM3_CMD_FILE = PM3_DIRECTORY + "/" + DEFAULT_CMD_NAME;

    String PN53X_FORWARD_O = PN53X_DIRRECTORY + "/" + "pn53x_forward_o.txt";
    String PN53X_FORWARD_E = PN53X_DIRRECTORY + "/" + "pn53x_forward_e.txt";
    String PN53X_FORWARD_MF_O = PN53X_DIRRECTORY + "/" + "pn53x_forward_mf_o.txt";
    String PN53X_FORWARD_MF_E = PN53X_DIRRECTORY + "/" + "pn53x_forward_mf_e.txt";
    String COMMON_FORWARD_O = COMMON_DIRECTORY + "/" + "common_forward_o.txt";
    String COMMON_FORWARD_E = COMMON_DIRECTORY + "/" + "common_forward_e.txt";
    String COMMON_FORWARD_I = COMMON_DIRECTORY + "/" + "common_forward_i.txt";

    String DEFAULT_KEYS_FILE = KEY_DIRECTORY + "/" + DEFAULT_KEYS_NAME;
    String DEFAULT_DUMP_FILE = DUMP_DIRECTORY + "/" + DEFAULT_DUMP_NAME;
    String DRIVER_DIRECTORY = TOOLS_DIRECTORY + "/" + DRIVER_PATH;

    String PN53X_CONF_FILE = PN53X_DIRRECTORY + "/" + DEFAULT_PN53X_CONF;
}
