package cn.rrg.rdv.application;

/**
 * @author DXL
 */
public class Properties {
    //变色龙的卡槽是否使用别名!
    public static String k_chameleon_aliases_status = "chameleonSlotAliasesStatus";
    public static boolean v_chameleon_aliases_status = false;
    //变色龙的别名!
    public static String k_chameleon_aliases = "chameleonSlotAliasesName";
    public static String[] v_chameleon_aliases = new String[]{"卡1", "卡2", "卡3", "卡4", "卡5", "卡6", "卡7", "卡8",};

    //语言切换设置!
    public static String k_app_language = "language";
    //Key files select history!
    public static String k_common_rw_keyfile_selected = "commonRWKeyFilesSelected";
    // Auto Goto terminal View
    public static String k_auto_goto_terminal = "autoGotoTermuxView";
    // The type of terminal
    public static String k_terminal_type = "terminal_type";
    // The enable status of p3m external work directory
    public static String k_pm3_externl_cwd_enable = "pm3_cwd_external_enable";
}
