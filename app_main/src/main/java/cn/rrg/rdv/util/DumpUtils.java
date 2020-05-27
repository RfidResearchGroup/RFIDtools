package cn.rrg.rdv.util;

import android.net.Uri;
import android.nfc.tech.MifareClassic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cn.dxl.common.util.FileUtils;
import cn.dxl.common.util.HexUtil;
import cn.dxl.common.util.RegexGroupUtil;
import cn.dxl.mifare.MifareClassicUtils;
import cn.rrg.rdv.javabean.M1Bean;
import cn.rrg.rdv.javabean.M1KeyBean;

/*Dump文件操作类封装*/
public class DumpUtils {

    private static final String LOG_TAG = DumpUtils.class.getSimpleName();

    //默认的空数据!
    public static final String BLANK_DATA = "00000000000000000000000000000000";
    // 默认的秘钥
    public static final String BLANK_KEY = "FFFFFFFFFFFF";
    // 默认的尾部块
    public static final String BLANK_TRAIL = "FF078069";
    // 空的尾部块!
    public static final String BLANK_TRAIL_BLOCK = BLANK_KEY + BLANK_TRAIL + BLANK_KEY;
    //没有密钥时的填充
    public static final String NO_KEY = "*FFFFFFFFFFF";
    //没有数据时的填充
    public static final String NO_DAT = "*0000000000000000000000000000000";
    //没有尾部块的时候的填充
    public static final String NO_TRAIL = "*F078069";
    //默认的空尾部块!
    public static final String NO_TRAIL_BLOCK = NO_KEY + NO_TRAIL + NO_KEY;

    // 默认秘钥!
    public final static String[] KEY_DEFAULT = new String[]{
            "FFFFFFFFFFFF",
            "A0A1A2A3A4A5",
            "D3F7D3F7D3F7",
            "000000000000",
            "A0B0C0D0E0F0",
            "A1B1C1D1E1F1",
            "B0B1B2B3B4B5",
            "4D3A99C351DD",
            "1A982C7E459A",
            "AABBCCDDEEFF"
    };

    public final static byte[][] KEY_DEFAULT_BYTE = mergeHexKeys(KEY_DEFAULT);

    public final static long MAX_DUMP_FILE_SIZE = FileUtils.Size.KB * 10;

    /*
     * 由于卡的种类问题，扇区数不能固定，块数量也不能固定
     * 因此，不可以写死扇区和数据块,并且，数据一定要完整!!!
     * */

    private DumpUtils() {
        //no instance
    }

    /*
     * 目前已知的数据格式
     * S50的结构和S70的结构
     * 我们需要知道，块的数据格式总是16字节的长度
     * 但是，S50和S70的卡由于扇区数量不同，因此，不能单纯判断块的长度
     * S50: 64个块，64 * 16 = 1024b
     * S70: 256个块，256 * 16 = 4096b
     * */
    public static final int TYPE_TXT = 1;
    public static final int TYPE_BIN = 2;
    public static final int TYPE_NOT = -1;

    // 将秘钥字符串数组转换为秘钥字节数组!
    public static byte[][] mergeHexKeys(String[] hexKeys) {
        byte[][] ret = new byte[hexKeys.length][];
        for (int i = 0; i < ret.length; i++) {
            // 将字符串转为字节!
            ret[i] = HexUtil.hexStringToByteArray(hexKeys[i]);
        }
        return ret;
    }

    //判断是否是注释行
    public static boolean isAnnotaion(String str) {
        return str.trim().startsWith("#");
    }

    // 根据扇区号来获取相应的空扇区!
    public static M1Bean getEmptyM1Bean(int sector) {
        M1Bean ret = new M1Bean();
        int blockCount = MifareClassicUtils.getBlockCountInSector(sector);
        String[] datas = new String[blockCount];
        for (int i = 0; i < blockCount; i++) {
            datas[i] = i < (blockCount - 1) ? BLANK_DATA : BLANK_TRAIL_BLOCK;
        }
        ret.setDatas(datas);
        ret.setSector(sector);
        return ret;
    }

    //判断是否是密钥格式
    public static boolean isKeyFormat(String key) {
        return HexUtil.isHexString(key) && key.length() == 12;
    }

    // 是否是正常的块数据!
    public static boolean isBlocksValids(String[] datas) {
        switch (datas.length) {
            case MifareClassic.SIZE_1K / MifareClassic.BLOCK_SIZE:
            case MifareClassic.SIZE_2K / MifareClassic.BLOCK_SIZE:
            case MifareClassic.SIZE_4K / MifareClassic.BLOCK_SIZE:
                return true;
        }
        // LogUtils.d("检测的长度: " + datas.length);
        return false;
    }

    //提取密钥
    public static String[] extractKeys(M1Bean[] datas) {
        ArrayList<String> ret = new ArrayList<>();
        //迭代当前的bean。
        for (M1Bean b : datas) {
            //跳过无效的bean包!
            if (b == null) continue;
            //得到其中的数据封包
            String[] dataArr = b.getDatas();
            //跳过无效的数据封包!
            if (dataArr == null) continue;
            //判断块的长度，必须正确!
            if (dataArr.length == 4 || dataArr.length == 16) {
                //得到尾部的数据块!
                String lastBlock = dataArr[dataArr.length - 1];
                //判断尾部块的数据是否有效!
                if (lastBlock == null || (lastBlock.length() != 32)) continue;
                //开始提取A密钥!
                String keyA = lastBlock.substring(0, 12);
                //开始提取密钥B！
                String keyB = lastBlock.substring(20, 32);
                //判断密钥有效性，酌情提取!
                if (isKeyFormat(keyA)) {
                    ret.add(keyA);
                }
                if (isKeyFormat(keyB)) {
                    ret.add(keyB);
                }
            }
        }
        return ret.toArray(new String[0]);
    }

    /*
     * BCC获取
     * */
    public static byte calcBCC(byte[] uid) {
        if (uid.length != 4) {
            return -1;
        }
        byte bcc = uid[0];
        for (int i = 1; i < uid.length; i++) bcc = (byte) (bcc ^ uid[i]);
        return bcc;
    }

    /*
     * BCC有效性判断!
     * */
    public static boolean isBCCVaild(String uidAndBcc) {
        if (uidAndBcc.length() != 10) return false;
        String bccStr = uidAndBcc.substring(8, 10);
        byte bcc = HexUtil.hexStringToByteArray(bccStr)[0];
        String uidStr = uidAndBcc.substring(0, 8);
        byte[] uidBytes = HexUtil.hexStringToByteArray(uidStr);
        return calcBCC(uidBytes) == bcc;
    }

    /*
     * 分离字符串，以换行符分割
     * */
    public static String[] splitDump(String dump) {
        //判断dump来源
        if (isUnixLFFormat(dump)) {
            //unix类系统
            return dump.split(getSystemLF("unix"));
        } else {
            return dump.split(getSystemLF("windows"));
        }
    }

    /*
     * 判断是不是原生的数据文件
     * */
    public static boolean isRaw1K(File dump) {
        return dump.length() == 1024;
    }

    public static boolean isRaw1K(byte[] dumpByte) {
        return dumpByte.length == 1024;
    }

    /*
     * 判断是否是2K的数据文件
     * */
    public static boolean isRaw2K(File dump) {
        return dump.length() == 2048;
    }

    public static boolean isRaw2K(byte[] dumpByte) {
        return dumpByte.length == 2048;
    }

    /*
     * 判断是否是原生4k文件
     * */
    public static boolean isRaw4K(File dump) {
        return dump.length() == 4096;
    }

    public static boolean isRaw4K(byte[] dumpByte) {
        return dumpByte.length == 4096;
    }

    /*
     * 将1kdump转为4k文件
     * */
    public static byte[] raw1Kto4k(byte[] raw1k) throws Exception {
        if (isRaw1K(raw1k))
            throw new Exception("非1k字节文件!");
        //建立一个4096大小的数组
        byte[] ret = new byte[4096];
        for (int i = 0; i < raw1k.length; i++) {
            //将1k数据合并至4k的数据
            ret[i] = raw1k[i];
        }
        //填充剩余的字节为0
        for (int i = raw1k.length; i < ret.length; i++) {
            ret[i] = 0x00;
        }
        return ret;
    }

    /*
     * 将4k文件转换为1k的文件
     * */
    public static byte[] raw4kto1k(byte[] raw4k) throws Exception {
        if (isRaw4K(raw4k))
            throw new Exception("非4k字节文件!");
        byte[] ret = new byte[1024];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = raw4k[i];
        }
        return ret;
    }

    /*
     * 判断是否是16进制字符
     * */
    public static boolean isDataChar(char c) {
        if (c >= 'a' && c <= 'z') {
            return true;
        }
        if (c >= 'A' && c <= 'Z') {
            return true;
        }
        if (c >= '0' && c <= '9') {
            return true;
        }
        if (c == '-') {
            return true;
        }
        if (c == '?')
            return true;
        return c == '*';
    }

    /*
     * 裁剪有效数据
     * */
    public static String cutVaildData(String str) {
        String ret;
        ret = RegexGroupUtil.matcherGroup(str, "([A-Fa-f0-9]{32})", 1, 0);
        if (ret != null) {
            return ret;
        }
        char[] cs = new char[str.length()];
        str.getChars(0, str.length(), cs, 0);
        //搜索字符串数组，从尾部开始搜素!
        int pos = 0;
        for (int i = cs.length - 1; i >= 0; i--) {
            if (!isDataChar(cs[i])) {
                //如果当前不是数据字符，则移动指针到上一位(也是顺序下一位)
                pos = i + 1;
                // Log.d(LOG_TAG, "非数据字符，跳过");
                break;
            }
        }
        //走常规的判断应当从尾部开始截取,直到遇见任何非16进制字符或者非注释字符时停止并获得定位
        if (pos == 0) {
            // Log.d(LOG_TAG, "定位在0,直接返回原字符串: " + str);
            return str;
        } else {
            ret = str.substring(pos);
            // Log.d(LOG_TAG, "定位不为零，返回经过裁剪后的: " + ret);
        }
        // 修复非有效块依旧返回的问题!
        return isValidBlockData(ret) ? ret : null;
    }

    /*
     * 判断是否是块数据
     * */
    public static boolean isBlockData(String data) {
        return data.length() == 32 && data.matches("[0-9a-fA-F-?*]{32}");
    }

    public static boolean isValidBlockData(String data) {
        return data.length() == 32 && data.matches("[0-9a-fA-F]{32}");
    }

    /*
     * 转换为bin数据流
     */
    public static byte[][] getBin(byte[] data) {
        if (data == null) return null;
        if (data.length == MifareClassic.SIZE_1K ||
                data.length == MifareClassic.SIZE_2K ||
                data.length == MifareClassic.SIZE_4K) {
            return HexUtil.splitBytes(data, 16);
        } else {
            return null;
        }
    }

    /*
     * 合并
     * */
    public static byte[] mergeBins(byte[][] datas) {
        if (datas == null) return null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        for (int i = 0; i < datas.length; i++) {
            for (int j = 0; j < datas[i].length; j++) {
                bos.write(datas[i][j]);
            }
        }
        return bos.toByteArray();
    }

    /*
     * 转换为txt数据流
     * */
    public static String[] getTxt(byte[] data) {
        if (data == null) return null;
        StringBuilder sb = new StringBuilder();
        //转换为字符串
        String dataStr = new String(data);
        //根据换行符进行切割字符串
        String[] dataLines = splitDump(dataStr);
        //进行有效数据的判断切割
        for (int i = 0; i < dataLines.length; i++) {
            //判断一下，如果数据有效，则追加进字符串构造器中
            //16进制的字符串必须要有至少HEX32个字符
            if (dataLines[i].length() < 32) continue;
            //如果等于32个字符串则需要验证一下字符串是否是正确的16进制字符串!
            if (dataLines[i].length() == 32) {
                //添加结果集
                if (isBlockData(dataLines[i])) sb.append(dataLines[i]);
                //判断是否需要换行!
                if (i != (dataLines.length - 1)) sb.append(getSystemLF("unix"));
            } else {
                //如果大于32个字符则需要尝试截取一下
                String vaildData = cutVaildData(dataLines[i]);
                // Log.d(LOG_TAG, "测试输出数据: " + vaildData);
                //数据裁剪成功，添加进数据集中
                if (vaildData != null) sb.append(vaildData);
                //判断是否需要换行
                if (i != dataLines.length - 1) sb.append(getSystemLF("unix"));
            }
        }
        //结果集转换为数组
        String[] ret = sb.toString().split(getSystemLF("unix"));
        //分别符合1K,2K,4K的规范!
        //判断块数量，严格控制规范，使块数量在64或者256之间
        if (ret.length == 64 || ret.length == 128 || ret.length == 256) {
            /*for (String b : ret) {
                Log.d(LOG_TAG, "测试输出截取结果: " + b);
            }*/
            return ret;
        } else {
            return null;
        }
    }

    /*
     * 合并数组为字符串！
     * */
    public static String mergeTxt(String[] txts, boolean needNewLine, String lineChar) {
        if (txts == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < txts.length; i++) {
            if (needNewLine) {
                if (i == txts.length - 1) {
                    sb.append(txts[i]);
                } else {
                    sb.append(txts[i]).append(lineChar);
                }
            } else {
                sb.append(txts[i]);
            }
        }
        return sb.toString();
    }

    /*
     * 判断数据类型
     * */
    public static int getType(byte[] data) {
        //初步断定是原生的二进制文件
        if (getBin(data) != null) return TYPE_BIN;
        //最后判断是txt文件
        if (getTxt(data) != null) return TYPE_TXT;
        //然后尝试切割有效的数据
        return TYPE_NOT;
    }

    /*
     * txt转换为bin
     * */
    public static byte[] txt2Bin(byte[] txt) {
        //已经封装过将文本忽略修饰直接转换为纯hex文本的方法
        //直接调用这个方法进行获取，而后转换为bin格式
        String[] datas = getTxt(txt);
        //输出流，把字节输出到buf，初始大小1024，4k时自动扩展!
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        //进行迭代
        for (String data : datas) {
            //进行转换
            byte[] _tmps = HexUtil.hexStringToByteArray(data);
            try {
                if (_tmps != null) {
                    baos.write(_tmps);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return baos.toByteArray().length >= 1024 ? baos.toByteArray() : null;
    }

    /*
     * bin转换为txt
     * */
    public static byte[] bin2Txt(byte[] bin) {
        //得到经过解析的块数组
        byte[][] blocks = getBin(bin);
        StringBuilder sb = new StringBuilder();
        //如果不为空，则是有效的数据
        if (blocks != null) {
            //抽取出每一块的数据，将其转换为16字节16进制的32个字符的字符串
            for (int i = 0; i < blocks.length; ++i) {
                //迭代这个数据，进行转换处理
                if (i == blocks.length - 1) {
                    //最后一个元素，无需添加换行
                    sb.append(HexUtil.toHexString(blocks[i]));
                } else {
                    sb.append(HexUtil.toHexString(blocks[i])).append("\n");
                }
            }
        } else {
            return null;
        }
        return sb.toString().getBytes();
    }

    /*
     * 进行修饰
     * */
    public static String decorate(byte[] bytes) {
        /*
         * 修饰为MCT支持的格式（为了兼容性）
         * */
        //调用已经封装的字节数组转String数组的方法
        String[] blocks = getTxt(bytes);
        return decorate(blocks);
    }

    public static String decorate(String[] bytes) {
        //根据规范，当块的数量是64时，他是1K卡（S50），有16个扇区
        //如果是256个块时，他是4K卡（S70）
        if (bytes == null) {
            // LogUtils.d("decorate()函数截取失败!");
            return null;
        }
        if (bytes.length != 64 && bytes.length != 128 && bytes.length != 256)
            return null;
        String label = "+Sector: ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            //当前是每个扇区的起始块！
            if (isHeader(i)) {
                //添加扇区修饰，使数据可被MCT识别!
                sb.append(label).append(toSector(i)).append('\n');
                //继而将这个起始扇区写在后面
                sb.append(bytes[i]).append('\n');
            } else {
                if (i == bytes.length - 1) {
                    //已经在最结尾的一行，不需要换行
                    sb.append(bytes[i]);
                } else {
                    //需要换行
                    sb.append(bytes[i]).append('\n');
                }
            }
        }
        return sb.toString();
    }

    /*
     * 块转扇区
     * */
    public static int toSector(int block) {
        if (block < 32 * 4) {
            return (block / 4);
        } else {
            return (32 + (block - 32 * 4) / 16);
        }
    }

    /*
     * 扇区转块
     * */
    public static int toBlock(int sector) {
        if (sector < 32) {
            return (sector * 4);
        } else {
            return (32 * 4 + (sector - 32) * 16);
        }
    }

    /*
     * 是否是在首行
     * */
    public static boolean isHeader(int block) {
        if (block < 128)
            return (block % 4 == 0);
        else
            return (block % 16 == 0);
    }

    /*
     * 是否在尾部
     * */
    public static boolean isFoolter(int block) {
        if (block < 128)
            return ((block + 1) % 4 == 0);
        else
            return ((block + 1) % 16 == 0);
    }

    /*
     * 去除修饰
     * */
    public static String[] undecorate(byte[] bytes) {
        return getTxt(bytes);
    }

    /*
     * 将M1数据bean合并为数组
     * */
    public static String[] mergeDatas(ArrayList<M1Bean> beans) {
        StringBuilder sb = new StringBuilder();
        // 排序一下!l
        Collections.sort(beans, new Comparator<M1Bean>() {
            @Override
            public int compare(M1Bean o1, M1Bean o2) {
                return Integer.compare(o1.getSector(), o2.getSector());
            }
        });
        for (int i = 0; i < beans.size(); i++) {
            //取出bean
            M1Bean bean = beans.get(i);
            if (bean == null) return null;
            //迭代bean中的信息，进行处理
            String[] _tmp = bean.getDatas();
            if (_tmp != null) {
                for (int j = 0; j < _tmp.length; j++) {
                    if (i == beans.size() - 1 && j == (_tmp.length - 1)) {
                        //结尾，无需换行
                        sb.append(_tmp[j]);
                    } else {
                        sb.append(_tmp[j]).append("\n");
                    }
                }
            } else {
                return null;
            }
        }
        return sb.toString().split("\n");
    }

    /*
     * 将数组分散为M1数据!
     * */
    public static M1Bean[] mergeDatas(String[] datas) {
        //块数量!
        int blockCount = datas.length;
        ArrayList<M1Bean> rets = new ArrayList<>(16);
        for (int i = 0; i < blockCount; ) {
            M1Bean bean = new M1Bean();
            //获取当前的扇区
            int sector = MifareClassicUtils.blockToSector(i);
            bean.setSector(sector);
            //获取当前的块数量统计!
            int blockCountInSecrot = MifareClassicUtils.getBlockCountInSector(sector);
            String[] dataArray = new String[blockCountInSecrot];
            //进行迭代添加!
            for (int j = i, k = 0; j < blockCountInSecrot; ++j, ++k) {
                dataArray[i] = datas[j];
            }
            //设置进去数据当中!
            bean.setDatas(dataArray);
            rets.add(bean);
            //i值自增块数量!
            i += blockCountInSecrot;
        }
        return rets.toArray(new M1Bean[0]);
    }

    // 是否整个块都是零!
    public static boolean isBlockAllZero(String block) {
        if (block == null) return true;
        return block.matches("[0]{32}");
    }

    //实现合并读取结果
    public static M1Bean mergeBean(M1Bean aBean, M1Bean bBean) {
        M1Bean ret;
        //判断某个bean是否可用
        if (aBean == null || bBean == null) {
            if (aBean != null) {
                return aBean;
            }
            if (bBean != null) {
                return bBean;
            }
            return getEmptyM1Bean(0);
        }
        //初始化结果bean
        ret = new M1Bean(bBean.getSector());
        //否则将数据进行对比合并
        String[] aBeanDatas = aBean.getDatas();
        if (aBeanDatas == null) {
            aBean = getEmptyM1Bean(aBean.getSector());
            aBeanDatas = aBean.getDatas();
        }
        int last = aBeanDatas.length - 1;
        //建立数据保存数组
        String[] datas = new String[aBean.getDatas().length];
        //取出块数据
        String[] tmpA = aBeanDatas;
        String[] tmpB = bBean.getDatas() == null ? getEmptyM1Bean(bBean.getSector()).getDatas() : bBean.getDatas();
        for (int i = 0; i <= last; ++i) {
            //先判断两组块数据是否相同
            if (tmpA[i].equals(tmpB[i])) {
                //相同则取任意一个合并到ret中
                datas[i] = tmpA[i];
            } else {
                // 修复某些块异常也被用来处理的问题
                if (!isValidBlockData(tmpA[i]) && isValidBlockData(tmpB[i])) {
                    datas[i] = tmpB[i];
                    continue;
                }
                if (!isValidBlockData(tmpB[i]) && isValidBlockData(tmpA[i])) {
                    datas[i] = tmpA[i];
                    continue;
                }
                if (i != last) {
                    //否则是否全都是0,如果全部为零则优先考虑其他的情况!
                    boolean isADataAllZero = isBlockAllZero(tmpA[i]);
                    boolean isBDataAllZero = isBlockAllZero(tmpB[i]);
                    // 两者都是零，说明数据真的是0
                    if (isADataAllZero && isBDataAllZero) {
                        // 开始选择用正确的数据
                        if (DumpUtils.isValidBlockData(tmpB[i])) {
                            datas[i] = tmpB[i];
                        } else if (DumpUtils.isValidBlockData(tmpA[i])) {
                            datas[i] = tmpA[i];
                        } else {
                            //不符合数据规范，跳过操作
                            datas[i] = DumpUtils.BLANK_DATA;
                        }
                    } else {
                        // 开始选择用正确的数据
                        if (DumpUtils.isValidBlockData(tmpB[i]) && isADataAllZero) {
                            datas[i] = tmpB[i];
                        } else if (DumpUtils.isValidBlockData(tmpA[i]) && isBDataAllZero) {
                            datas[i] = tmpA[i];
                        } else {
                            //不符合数据规范，跳过操作
                            datas[i] = DumpUtils.BLANK_DATA;
                        }
                    }
                } else {
                    //判断bBean的块数据非空
                    //并且aBean中密钥B不可读的情况下
                    //才能将bBean中的数据传入到ret中
                    //得到控制位
                    boolean isADataTrialerAllDefault = tmpA[i].equalsIgnoreCase(BLANK_TRAIL_BLOCK);
                    boolean isBDataTrialerAllDefault = tmpB[i].equalsIgnoreCase(BLANK_TRAIL_BLOCK);
                    // 两者皆为默认，暂且认定为真的数据就是这个!
                    if (isADataTrialerAllDefault && isBDataTrialerAllDefault) {
                        // 随便填充一个就行了!
                        datas[i] = tmpA[i];
                    } else {
                        if (DumpUtils.isValidBlockData(tmpB[i]) && isADataTrialerAllDefault) {
                            //有效的尾部块
                            datas[i] = tmpB[i];
                        } else if (DumpUtils.isValidBlockData(tmpA[i]) && isBDataTrialerAllDefault) {
                            datas[i] = tmpA[i];
                        } else {
                            //有效的尾部块
                            // 置空尾部块！
                            datas[i] = DumpUtils.BLANK_TRAIL_BLOCK;
                        }
                    }
                }

            }
        }
        ret.setDatas(datas);
        return ret;
    }

    //把密钥更新进数据组中
    public static void updateTrailer(M1Bean dB, M1KeyBean kB) {
        if (dB == null || kB == null) return;
        if (dB.getSector() != kB.getSector()) return;
        String[] datas = dB.getDatas();
        if (datas == null) {
            dB = getEmptyM1Bean(dB.getSector());
            datas = dB.getDatas();
        }
        //有些时候，我们可以验证成功，但是无法读取操作数据块，此时，我们还是可以将密钥更新进数据集中
        int lastIndex = datas.length - 1;
        String last = dB.getDatas()[lastIndex];
        if (last.length() != 32) {
            //修复越界异常，我们进行判断并且跳过操作!
            /*Log.d(LOG_TAG, "****************");
            Log.d(LOG_TAG, "Update position: " + dB.getSector());
            Log.d(LOG_TAG, "Invalid content: " + last);
            Log.d(LOG_TAG, "Invalid length: " + last.length());
            Log.d(LOG_TAG, "****************");*/
            return;
        }
        //截取到最后一个块（尾部块），然后把有效密钥更新进去
        String _kA = kB.getKeyA().replaceAll("\\*", "F");
        String _kB = kB.getKeyB().replaceAll("\\*", "F");
        //更新密钥进去
        last = _kA + last.substring(12, 32);
        last = last.substring(0, 20) + _kB;
        dB.getDatas()[dB.getDatas().length - 1] = last;
    }

    /*
     * 判断是否是unix类系统
     * */
    public static boolean isUnixLFFormat(String str) {
        return !str.contains("\r\n");
    }

    /*
     * 获取对应的类系统的换行符
     * */
    public static String getSystemLF(String sysClz) {
        String ret = "\r\n";
        switch (sysClz) {
            case "windows":
                break;
            case "unix":
                ret = "\n";
                break;
        }
        return ret;
    }

    public static M1Bean[] getSectorFromArray(String[] contents) {
        ArrayList<M1Bean> retList = new ArrayList<>();
        if (contents != null) {
            // 类型判断!
            boolean is4K = contents.length == MifareClassic.SIZE_4K / MifareClassic.BLOCK_SIZE;
            boolean is2K = contents.length == MifareClassic.SIZE_2K / MifareClassic.BLOCK_SIZE;
            boolean is1K = contents.length == MifareClassic.SIZE_1K / MifareClassic.BLOCK_SIZE;
            boolean is1S = contents.length == 4 || contents.length == 16;
            if (is4K || is2K || is1K) {
                //1K卡,2K卡!
                M1Bean bean = null;
                String[] _tmps = null;
                for (int i = 0; i < contents.length; ) {
                    // 进行尾部的判断，如果是256的扇区的话我们需要进行偏移量的重新设置!
                    int count = MifareClassicUtils.getBlockCountInSector(MifareClassicUtils.blockToSector(i));
                    if (DumpUtils.isHeader(i)) {
                        //在首部块!
                        bean = new M1Bean();
                        _tmps = new String[count];
                    }
                    //进行偏移拷贝!
                    if (_tmps != null)
                        System.arraycopy(contents, i, _tmps, 0, count);
                    if (bean != null) {
                        bean.setDatas(_tmps);
                        bean.setSector(DumpUtils.toSector(i));
                    }
                    //结束(将结果添加至集合中)
                    retList.add(bean);
                    i += count;
                }
            }
            if (is1S) { // 一个扇区!
                M1Bean b1 = new M1Bean();
                b1.setDatas(contents);
                retList.add(b1);
            }
        }
        return retList.toArray(new M1Bean[0]);
    }

    public static boolean isDump(File file) {
        if (file == null) return false;
        // 大于50k的数据也认为不是正确的数据!
        if (file.length() > MAX_DUMP_FILE_SIZE) return false;
        if (file.exists() && file.isFile()) {
            try {
                return getType(FileUtils.readBytes(file)) != TYPE_NOT;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static boolean isDump(byte[] data) {
        if (data == null) return false;
        // 大于50k的数据也认为不是正确的数据!
        if (data.length > MAX_DUMP_FILE_SIZE) return false;
        return getType(data) != TYPE_NOT;
    }

    public static boolean isDump(Uri uri) {
        byte[] data = readDump(uri);
        if (data == null) return false;
        return isDump(data);
    }

    public static byte[] readDump(Uri uri) {
        try {
            return FileUtils.readBytes(uri, -1, MAX_DUMP_FILE_SIZE, -1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static M1Bean[] readDumpBeans(Uri uri) {
        byte[] hexDat = readDump(uri);
        if (hexDat == null) return null;
        int type = getType(hexDat);
        byte[] data;
        switch (type) {
            case TYPE_BIN:
                // 读取到的是二进制类型，直接进行操作转换!
                data = bin2Txt(mergeBins(getBin(hexDat)));
                break;
            case TYPE_TXT:
                data = hexDat;
                break;
            case TYPE_NOT:
            default:
                return null;
        }
        return getSectorFromArray(getTxt(data));
    }
}
