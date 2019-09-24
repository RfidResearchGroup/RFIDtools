package cn.dxl.common.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * TODO 正则组匹配工具类
 * 根据给出的组标签反选或者正选内容!
 * */
public class RegexGroupUtil {

    /*public static void main(String args[]) {
        String c = "UID : 8b 36 8b eb";
        String s = matcherGroup(c, ".*UID : (.{11}).*", 1, 0);
        System.out.println("最终测试组匹配截取: " + s);
    }*/

    /**
     * @param content 被匹配的内容!
     * @param regex   匹配规则
     * @return 匹配的组!
     * @Method 匹配组多个，根据组得到内容结果!
     */
    public static String[] matcherGroups(String content, String regex, int group) {
        //准备格式匹配对象!
        Pattern p = Pattern.compile(regex);
        //准备匹配器对象!
        Matcher m = p.matcher(content);
        //结果集!
        ArrayList<String> ret = new ArrayList<>();
        //迭代查组
        while (m.find()) {
            ret.add(m.group(group));
        }
        return ret.toArray(new String[0]);
    }

    /*
     * Method 匹配一个组
     * @Param content 被匹配的内容!
     * @Param regex 被匹配的正则!
     * @Param index 组索引，位于字符串中的第一次pos
     * @Return 返回指定索引的组内容!
     * */
    public static String matcherGroup(String content, String regex, int group, int index) {
        String[] res = matcherGroups(content, regex, group);
        if (index >= res.length || index < 0) return null;
        return res[index];
    }
}
