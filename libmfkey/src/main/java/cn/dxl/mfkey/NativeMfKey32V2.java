package cn.dxl.mfkey;

public class NativeMfKey32V2 implements MfKey32 {

    static {
        System.loadLibrary("mfkey32v2");
    }

    /**
     * 单独实现一个原生函数，直接做计算操作，不走控制台实现!
     *
     * @return key
     */
    public native String decrypt4IntParams(
            int uid, /* 计算的UID,整形类型 */
            int nt0,
            int nr0,
            int ar0,
            int nt1,
            int nr1,
            int ar1
    );

    /*
     * 参数重写，传入的参数进行重新转换之类的!
     * */
    public native String decrypt4StrParams(
            String uid, /* 计算的UID,16HEX字符类型 */
            String nt0,
            String nr0,
            String ar0,
            String nt1,
            String nr1,
            String ar1
    );
}
