package cn.rrg.chameleon.utils;

import java.util.HashMap;

import cn.rrg.chameleon.defined.ChameleonCMDSet;
import cn.rrg.chameleon.defined.ChameleonType;

import static cn.rrg.chameleon.defined.ChameleonCMDSet.CLEAR_ACTIVE_SLOT;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.DETECTION;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.DOWNLOAD_XMODEM;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.GENKEY;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.GET_ACTIVE_SLOT;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.GET_BUTTON_CLICK;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.GET_MEMORY_SIZE;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.GET_RSSI_VOLTAGE;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.GET_UID_SIZE;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.GET_VERSION;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.HELP;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.KEYAUTH;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.QUERY_CONFIG;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.QUERY_READONLY;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.QUERY_UID;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.RESET_DEVICE;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.SETKEY;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.SET_ACTIVE_SLOT;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.SET_BUTTON_CLICK;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.SET_CONFIG;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.SET_READONLY;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.SET_UID;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.UPLOAD_ENCRYPTED;
import static cn.rrg.chameleon.defined.ChameleonCMDSet.UPLOAD_XMODEM;

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
        reveCMDMap.put(HELP, "helpmy");
        reveCMDMap.put(GET_VERSION, "versionmy?");
        reveCMDMap.put(SET_CONFIG, "configmy=%s");
        reveCMDMap.put(QUERY_CONFIG, "configmy?");
        reveCMDMap.put(QUERY_UID, "uidmy?");
        reveCMDMap.put(SET_UID, "uidmy=%s");
        reveCMDMap.put(QUERY_READONLY, "readonlymy?");
        reveCMDMap.put(SET_READONLY, "readonlymy=%d");
        reveCMDMap.put(UPLOAD_XMODEM, "uploadmy");
        reveCMDMap.put(DOWNLOAD_XMODEM, "downloadmy");
        reveCMDMap.put(RESET_DEVICE, "resetmy");
        reveCMDMap.put(GET_MEMORY_SIZE, "memsizemy?");
        reveCMDMap.put(GET_UID_SIZE, "uidsizemy?");
        reveCMDMap.put(GET_ACTIVE_SLOT, "settingmy?");
        reveCMDMap.put(SET_ACTIVE_SLOT, "settingmy=%d");
        reveCMDMap.put(GET_BUTTON_CLICK, "buttonmy?");
        reveCMDMap.put(SET_BUTTON_CLICK, "buttonmy=%s");
        reveCMDMap.put(GET_RSSI_VOLTAGE, "rssimy?");
        reveCMDMap.put(CLEAR_ACTIVE_SLOT, "clearmy");
        reveCMDMap.put(DETECTION, "detectionmy?");
        //初始化Rev G
        revgCMDMap.put(GET_VERSION, "VERSION?");
        revgCMDMap.put(SET_CONFIG, "CONFIG=%s");
        revgCMDMap.put(QUERY_CONFIG, "CONFIG?");
        revgCMDMap.put(QUERY_UID, "UID?");
        revgCMDMap.put(SET_UID, "UID=%s");
        revgCMDMap.put(QUERY_READONLY, "READONLY?");
        revgCMDMap.put(SET_READONLY, "READONLY=%d");
        revgCMDMap.put(UPLOAD_XMODEM, "UPLOAD");
        revgCMDMap.put(DOWNLOAD_XMODEM, "DOWNLOAD");
        revgCMDMap.put(RESET_DEVICE, "RESET");
        revgCMDMap.put(GET_MEMORY_SIZE, "MEMSIZE?");
        revgCMDMap.put(GET_UID_SIZE, "UIDSIZE?");
        revgCMDMap.put(GET_ACTIVE_SLOT, "SETTING?");
        revgCMDMap.put(SET_ACTIVE_SLOT, "SETTING=%d");
        revgCMDMap.put(GET_RSSI_VOLTAGE, "RSSI?");
        revgCMDMap.put(CLEAR_ACTIVE_SLOT, "CLEAR");
        revgCMDMap.put(UPLOAD_ENCRYPTED, null);
        revgCMDMap.put(KEYAUTH, null);
        revgCMDMap.put(SETKEY, null);
        revgCMDMap.put(GENKEY, null);
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
