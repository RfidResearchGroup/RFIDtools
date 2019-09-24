package cn.rrg.console.define;

/*
 * 定义命令行程序的接口!
 * */
public interface ICommandTools {
    /*＊
     * 执行一个命令，根据传入的参数!
     * @param cmd 命令行,需要被执行的命令
     * @return 程序执行命令可能返回的值
     * */
    int startExecute(String cmd);
 
    /*
     * 当前是否在执行当中!
     * @return 是否在运行当中，true or false
     * */
    boolean isExecuting();

    /*
     * 停止一个当前正在执行的命令!
     * */
    void stopExecute();
}
