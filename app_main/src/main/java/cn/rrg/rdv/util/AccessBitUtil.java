package cn.rrg.rdv.util;

/**
 * Created by DXL on 2018/4/4.
 */

import java.util.HashMap;

public class AccessBitUtil {

    /*
     * 控制位长度，4位字节八个十六进制字符
     */
    public static final int CONTROLBIT_HEX_SIZE = 8;
    public static final int CONTROLBIT_BYTE_SIZE = 4;

    //可以实例化对象时使用的全局控制位缓存
    private byte[] mAcBytes = null;

    //静态工具库，default构造方法设为私有较好
    private AccessBitUtil() { /*This is don't need*/ }

    //有参构造方法，简化对相同控制位解析时方法调用的实现
    private AccessBitUtil(byte[] acBytes) {
        this.mAcBytes = acBytes;
    }

    /**
     * 参考文档
     * https://blog.csdn.net/liujianhua1989/article/details/72639307
     * https://www.cnblogs.com/h2zZhou/p/5250703.html
     * https://github.com/ikarus23/MifareClassicTool
     */

    /**
     * 认证后可执行下列操作：
     * 1、读数据块，读操作支持普通的数据块（0-2）与电子钱包块与尾块（trail）
     * 2、写数据块，写操作支持普通的数据块（0-2）与电子钱包块与尾块（trail）
     * 3、减值：减少数据块内的数值，并将结果保存在临时内部数据寄存器中。（仅支持电子钱包块）
     * 4、加值：增加数据块内的数值，并将结果保存在数据寄存器中。（仅支持电子钱包块）
     * 5、恢复：将数据块内容移入数据寄存器。（仅支持电子钱包块）
     * 6、转存：将临时内部数据寄存器的内容写入数值块。 （仅支持电子钱包块）
     * 枚举操作类型,依次为 -> 读卡 ， 写卡 ， 增值 ， 减值 ， 恢复 ， 转存
     */
    public static enum Operation {
        READ, WRITE, INCREMENT, DECREMENT, RESTORE, TRANSFER
    }

    //密钥的验证是有KeyA，KeyB的两种不同的选择的，
    //当控制位的条件成立时，Key也可当作鉴权值之一
    //三个枚举元素分别是 A密钥， B密钥，AB密钥，两者都没
    public static enum KeyType {
        A, B, AB, NEVER
    }

    //对于一个扇区，能操作的只有两部分，一部分是数据块，一部分是尾部块
    public static enum SectorStructure {
        DATA, TRAIL
    }

    /*
     * 对数据块，能操作的总共有“三部分”，
     * Block0和Block1和Block2,不同容量的卡块数量不同，
     * 但是对于MIFARE CLASSIC CARD的兼容性来说，能操作的，就是“三部分”
     */
    public static enum DataBlockStructure {
        BLOCK0, BLOCK1, BLOCK2, BLOCK3
    }

    //对于特殊的块3，是有三部分的分割读写区域
    //其中分别是KeyA的读写，还有KeyB的读写， 还有控制位的读写
    //因此我们要根据要操作的区域对控制位进行解析鉴权
    public static enum TrailStructure {
        KEYA, KEYB, CONTROLBIT
    }

    //对于控制位，是有C1 , C2, C3的编号
    public static enum ControlBitNum {
        C1, C2, C3
    }

    /**
     * 块具有相对于区的独立性，16扇区各自拥有独立的鉴权方式，
     * 块鉴权在控制位的约束下，对于密码的权限是依赖控制位的实现
     * 依据控制位的约束，密钥对于块进行上述操作是必须要条件成立
     * 每个数据块和尾块的读写条件均由3个bit定义，
     * 并以非取反和取反形式保存在各个区的尾块中。
     * *尾部块由C13 C23 C33三个控制位来控制，由于不是数据块，因此仅支持读写操作
     * 数据块2由C12 C22 C32三个控制位控制，由于是数据块，因此支持读写增值减值操作
     * 数据块1由C11 C21 C31三个控制位控制，由于是数据块，因此支持读写增值减值操作
     * 数据块0由C10 C20 C30三个控制位控制，由于是数据块，因此支持读写增值减值操作
     */

    /*							警告，若控制位错误，将导致扇区锁死						*/

    /**
     * 尾部块操作权限解析方法
     * 根据传入的控制字节对操作进行解析，返回对应的ENUM对象
     *
     * @param c1           控制字节1
     * @param c2           控制字节2
     * @param c3           控制字节3
     * @param opera        将要进行的操作
     * @param whereToOpera 将要操作尾部块那一部分
     */
    public static KeyType trailOperation(byte c1, byte c2, byte c3,
                                         Operation opera, TrailStructure whereToOpera) {
        //判断传入的操作类型是否正确，如果不正确则直接返回NEVER
        //特别警告！！！在尾部块，操作仅限于支持读操作与写操作
        if (opera != Operation.READ && opera != Operation.WRITE) return KeyType.NEVER;
        //为了简化鉴权过程，先执行把操作为读的鉴权
        if (opera == Operation.READ) {
            //在尾部块的操作为在KeyA部分上进行的时候的鉴权
            if (whereToOpera == TrailStructure.KEYA) {
                //当控制位为000时KeyA部分禁止读
                if (c1 == 0 && c2 == 0 && c3 == 0) return KeyType.NEVER;
                //当控制位为001时KeyA部分禁止读
                if (c1 == 0 && c2 == 0 && c3 == 1) return KeyType.NEVER;
                //当控制位为010时KeyA部分禁止读
                if (c1 == 0 && c2 == 1 && c3 == 0) return KeyType.NEVER;
                //当控制位为011时KeyA部分禁止读
                if (c1 == 0 && c2 == 1 && c3 == 1) return KeyType.NEVER;
                //当控制位为100时KeyA部分禁止读
                if (c1 == 1 && c2 == 0 && c3 == 0) return KeyType.NEVER;
                //当控制位为101时KeyA部分禁止读
                if (c1 == 1 && c2 == 0 && c3 == 1) return KeyType.NEVER;
                //当控制位为110时KeyA部分禁止读
                if (c1 == 1 && c2 == 1 && c3 == 0) return KeyType.NEVER;
                //当控制位为111时KeyA部分禁止读
                if (c1 == 1 && c2 == 1 && c3 == 1) return KeyType.NEVER;
            }
            //在尾部块的操作为在控制位部分上进行的时候的鉴权
            if (whereToOpera == TrailStructure.CONTROLBIT) {
                //当控制位为000时控制位部分允许验证密钥A后读
                if (c1 == 0 && c2 == 0 && c3 == 0) return KeyType.A;
                //当控制位为001时控制位部分允许验证密钥A后读
                if (c1 == 0 && c2 == 0 && c3 == 1) return KeyType.A;
                //当控制位为010时控制位部分允许验证密钥A后读
                if (c1 == 0 && c2 == 1 && c3 == 0) return KeyType.A;
                //当控制位为011时控制位部分允许验证密钥A|B后读
                if (c1 == 0 && c2 == 1 && c3 == 1) return KeyType.AB;
                //当控制位为100时控制位部分允许验证密钥A|B后读
                if (c1 == 1 && c2 == 0 && c3 == 0) return KeyType.AB;
                //当控制位为101时控制位部分允许验证密钥A|B后读
                if (c1 == 1 && c2 == 0 && c3 == 1) return KeyType.AB;
                //当控制位为110时控制位部分允许验证密钥A|B后读
                if (c1 == 1 && c2 == 1 && c3 == 0) return KeyType.AB;
                //当控制位为111时控制位部分允许验证密钥A|B后读
                if (c1 == 1 && c2 == 1 && c3 == 1) return KeyType.AB;
            }
            //在尾部块的操作为在KeyB部分上进行的时候的鉴权
            if (whereToOpera == TrailStructure.KEYB) {
                //当控制位为000时KeyB部分允许验证密钥A后读
                if (c1 == 0 && c2 == 0 && c3 == 0) return KeyType.A;
                //当控制位为001时KeyB部分允许验证密钥A后读
                if (c1 == 0 && c2 == 0 && c3 == 1) return KeyType.A;
                //当控制位为010时KeyB部分允许验证密钥A后读
                if (c1 == 0 && c2 == 1 && c3 == 0) return KeyType.A;
                //当控制位为011时KeyB部分禁止读
                if (c1 == 0 && c2 == 1 && c3 == 1) return KeyType.NEVER;
                //当控制位为100时KeyB部分禁止读
                if (c1 == 1 && c2 == 0 && c3 == 0) return KeyType.NEVER;
                //当控制位为101时KeyB部分禁止读
                if (c1 == 1 && c2 == 0 && c3 == 1) return KeyType.NEVER;
                //当控制位为110时KeyB部分禁止读
                if (c1 == 1 && c2 == 1 && c3 == 0) return KeyType.NEVER;
                //当控制位为111时KeyB部分禁止读
                if (c1 == 1 && c2 == 1 && c3 == 1) return KeyType.NEVER;
            }

        }
        //然后再执行操作为写的鉴权
        if (opera == Operation.WRITE) {
            //在尾部块的操作为在KeyA部分上进行的时候的鉴权
            if (whereToOpera == TrailStructure.KEYA) {
                //当控制位为000时KeyA部分允许验证密钥A后写
                if (c1 == 0 && c2 == 0 && c3 == 0) return KeyType.A;
                //当控制位为001时KeyA部分允许验证密钥A后写
                if (c1 == 0 && c2 == 0 && c3 == 1) return KeyType.A;
                //当控制位为010时KeyA部分禁止写
                if (c1 == 0 && c2 == 1 && c3 == 0) return KeyType.NEVER;
                //当控制位为011时KeyA部分允许验证密钥B后写
                if (c1 == 0 && c2 == 1 && c3 == 1) return KeyType.B;
                //当控制位为100时KeyA部分允许验证密钥B后写
                if (c1 == 1 && c2 == 0 && c3 == 0) return KeyType.B;
                //当控制位为101时KeyA部分禁止写
                if (c1 == 1 && c2 == 0 && c3 == 1) return KeyType.NEVER;
                //当控制位为110时KeyA部分禁止写
                if (c1 == 1 && c2 == 1 && c3 == 0) return KeyType.NEVER;
                //当控制位为111时KeyA部分禁止写
                if (c1 == 1 && c2 == 1 && c3 == 1) return KeyType.NEVER;
            }
            //在尾部块的操作为在控制位部分上进行的时候的鉴权
            if (whereToOpera == TrailStructure.CONTROLBIT) {
                //当控制位为000时控制位部分允许验证密钥B后写
                if (c1 == 0 && c2 == 0 && c3 == 0) return KeyType.B;
                //当控制位为001时控制位部分允许验证密钥A后写
                if (c1 == 0 && c2 == 0 && c3 == 1) return KeyType.A;
                //当控制位为010时控制位部分禁止写
                if (c1 == 0 && c2 == 1 && c3 == 0) return KeyType.NEVER;
                //当控制位为011时控制位部分允许验证密钥B后写
                if (c1 == 0 && c2 == 1 && c3 == 1) return KeyType.B;
                //当控制位为100时控制位部分禁止写
                if (c1 == 1 && c2 == 0 && c3 == 0) return KeyType.NEVER;
                //当控制位为101时控制位部分允许验证密钥B后写
                if (c1 == 1 && c2 == 0 && c3 == 1) return KeyType.B;
                //当控制位为110时控制位部分禁止写
                if (c1 == 1 && c2 == 1 && c3 == 0) return KeyType.NEVER;
                //当控制位为111时控制位部分禁止写
                if (c1 == 1 && c2 == 1 && c3 == 1) return KeyType.NEVER;
            }
            //在尾部块的操作为在KeyB部分上进行的时候的鉴权
            if (whereToOpera == TrailStructure.KEYB) {
                //当控制位为000时KeyB部分允许验证密钥A后写
                if (c1 == 0 && c2 == 0 && c3 == 0) return KeyType.A;
                //当控制位为001时KeyB部分允许验证密钥A后写
                if (c1 == 0 && c2 == 0 && c3 == 1) return KeyType.A;
                //当控制位为010时KeyB部分禁止写
                if (c1 == 0 && c2 == 1 && c3 == 0) return KeyType.NEVER;
                //当控制位为011时KeyB部分允许验证密钥B后写
                if (c1 == 0 && c2 == 1 && c3 == 1) return KeyType.B;
                //当控制位为100时KeyB部分允许验证密钥B后写
                if (c1 == 1 && c2 == 0 && c3 == 0) return KeyType.B;
                //当控制位为101时KeyB部分禁止写
                if (c1 == 1 && c2 == 0 && c3 == 1) return KeyType.NEVER;
                //当控制位为110时KeyB部分禁止写
                if (c1 == 1 && c2 == 1 && c3 == 0) return KeyType.NEVER;
                //当控制位为111时KeyB部分禁止写
                if (c1 == 1 && c2 == 1 && c3 == 1) return KeyType.NEVER;
            }
        }
        //在条件都不成立时返回NEVER
        return KeyType.NEVER;
    }

    /**
     * 数据块操作权限解析方法
     * 根据传入的控制字节对操作进行解析，返回对应的ENUM对象
     *
     * @param c1    控制字节1
     * @param c2    控制字节2
     * @param c3    控制字节3
     * @param opera 将要对数据块进行的操作
     */
    public static KeyType generalOperation(byte c1, byte c2, byte c3, Operation opera) {
        //1、进行数据块读操作的鉴权
        if (opera == Operation.READ) {
            //在控制位为000时允许密钥AB后读
            if (c1 == 0 && c2 == 0 && c3 == 0) return KeyType.AB;
            //在控制位为010时允许密钥AB后读
            if (c1 == 0 && c2 == 1 && c3 == 0) return KeyType.AB;
            //在控制位为100时允许密钥AB后读
            if (c1 == 1 && c2 == 0 && c3 == 0) return KeyType.AB;
            //在控制位为110时允许密钥AB后读
            if (c1 == 1 && c2 == 1 && c3 == 0) return KeyType.AB;
            //在控制位为001时允许密钥AB后读
            if (c1 == 0 && c2 == 0 && c3 == 1) return KeyType.AB;
            //在控制位为011时允许密钥B后读
            if (c1 == 0 && c2 == 1 && c3 == 1) return KeyType.B;
            //在控制位为101时允许密钥B后读
            if (c1 == 1 && c2 == 0 && c3 == 1) return KeyType.B;
            //在控制位为111时禁止读
            if (c1 == 1 && c2 == 1 && c3 == 1) return KeyType.NEVER;
        }
        //1、进行数据块写操作的鉴权
        if (opera == Operation.WRITE) {
            //在控制位为000时允许密钥AB后写
            if (c1 == 0 && c2 == 0 && c3 == 0) return KeyType.AB;
            //在控制位为010时允许密钥B后写
            if (c1 == 0 && c2 == 1 && c3 == 0) return KeyType.B;
            //在控制位为100时允许密钥B后写
            if (c1 == 1 && c2 == 0 && c3 == 0) return KeyType.B;
            //在控制位为110时允许密钥B后写
            if (c1 == 1 && c2 == 1 && c3 == 0) return KeyType.B;
            //在控制位为001时禁止读
            if (c1 == 0 && c2 == 0 && c3 == 1) return KeyType.NEVER;
            //在控制位为011时允许密钥B后写
            if (c1 == 0 && c2 == 1 && c3 == 1) return KeyType.B;
            //在控制位为101时禁止写
            if (c1 == 1 && c2 == 0 && c3 == 1) return KeyType.NEVER;
            //在控制位为111时禁止写
            if (c1 == 1 && c2 == 1 && c3 == 1) return KeyType.NEVER;
        }
        //3、进行数据块增值操作的鉴权
        if (opera == Operation.INCREMENT) {
            //在控制位为000时允许密钥AB后增值
            if (c1 == 0 && c2 == 0 && c3 == 0) return KeyType.AB;
            //在控制位为010时允许密钥B后增值
            if (c1 == 0 && c2 == 1 && c3 == 0) return KeyType.NEVER;
            //在控制位为100时允许密钥B后增值
            if (c1 == 1 && c2 == 0 && c3 == 0) return KeyType.NEVER;
            //在控制位为110时允许密钥B后增值
            if (c1 == 1 && c2 == 1 && c3 == 0) return KeyType.B;
            //在控制位为001时禁止增值
            if (c1 == 0 && c2 == 0 && c3 == 1) return KeyType.NEVER;
            //在控制位为011时允许密钥B后增值
            if (c1 == 0 && c2 == 1 && c3 == 1) return KeyType.NEVER;
            //在控制位为101时禁止增值
            if (c1 == 1 && c2 == 0 && c3 == 1) return KeyType.NEVER;
            //在控制位为111时禁止增值
            if (c1 == 1 && c2 == 1 && c3 == 1) return KeyType.NEVER;
        }
        //4、进行数据块减值操作的鉴权
        //5、进行数据块恢复操作的鉴权
        //6、进行数据块转存操作的鉴权
        if (opera == Operation.DECREMENT ||
                opera == Operation.RESTORE ||
                opera == Operation.TRANSFER) {
            //在控制位为000时允许密钥AB后减值转存恢复
            if (c1 == 0 && c2 == 0 && c3 == 0) return KeyType.AB;
            //在控制位为010时允许密钥B后减值转存恢复
            if (c1 == 0 && c2 == 1 && c3 == 0) return KeyType.NEVER;
            //在控制位为100时允许密钥B后减值转存恢复
            if (c1 == 1 && c2 == 0 && c3 == 0) return KeyType.NEVER;
            //在控制位为110时允许密钥AB后减值转存恢复
            if (c1 == 1 && c2 == 1 && c3 == 0) return KeyType.AB;
            //在控制位为001时允许密钥AB后减值转存恢复
            if (c1 == 0 && c2 == 0 && c3 == 1) return KeyType.AB;
            //在控制位为011时禁止减值转存恢复
            if (c1 == 0 && c2 == 1 && c3 == 1) return KeyType.NEVER;
            //在控制位为101时禁止减值转存恢复
            if (c1 == 1 && c2 == 0 && c3 == 1) return KeyType.NEVER;
            //在控制位为111时禁止减值转存恢复
            if (c1 == 1 && c2 == 1 && c3 == 1) return KeyType.NEVER;
        }
        //在条件都不成立时返回NEVER
        return KeyType.NEVER;
    }

    /**
     * 密钥B可读性判断方法
     * 返回TRUE则keyB可读并可用于存储数据的访问控制条件
     * 尾块和key A被预定义为传输配置状态。
     * 因为在传输配置状态下key B可读，
     * (KeyB可读的情况下)新卡必须用keyA认证。
     *
     * @param c1 控制字节1
     * @param c2 控制字节2
     * @param c3 控制字节3
     */
    public static boolean isKeyBReadable(byte c1, byte c2, byte c3) {
        return c1 == 0
                //在控制位为000时密钥B可读
                && (c2 == 0 && c3 == 0)
                //在控制位为010时密钥B可读
                || (c2 == 1 && c3 == 0)
                //在控制位为001时密钥B可读
                || (c2 == 0 && c3 == 1);
    }

    public static boolean isKeyBReadable(byte[] ac) {
        byte c1 = (byte) ((ac[1] & 0x80) >>> 7);
        byte c2 = (byte) ((ac[2] & 0x08) >>> 3);
        byte c3 = (byte) ((ac[2] & 0x80) >>> 7);
        return isKeyBReadable(c1, c2, c3);
    }

    /**
     * 传输配置状态检测方法
     *
     * @param c1    控制字节1
     * @param c2    控制字节2
     * @param c3    控制字节3
     * @param block 要操作的区域
     */
    public static boolean isTranspotConfiguration(byte c1, byte c2, byte c3,
                                                  SectorStructure block) {
        if (c1 == 0 && c2 == 0) {
            //操作数据块的情况下如果控制位为000则为传输配置状态
            if (block == SectorStructure.DATA && c3 == 0) return true;
            //操作尾部块的情况下如果控制位为001则为传输配置状态
            if (block == SectorStructure.TRAIL && c3 == 1) return true;
        }
        return false;
    }

    /**
     * 解析控制字节的方法
     *
     * @param controlBit 长度为4byte的控制位
     * @param block      枚举对象，用于判断要解析的块的控制位的鉴权
     *                   返回对应的区块的控制权限_对应的byte
     */
    public static HashMap<ControlBitNum, Byte> unpackAccessInfo(byte[] controlBit, DataBlockStructure block) {
        //控制位长度查错
        if (controlBit.length != CONTROLBIT_BYTE_SIZE) {
            throw new RuntimeException("The ControlBit length is error!!!");
        }
        //存结果的图表
        HashMap<ControlBitNum, Byte> result = new HashMap<>();
        //建立一个矩阵数组，存控制位换算结果
        //三个控制位，四个块
        byte[][] acMatrix = new byte[3][4];
        //验证控制位的可行性
        if ((byte) ((controlBit[1] >>> 4) & 0x0F) ==
                (byte) ((controlBit[0] ^ 0xFF) & 0x0F) &&
                (byte) (controlBit[2] & 0x0F) ==
                        (byte) (((controlBit[0] ^ 0xFF) >>> 4) & 0x0F) &&
                (byte) ((controlBit[2] >>> 4) & 0x0F) ==
                        (byte) ((controlBit[1] ^ 0xFF) & 0x0F)) {
            // C1, Block 0-3
            for (int i = 0; i < 4; i++) {
                acMatrix[0][i] = (byte) ((controlBit[1] >>> 4 + i) & 0x01);
            }
            // C2, Block 0-3
            for (int i = 0; i < 4; i++) {
                acMatrix[1][i] = (byte) ((controlBit[2] >>> i) & 0x01);
            }
            // C3, Block 0-3
            for (int i = 0; i < 4; i++) {
                acMatrix[2][i] = (byte) ((controlBit[2] >>> 4 + i) & 0x01);
            }
        }
        if (block == DataBlockStructure.BLOCK0) {
            result.put(ControlBitNum.C1, acMatrix[0][0]);
            result.put(ControlBitNum.C2, acMatrix[1][0]);
            result.put(ControlBitNum.C3, acMatrix[2][0]);
            return result;
        }
        if (block == DataBlockStructure.BLOCK1) {
            result.put(ControlBitNum.C1, acMatrix[0][1]);
            result.put(ControlBitNum.C2, acMatrix[1][1]);
            result.put(ControlBitNum.C3, acMatrix[2][1]);
            return result;
        }
        if (block == DataBlockStructure.BLOCK2) {
            result.put(ControlBitNum.C1, acMatrix[0][2]);
            result.put(ControlBitNum.C2, acMatrix[1][2]);
            result.put(ControlBitNum.C3, acMatrix[2][2]);
            return result;
        }
        if (block == DataBlockStructure.BLOCK3) {
            result.put(ControlBitNum.C1, acMatrix[0][3]);
            result.put(ControlBitNum.C2, acMatrix[1][3]);
            result.put(ControlBitNum.C3, acMatrix[2][3]);
            return result;
        }
        return null;
    }

    /**
     * 打包控制字节的方法
     *
     * @param controlBit 全部控制位的实现，当有四个对象,每个对象里有用C1 , C2 , C3
     *                   三个键值对,对应四个块的三个控制位
     *                   返回全部区块的控制权限_对应的byte（三个，不包括预留字节）
     */
    public static byte[] packAccessInfo(HashMap<DataBlockStructure, HashMap<ControlBitNum, Byte>> controlBit) {
        //建立一个矩阵数组，存控制位换算结果
        //三个控制位，四个块,从图表中取出来填充到矩阵数组中
        byte[][] acMatrix = new byte[3][4];
        //外层迭代
        for (int i = 0; i < 3; ++i) {
            //内层迭代
            for (int j = 0; j < 4; ++j) {
                acMatrix[i][j] =
                        //取出对应扇区的控制比特位封包
                        controlBit.get(DataBlockStructure.values()[j])
                                //取出封包中的比特位打包进矩阵中
                                .get(ControlBitNum.values()[i]);
            }
        }
        //开始将比特矩阵数组转化为字节数组
        for (int i = 0; i < 3; i++) {
            if (acMatrix[i].length != 4)
                // Error.
                return null;
        }
        byte[] acBytes = new byte[3];
        // Byte 6, Bit 0-3.

        acBytes[0] = (byte) ((acMatrix[0][0] ^ 0xFF) & 0x01);
        acBytes[0] |= (byte) (((acMatrix[0][1] ^ 0xFF) << 1) & 0x02);
        acBytes[0] |= (byte) (((acMatrix[0][2] ^ 0xFF) << 2) & 0x04);
        acBytes[0] |= (byte) (((acMatrix[0][3] ^ 0xFF) << 3) & 0x08);
        // Byte 6, Bit 4-7.
        acBytes[0] |= (byte) (((acMatrix[1][0] ^ 0xFF) << 4) & 0x10);
        acBytes[0] |= (byte) (((acMatrix[1][1] ^ 0xFF) << 5) & 0x20);
        acBytes[0] |= (byte) (((acMatrix[1][2] ^ 0xFF) << 6) & 0x40);
        acBytes[0] |= (byte) (((acMatrix[1][3] ^ 0xFF) << 7) & 0x80);
        // Byte 7, Bit 0-3.
        acBytes[1] = (byte) ((acMatrix[2][0] ^ 0xFF) & 0x01);
        acBytes[1] |= (byte) (((acMatrix[2][1] ^ 0xFF) << 1) & 0x02);
        acBytes[1] |= (byte) (((acMatrix[2][2] ^ 0xFF) << 2) & 0x04);
        acBytes[1] |= (byte) (((acMatrix[2][3] ^ 0xFF) << 3) & 0x08);
        // Byte 7, Bit 4-7.
        acBytes[1] |= (byte) ((acMatrix[0][0] << 4) & 0x10);
        acBytes[1] |= (byte) ((acMatrix[0][1] << 5) & 0x20);
        acBytes[1] |= (byte) ((acMatrix[0][2] << 6) & 0x40);
        acBytes[1] |= (byte) ((acMatrix[0][3] << 7) & 0x80);
        // Byte 8, Bit 0-3.
        acBytes[2] = (byte) (acMatrix[1][0] & 0x01);
        acBytes[2] |= (byte) ((acMatrix[1][1] << 1) & 0x02);
        acBytes[2] |= (byte) ((acMatrix[1][2] << 2) & 0x04);
        acBytes[2] |= (byte) ((acMatrix[1][3] << 3) & 0x08);
        // Byte 8, Bit 4-7.
        acBytes[2] |= (byte) ((acMatrix[2][0] << 4) & 0x10);
        acBytes[2] |= (byte) ((acMatrix[2][1] << 5) & 0x20);
        acBytes[2] |= (byte) ((acMatrix[2][2] << 6) & 0x40);
        acBytes[2] |= (byte) ((acMatrix[2][3] << 7) & 0x80);

        return acBytes;
    }

    /**
     * Convert the Access Condition bytes to a matrix containing the
     * resolved C1, C2 and C3 for each block.
     *
     * @param acBytes 控制位字节数组
     * @return 返回由3字节的控制位解析出来的控制位（bit）矩阵
     * null will be returned.
     */
    public static byte[][] acBytesToACMatrix(byte acBytes[]) {
        // ACs correct?
        // C1 (Byte 7, 4-7) == ~C1 (Byte 6, 0-3) and
        // C2 (Byte 8, 0-3) == ~C2 (Byte 6, 4-7) and
        // C3 (Byte 8, 4-7) == ~C3 (Byte 7, 0-3)
        byte[][] acMatrix = new byte[3][4];
        if (acBytes.length > 2 &&
                (byte) ((acBytes[1] >>> 4) & 0x0F) == (byte) ((acBytes[0] ^ 0xFF) & 0x0F) &&
                (byte) (acBytes[2] & 0x0F) == (byte) (((acBytes[0] ^ 0xFF) >>> 4) & 0x0F) &&
                (byte) ((acBytes[2] >>> 4) & 0x0F) == (byte) ((acBytes[1] ^ 0xFF) & 0x0F)) {
            // C1, Block 0-3
            for (int i = 0; i < 4; i++) {
                acMatrix[0][i] = (byte) ((acBytes[1] >>> 4 + i) & 0x01);
            }
            // C2, Block 0-3
            for (int i = 0; i < 4; i++) {
                acMatrix[1][i] = (byte) ((acBytes[2] >>> i) & 0x01);
            }
            // C3, Block 0-3
            for (int i = 0; i < 4; i++) {
                acMatrix[2][i] = (byte) ((acBytes[2] >>> 4 + i) & 0x01);
            }
            return acMatrix;
        }
        return null;
    }

    /**
     * 把矩阵控制位（比特类型的矩阵数组）转换为3字节的控制位
     *
     * @param acMatrix bit类型的控制位矩阵数组
     * @return 返回3字节的字节数组
     */
    public static byte[] acMatrixToACBytes(byte acMatrix[][]) {
        if (acMatrix != null && acMatrix.length == 3) {
            for (int i = 0; i < 3; i++) {
                if (acMatrix[i].length != 4)
                    // Error.
                    return null;
            }
        } else {
            // Error.
            return null;
        }
        byte[] acBytes = new byte[3];
        // Byte 6, Bit 0-3.
        acBytes[0] = (byte) ((acMatrix[0][0] ^ 0xFF) & 0x01);
        acBytes[0] |= (byte) (((acMatrix[0][1] ^ 0xFF) << 1) & 0x02);
        acBytes[0] |= (byte) (((acMatrix[0][2] ^ 0xFF) << 2) & 0x04);
        acBytes[0] |= (byte) (((acMatrix[0][3] ^ 0xFF) << 3) & 0x08);
        // Byte 6, Bit 4-7.
        acBytes[0] |= (byte) (((acMatrix[1][0] ^ 0xFF) << 4) & 0x10);
        acBytes[0] |= (byte) (((acMatrix[1][1] ^ 0xFF) << 5) & 0x20);
        acBytes[0] |= (byte) (((acMatrix[1][2] ^ 0xFF) << 6) & 0x40);
        acBytes[0] |= (byte) (((acMatrix[1][3] ^ 0xFF) << 7) & 0x80);
        // Byte 7, Bit 0-3.
        acBytes[1] = (byte) ((acMatrix[2][0] ^ 0xFF) & 0x01);
        acBytes[1] |= (byte) (((acMatrix[2][1] ^ 0xFF) << 1) & 0x02);
        acBytes[1] |= (byte) (((acMatrix[2][2] ^ 0xFF) << 2) & 0x04);
        acBytes[1] |= (byte) (((acMatrix[2][3] ^ 0xFF) << 3) & 0x08);
        // Byte 7, Bit 4-7.
        acBytes[1] |= (byte) ((acMatrix[0][0] << 4) & 0x10);
        acBytes[1] |= (byte) ((acMatrix[0][1] << 5) & 0x20);
        acBytes[1] |= (byte) ((acMatrix[0][2] << 6) & 0x40);
        acBytes[1] |= (byte) ((acMatrix[0][3] << 7) & 0x80);
        // Byte 8, Bit 0-3.
        acBytes[2] = (byte) (acMatrix[1][0] & 0x01);
        acBytes[2] |= (byte) ((acMatrix[1][1] << 1) & 0x02);
        acBytes[2] |= (byte) ((acMatrix[1][2] << 2) & 0x04);
        acBytes[2] |= (byte) ((acMatrix[1][3] << 3) & 0x08);
        // Byte 8, Bit 4-7.
        acBytes[2] |= (byte) ((acMatrix[2][0] << 4) & 0x10);
        acBytes[2] |= (byte) ((acMatrix[2][1] << 5) & 0x20);
        acBytes[2] |= (byte) ((acMatrix[2][2] << 6) & 0x40);
        acBytes[2] |= (byte) ((acMatrix[2][3] << 7) & 0x80);

        return acBytes;
    }

    /**
     * 根据一个控制位来进行所有的操作时使用的对象
     *
     * @param acBytes 控制位字节,四字节的控制位字节数组
     * @return 返回this
     */
    public static AccessBitUtil operaOneAc(byte[] acBytes) {
        return new AccessBitUtil(acBytes);
    }

    /**
     * 重载unpackAccessInfo，简化调用
     */
    public HashMap<ControlBitNum, Byte> unpackAccessInfo(DataBlockStructure block) {
        return unpackAccessInfo(mAcBytes, block);
    }

    /**
     * 重载unpackAccessInfo，简化调用
     */
    public HashMap<DataBlockStructure, HashMap<ControlBitNum, Byte>> unpackAccessInfo() {
        DataBlockStructure[] blocks = {DataBlockStructure.BLOCK0,
                DataBlockStructure.BLOCK1,
                DataBlockStructure.BLOCK2,
                DataBlockStructure.BLOCK3};
        HashMap<DataBlockStructure, HashMap<ControlBitNum, Byte>> result = new HashMap<DataBlockStructure, HashMap<ControlBitNum, Byte>>();
        for (int i = 0; i < 4; ++i) {
            result.put(blocks[i], unpackAccessInfo(blocks[i]));
        }
        return result;
    }
}