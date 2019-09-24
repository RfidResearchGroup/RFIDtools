package cn.rrg.chameleon.executor;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cn.dxl.common.posixio.Communication;
import cn.dxl.common.util.HexUtil;
import cn.rrg.chameleon.utils.ChameleonResult;
import cn.rrg.chameleon.defined.IChameleonExecutor;

/**
 * @author DXL
 * 执行器实现类，具体封装了执行和获得返回值的函数!
 */
public class ChameleonExecutor implements IChameleonExecutor {

    private static final String LOG_TAG = ChameleonExecutor.class.getSimpleName();
    private static final Object lock = new Object();
    private static Communication mCom;
    private static ChameleonExecutor thiz;

    /**
     * 设备初始化方法!
     *
     * @param com 传入的串口通信接口!
     */
    @Override
    public boolean initExecutor(Communication com) {
        mCom = com;
        //判断一下设备是否可以正常开启关闭!
        byte[] result = thiz.requestChameleon("versionmy?", 1000, false);
        return ChameleonResult.isCommandResponse(result);
    }

    /**
     * @return 单例
     */
    public static ChameleonExecutor get() {
        synchronized (lock) {
            if (thiz == null)
                thiz = new ChameleonExecutor();
        }
        return thiz;
    }

    /**
     * @param at      指令，ascii编码集，需要在后缀附带\r换行!
     * @param timeout 超时，多久之后接收不到完整的数据帧自动返回?
     * @return 设备的返回信息，可能超时(无返回值),如果超时则返回null，否则返回对应命令的应答!
     */
    @Override
    public byte[] requestChameleon(String at, int timeout, boolean xmodemMode) {
        try {
            synchronized (lock) {
                //请求并且判断结果!
                if (requestChameleon(at, timeout) == -1) return null;
                //初始化必须的变量
                ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
                long currentTime = System.currentTimeMillis();
                //Log.d(LOG_TAG, "得到锁成功，当前线程ID: " + Thread.currentThread().getId());
                do {
                    //开始接收，每次接收一个字节!
                    byte tmpByte = read(1);
                    if (tmpByte != -1) {
                        //接收完毕,有有效的字节!!!
                        bos.write(tmpByte);
                        //Log.d(LOG_TAG, "打印接收到的字节: " + HexUtil.toHexString(tmpByte));
                        //有有效数据，进行超时拖延!
                        currentTime = System.currentTimeMillis();
                        //判断到换行，则可能是一帧的结束!
                        // FIXME: 2019/4/21 谨记，上传或下载将会打开xmodem通道，此时应当进行判断，断定下一步的操作!
                        if (tmpByte == 0x0A) {
                            //Log.d(LOG_TAG, "有换行，下一步判断是否需要继续接收!");
                            if (!xmodemMode) {
                                //延迟判断新行，200ms延迟最大限度提升成功率!!!
                                tmpByte = read(50);
                                if (tmpByte != -1) {
                                    //Log.d(LOG_TAG, "需要");
                                    bos.write(tmpByte);
                                    currentTime = System.currentTimeMillis();
                                } else {
                                    //Log.d(LOG_TAG, "不需要");
                                    //接收完毕，直接返回!
                                    //Log.d(LOG_TAG, "锁释放完成: " + Thread.currentThread().getId());
                                    return bos.toByteArray();
                                }
                            } else {
                                return bos.toByteArray();
                            }
                        }
                    }
                } while (System.currentTimeMillis() - currentTime < timeout);   //超时中处理!
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "104锁释放完成: " + Thread.currentThread().getId());
            return null;
        }
        Log.d(LOG_TAG, "107锁释放完成: " + Thread.currentThread().getId());
        return null;
    }

    /**
     * 在指定的时间内接收指定的长度的值，
     *
     * @param timeout 超时值
     * @param length  欲接收的长度!
     * @return 接收结果!
     */
    @Override
    public byte[] requestChameleon(int timeout, int length) {
        byte[] ret = new byte[length];
        int pos = 0;
        long currentTime = System.currentTimeMillis();
        //Log.d(LOG_TAG, "得到锁成功，当前线程ID: " + Thread.currentThread().getId());
        do {
            //开始接收，每次接收一个字节!
            byte tmpByte = read(1);
            if (tmpByte != -1) {
                ret[pos] = tmpByte;
                if (++pos == length) break;
            }
        } while (System.currentTimeMillis() - currentTime < timeout);   //超时中处理!
        return ret;
    }

    /**
     * @param at      指令，ascii编码集，需要在后缀附带\r换行!
     * @param timeout 超时，多久之后接收不到完整的数据帧自动返回?
     * @return 发送成功的字节数，如果发送失败则返回 -1
     */
    @Override
    public int requestChameleon(String at, int timeout) throws IOException {
        if (at == null) return -1;
        //Log.d(LOG_TAG, "尝试得到锁，当前线程ID: " + Thread.currentThread().getId());
        //发送命令必须回应，否则系命令错误!
        at = checkAT(at);
        byte[] sendBuf = HexUtil.getAsciiBytes(at);
        return mCom.write(sendBuf, 0, sendBuf.length, timeout);
    }

    /**
     * @param at 指令!
     * @return 如果命令带\r后缀，则直接返回，否则添加!
     */
    private String checkAT(String at) {
        return at.endsWith("\r") ? at : (String.format("%s\r", at));
    }

    /**
     * 读取一个字节，简化读取!
     *
     * @param timeout 超时值
     * @return 读取结果，-1为失败!
     */
    private byte read(int timeout) {
        byte[] b = new byte[1];
        try {
            int len = mCom.read(b, 0, 1, timeout);
            if (len != -1) {
                return b[0];
            }
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 得到通信端口!
     *
     * @return 通信端口!
     */
    @Override
    public Communication getCom() {
        Log.w(LOG_TAG, "警告，直接操作COM端口可能产生通信数据串行干扰的问题，请务必保证同时操作com的只有一个Thread: " + Thread.currentThread().getId());
        return mCom;
    }

    /**
     * 清除可能缓存的数据!
     *
     * @param timeout 超时值
     * @return 被清除的字节个数
     */
    @Override
    public int clear(int timeout) {
        int count = 0;
        long currentTime = System.currentTimeMillis();
        //Log.d(LOG_TAG, "得到锁成功，当前线程ID: " + Thread.currentThread().getId());
        do {
            //开始接收，每次接收一个字节!
            byte tmpByte = read(1);
            if (tmpByte != -1) {
                ++count;
            }
        } while (System.currentTimeMillis() - currentTime < timeout);   //超时中处理!
        return count;
    }
}
