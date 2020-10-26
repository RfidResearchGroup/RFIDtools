package cn.rrg.freo;

/*
 * 标准流重定向工具!
 * */
public class IORedirector {

    static {
        //加载动态库!
        System.loadLibrary("freopen");
    }

    /**
     * 规范化类型传递!
     * STD_OUT 标准输出
     * STD_ERR 标准异常
     * STD_IN 标准输入!
     */
    public static final int STD_OUT = 0;
    public static final int STD_ERR = 1;
    public static final int STD_IN = 2;

    /**
     * 设置STDOUT、STDERR的重定向!
     *
     * @param file 文件地址，绝对路径!
     * @param type 类型，O或者E，对应{STD_OUT}、{STD_ERR}
     * @return 设置结果，可能的结果：文件不存在、文件无权限、文件被占用 or 成功!
     */
    public static native boolean setStdEO(String file, int type);

    /**
     * 设置的STD_IN重定向!
     *
     * @param file 文件地址，绝对路径!
     * @return 设置结果，可能的结果：文件不存在、文件无权限、文件被占用 or 成功!
     */
    public static native boolean setStdIN(String file);

    //清空标准输入的缓冲区，防止多次执行stop的误判!
    public static native void clearStdIN();

    /**
     * 关闭重定向，实际是关闭底层的文件!
     * * @param file 文件地址，绝对路径!
     */
    public static native boolean close(String file, int type);

    /* *//*
     * 进行重定向的流的flush,根据系统的实现不一定可用!
     * @param 0 = out, 1 = err
     * *//*
    public static native void flushStdEO(int type);

    *//*
     * 进行重定向文件的清空，与BUF的清空
     * @param type 需要被清空的标准流的类型，同上!
     * *//*
    public static native void resetStdEO(int type);*/

    /**
     * 更改工作目录，使一些相对路径的函数可以发现相关的文件
     *
     * @param path 绝对路径，最终程序的工作目录
     * @return 设置结果，如果参数不符合需求或者没有权限将会返回false，
     * 设置成功返回true
     */
    public static native boolean chdir(String path);
}
