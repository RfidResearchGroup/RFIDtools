package cn.rrg.rdv.util;

import android.os.Environment;

import com.termux.app.TermuxService;

import java.io.File;

public class Paths {

    public static String EXTERNAL_STORAGE_DIRECTORY = Environment.getExternalStorageDirectory().getPath();
    public static String TOOLS_PATH = "NfcTools";
    public static String SETTINGS_PATH = "Settings";
    public static String MCT_PATH = "MifareClassicTool";
    public static String MTools_PATH = "MTools";
    public static String HARDNESTED_PATH = "hardnested";
    public static String LOG_PATH = "log";
    public static String PM3_PATH = "proxmark3";
    public static String COMMON_PATH = "common";
    public static String PN53X_PATH = "pn53x";
    public static String DRIVER_PATH = "driver";
    public static String TENCENT_PATH = "tencent";
    public static String WECAT_PATH = "MicroMsg/Download";
    public static String QQ_PATH = "QQfile_recv";
    public static String KEY_PATH = "keyFile";
    public static String DUMP_PATH = "dumpFile";
    public static String DEFAULT_KEYS_NAME = "default_keys.txt";
    public static String DEFAULT_DUMP_NAME = "BLANK(空白).dump";
    public static String DEFAULT_CMD_NAME = "cmd.json";

    public static String TOOLS_DIRECTORY = EXTERNAL_STORAGE_DIRECTORY + "/" + TOOLS_PATH;
    public static String KEY_DIRECTORY = TOOLS_DIRECTORY + "/" + KEY_PATH;
    public static String LOG_DIRECTORY = TOOLS_DIRECTORY + "/" + LOG_PATH;

    public static String MCT_DIRECTORY = EXTERNAL_STORAGE_DIRECTORY + "/" + MCT_PATH;
    public static String MCT_DUMP_DIRECTORY = MCT_DIRECTORY + "/" + "dump-files";
    public static String MCT_KEYS_DIRECTORY = MCT_DIRECTORY + "/" + "key-files";

    public static String MTools_DIRECTORY = EXTERNAL_STORAGE_DIRECTORY + "/" + MTools_PATH;
    public static String MTools_DUMP_DIRECTORY = MTools_DIRECTORY + "/" + "dump";
    public static String MTools_KEYS_DIRECTORY = MTools_DIRECTORY + "/" + "key";

    public static String COMMON_DIRECTORY = TOOLS_DIRECTORY + "/" + COMMON_PATH;
    public static String DUMP_DIRECTORY = TOOLS_DIRECTORY + "/" + DUMP_PATH;

    public static String PM3_DIRECTORY = TOOLS_DIRECTORY + "/" + PM3_PATH;
    public static String PN53X_DIRRECTORY = TOOLS_DIRECTORY + "/" + PN53X_PATH;

    public static String WECAT_DIRECTORY = EXTERNAL_STORAGE_DIRECTORY + "/" + TENCENT_PATH + "/" + WECAT_PATH;
    public static String QQ_DIRECTORY = EXTERNAL_STORAGE_DIRECTORY + "/" + TENCENT_PATH + "/" + QQ_PATH;

    public static String SETTINGS_DIRECTORY = TOOLS_DIRECTORY + "/" + SETTINGS_PATH;
    public static String SETTINGS_FILE = SETTINGS_DIRECTORY + "/" + "set.dat";

    public static String PM3_BOOT_FILE_NAME = "bootrom.elf";
    public static String PM3_OS_FILE_NAME = "fullimage.elf";
    public static String PM3_FORWARD_O = PM3_DIRECTORY + "/" + "pm3_forward_o.txt";
    public static String PM3_FORWARD_E = PM3_DIRECTORY + "/" + "pm3_forward_e.txt";
    //PM3 Easy Button
    public static String PM3_CMD_FILE = PM3_DIRECTORY + "/" + DEFAULT_CMD_NAME;
    // PM3 Image
    public static String PM3_IMAGE_BOOT_FILE = TermuxService.HOME_PATH + File.separator + PM3_PATH + File.separator + PM3_BOOT_FILE_NAME;
    public static String PM3_IMAGE_OS_FILE = TermuxService.HOME_PATH + File.separator + PM3_PATH + File.separator + PM3_OS_FILE_NAME;
    // pm3 cwd -> sdcard
    public static String PM3_CWD = PM3_DIRECTORY + File.separator + "home";
    public static final String PM3_CWD_FINAL = PM3_DIRECTORY + File.separator + "home";

    public static String PN53X_FORWARD_O = PN53X_DIRRECTORY + "/" + "pn53x_forward_o.txt";
    public static String PN53X_FORWARD_E = PN53X_DIRRECTORY + "/" + "pn53x_forward_e.txt";
    public static String PN53X_FORWARD_MF_O = PN53X_DIRRECTORY + "/" + "pn53x_forward_mf_o.txt";
    public static String PN53X_FORWARD_MF_E = PN53X_DIRRECTORY + "/" + "pn53x_forward_mf_e.txt";
    public static String COMMON_FORWARD_O = COMMON_DIRECTORY + "/" + "common_forward_o.txt";
    public static String COMMON_FORWARD_E = COMMON_DIRECTORY + "/" + "common_forward_e.txt";
    public static String COMMON_FORWARD_I = COMMON_DIRECTORY + "/" + "common_forward_i.txt";

    public static String DEFAULT_KEYS_FILE = KEY_DIRECTORY + "/" + DEFAULT_KEYS_NAME;
    public static String DEFAULT_DUMP_FILE = DUMP_DIRECTORY + "/" + DEFAULT_DUMP_NAME;
    public static String DRIVER_DIRECTORY = TOOLS_DIRECTORY + "/" + DRIVER_PATH;
}
