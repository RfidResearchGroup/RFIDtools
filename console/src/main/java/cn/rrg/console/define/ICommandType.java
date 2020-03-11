package cn.rrg.console.define;

/*
 * 类型，对于控制台输出，可能有，1、密钥，2、数据，3、普通文本
 * */
public interface ICommandType {
    //是一个密钥
    boolean isKey(String output);

    //该怎么解析
    String parseKey(String output);
 
    //是一段数据
    boolean isData(String output);

    //该怎么解析
    String parseData(String output);

    //是普通文本
    boolean isText(String output);

    //该怎么解析
    Runnable parseText(String output);
}
