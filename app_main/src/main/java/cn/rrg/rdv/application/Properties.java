package cn.rrg.rdv.application;

/**
 * @author DXL
 */
public class Properties {
    /*
     * 程序参数缓存...
     * 类型_模块_作用
     * */
    public static String k_pm3_delayTime = "pm3DelayTime";
    public static int v_pm3_delayTime = 50;

    public static String k_view_main_page_column_count = "mainPageColumnCount";
    public static int v_view_main_page_column_count = 4;

    //变色龙的卡槽是否使用别名!
    public static String k_chameleon_aliases_status = "chameleonSlotAliasesStatus";
    public static boolean v_chameleon_aliases_status = false;
    //变色龙的别名!
    public static String k_chameleon_aliases = "chameleonSlotAliasesName";
    public static String[] v_chameleon_aliases = new String[]{"卡1", "卡2", "卡3", "卡4", "卡5", "卡6", "卡7", "卡8",};

    //语言切换设置!
    public static String k_app_language = "language";
    public static String v_app_language = "auto";

    //读写卡历史选择的秘钥!
    public static String k_common_rw_keyfile_selected = "commomRWKeyFilesSelected";
    public static String[] v_common_rw_keyfile_selected = null;
}
