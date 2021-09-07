package cn.rfidresearchgroup.chameleon.utils;

import java.util.HashMap;

import cn.rfidresearchgroup.chameleon.defined.ChameleonCMDSet;
import cn.rfidresearchgroup.chameleon.defined.ChameleonType;

/**
 * 变色龙标准命令集!
 * 实际运行的过程中变色龙是以类似AT命令的指令进行执行的。
 */
public class ChameleonCMDStr {

    /*
     * 使用表映射储存!
     * reve设备!
     * */
    private static HashMap<ChameleonCMDSet, String> reveCMDMap = new HashMap<>();

    /*
     * revg设备!
     * */
    private static HashMap<ChameleonCMDSet, String> revgCMDMap = new HashMap<>();

    static {
        //初始化Rev V
        // VERSIONMY,CONFIGMY,UIDMY,READONLYMY,UPLOADMY,DOWNLOADMY,RESETMY,UPGRADEMY,MEMSIZEMY,UIDSIZEMY,BUTTONMY,SETTINGMY,CLEARMY,HELPMY,RSSIMY
        reveCMDMap.put(ChameleonCMDSet.HELP, "helpmy");
        reveCMDMap.put(ChameleonCMDSet.GET_VERSION, "versionmy?");
        reveCMDMap.put(ChameleonCMDSet.SET_CONFIG, "configmy=%s");
        reveCMDMap.put(ChameleonCMDSet.QUERY_CONFIG, "configmy?");
        reveCMDMap.put(ChameleonCMDSet.QUERY_UID, "uidmy?");
        reveCMDMap.put(ChameleonCMDSet.SET_UID, "uidmy=%s");
        reveCMDMap.put(ChameleonCMDSet.QUERY_READONLY, "readonlymy?");
        reveCMDMap.put(ChameleonCMDSet.SET_READONLY, "readonlymy=%d");
        reveCMDMap.put(ChameleonCMDSet.UPLOAD_XMODEM, "uploadmy");
        reveCMDMap.put(ChameleonCMDSet.DOWNLOAD_XMODEM, "downloadmy");
        reveCMDMap.put(ChameleonCMDSet.RESET_DEVICE, "resetmy");
        reveCMDMap.put(ChameleonCMDSet.GET_MEMORY_SIZE, "memsizemy?");
        reveCMDMap.put(ChameleonCMDSet.GET_UID_SIZE, "uidsizemy?");
        reveCMDMap.put(ChameleonCMDSet.GET_ACTIVE_SLOT, "settingmy?");
        reveCMDMap.put(ChameleonCMDSet.SET_ACTIVE_SLOT, "settingmy=%d");
        reveCMDMap.put(ChameleonCMDSet.GET_BUTTON_CLICK, "buttonmy?");
        reveCMDMap.put(ChameleonCMDSet.SET_BUTTON_CLICK, "buttonmy=%s");
        reveCMDMap.put(ChameleonCMDSet.GET_RSSI_VOLTAGE, "rssimy?");
        reveCMDMap.put(ChameleonCMDSet.CLEAR_ACTIVE_SLOT, "clearmy");
        reveCMDMap.put(ChameleonCMDSet.DETECTION, "detectionmy?");
        //初始化Rev G
        revgCMDMap.put(ChameleonCMDSet.GET_VERSION, "VERSION?");
        revgCMDMap.put(ChameleonCMDSet.SET_CONFIG, "CONFIG=%s");
        revgCMDMap.put(ChameleonCMDSet.QUERY_CONFIG, "CONFIG?");
        revgCMDMap.put(ChameleonCMDSet.QUERY_UID, "UID?");
        revgCMDMap.put(ChameleonCMDSet.SET_UID, "UID=%s");
        revgCMDMap.put(ChameleonCMDSet.QUERY_READONLY, "READONLY?");
        revgCMDMap.put(ChameleonCMDSet.SET_READONLY, "READONLY=%d");
        revgCMDMap.put(ChameleonCMDSet.UPLOAD_XMODEM, "UPLOAD");
        revgCMDMap.put(ChameleonCMDSet.DOWNLOAD_XMODEM, "DOWNLOAD");
        revgCMDMap.put(ChameleonCMDSet.RESET_DEVICE, "RESET");
        revgCMDMap.put(ChameleonCMDSet.GET_MEMORY_SIZE, "MEMSIZE?");
        revgCMDMap.put(ChameleonCMDSet.GET_UID_SIZE, "UIDSIZE?");
        revgCMDMap.put(ChameleonCMDSet.GET_ACTIVE_SLOT, "SETTING?");
        revgCMDMap.put(ChameleonCMDSet.SET_ACTIVE_SLOT, "SETTING=%d");
        revgCMDMap.put(ChameleonCMDSet.GET_RSSI_VOLTAGE, "RSSI?");
        revgCMDMap.put(ChameleonCMDSet.CLEAR_ACTIVE_SLOT, "CLEAR");
        revgCMDMap.put(ChameleonCMDSet.UPLOAD_ENCRYPTED, null);
        revgCMDMap.put(ChameleonCMDSet.KEYAUTH, null);
        revgCMDMap.put(ChameleonCMDSet.SETKEY, null);
        revgCMDMap.put(ChameleonCMDSet.GENKEY, null);
    }

    //private static final String TAG = ChameleonCMDStr.class.getSimpleName();

    /**
     * Returns a String: if don't exists , return null
     * Note that by "format string" we mean that if there are (any) parameters to be appended to the
     * command, then this should be done by a call to:
     * String.format(Locale.ENGLISH, cmdFormatStr, IntOrStringCmdArgument);
     *
     * @param cmd cmd enum value!
     * @return Array of distinct format strings for each devices revision.
     * @ref (RevE) https://github.com/iceman1001/ChameleonMini-rebooted/wiki/Terminal-Commands
     * @ref (RevG) https://rawgit.com/emsec/ChameleonMini/master/Doc/Doxygen/html/_page__command_line.html
     */
    public static String getCMD(ChameleonCMDSet cmd, ChameleonType type) {
        switch (type) {
            case REVE:
                return reveCMDMap.get(cmd);
            case REVG:
                return revgCMDMap.get(cmd);
        }
        return null;
    }

    public static String getCMD(ChameleonCMDSet cmd, ChameleonType type, Object... format) {
        String cmdStr = getCMD(cmd, type);
        if (format != null && cmdStr != null) {
            return String.format(cmdStr, format);
        } else return cmdStr;
    }

    public static String getCMD4E(ChameleonCMDSet cmd, Object... format) {
        return getCMD(cmd, ChameleonType.REVE, format);
    }

    public static String getCMD4G(ChameleonCMDSet cmd, Object... format) {
        return getCMD(cmd, ChameleonType.REVG, format);
    }

    public static final String NODATA = "<NO-DATA>";
}
