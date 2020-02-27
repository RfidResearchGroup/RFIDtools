package cn.rrg.rdv.application;

/*
 * 这是一个存放静态资源的类，可以存放需要被全局共享的值
 * */
public class RuntimeProperties {
    //当前使用的PM3客户端版本!
    public static String PM3_CLIENT_VERSION = "RRG/Iceman// 2020/02/11";

    //是否已经连接到设备
    public static boolean isConnected = false;
}
