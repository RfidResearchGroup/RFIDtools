
/*AUTHOR: DXL
 * TIME: 2017.10.5
 */

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif // HAVE_CONFIG_H

#include <stdio.h>
#include <stdbool.h>
#include <string.h>
#include <mifare.h>
#include <tools.h>
#include <nfc/nfc.h>
#include "nfc/nfc-utils.h"
#include <nfc-internal.h>
#include <nfc/nfc-types.h>
#include <drivers.h>

//声明静态的相关变量
static nfc_context *context;
static nfc_device *pnd;
static nfc_target nt;
static mifare_param mp;
static bool bUseKeyA = false;
static bool bWriteZero = false;
static bool magic2 = false;
static bool unlocked = false;

//设置读写的目标卡类型
static const nfc_modulation nmMifare = {
        .nmt = NMT_ISO14443A,
        .nbr = NBR_106,
};

//储存标签扇区和块总数的数据结构
static struct MifareClassicTAG {
    //TODO 储存的是块总数
    uint8_t uiBlocks;
    //TODO 储存的是扇区总数
    int32_t uiSectors;
};
static struct MifareClassicTAG mft;

//帧大小限制（正常帧）
#define MAX_FRAME_LEN 264

//设置接收到的字节上限
static uint8_t abtRx[MAX_FRAME_LEN];

//硬件暂停命令
uint8_t abtHalt[4] = {0x50, 0x00, 0x00, 0x00};

//特殊卡解锁命令
uint8_t abtUnlock1[1] = {0x40};
uint8_t abtUnlock2[1] = {0x43};

//特殊卡上锁命令
uint8_t abtUplock1[] = {0xe1, 0x00, 0xe1, 0xee};
uint8_t abtUplock2[] = {0x85, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x08,
                        0x18, 0x47};

//卡的参数
static uint8_t *pbtUID;

//TODO this function is return rats
static bool get_rats(void) {
    int res;
    uint8_t abtRx[MAX_FRAME_LEN];
    uint8_t abtRats[2] = {0xe0, 0x50};
    // Use raw write/read methods
    if (nfc_device_set_property_bool(pnd, NP_EASY_FRAMING, false) < 0) {
        nfc_perror(pnd, "nfc_configure");
        return false;
    }
    res = nfc_initiator_transceive_bytes(pnd, abtRats, sizeof(abtRats), abtRx, sizeof(abtRx), 0);
    if (res > 0) {
        // ISO14443-4 card, turn RF field off/on to access ISO14443-3 again
        if (nfc_device_set_property_bool(pnd, NP_ACTIVATE_FIELD, false) < 0) {
            nfc_perror(pnd, "nfc_configure");
            return false;
        }
        if (nfc_device_set_property_bool(pnd, NP_ACTIVATE_FIELD, true) < 0) {
            nfc_perror(pnd, "nfc_configure");
            return false;
        }
    }

    //TODO BUG
    // Reselect tag
    /*if (nfc_initiator_select_passive_target(pnd, nmMifare, NULL, 0, &nt) <= 0) {
        LOGD("Error: tag disappeared\n");
        //nfc_close(pnd);
        //nfc_exit(context);
        return false;
        //exit(EXIT_FAILURE);
    }*/

    if (res > 0) {

        // 中国魔力仿真卡（UID-IC卡）, ATS=0978009102:dabc1910
        if ((res == 9) && (abtRx[5] == 0xda) && (abtRx[6] == 0xbc)
            && (abtRx[7] == 0x19) && (abtRx[8] == 0x10)) {
            magic2 = true;
        }

        if (res >= 10) {
            return ((abtRx[5] == 0xc1) && (abtRx[6] == 0x05)
                    && (abtRx[7] == 0x2f) && (abtRx[8] == 0x2f)
                    && ((nt.nti.nai.abtAtqa[1] & 0x02) == 0x00));
        } else {
            return false;
        }

    } else {
        return false;
    }
}

//TODO 验证一下扇区的数量的准确性
static bool validateSector(int sector) {
    // Do not be too strict on upper bounds checking, since some cards
    // have more addressable memory than they report. For example,
    // MIFARE Plus 2k cards will appear as MIFARE Classic 1k cards when in
    // MIFARE Classic compatibility mode.
    // Note that issuing a command to an out-of-bounds block is safe - the
    // tag should report error causing IOException. This validation is a
    // helper to guard against obvious programming mistakes.
    if (sector < 0 || sector >= NR_TRAILERS_4k) {
        ERR("sector out of bounds: %d\n", sector);
        return false;
    }
    return true;
}

//TODO 验证一下块的数量的准确性
static bool validateBlock(int block) {
    // Just looking for obvious out of bounds...
    if (block < 0 || block >= NR_BLOCKS_4k) {
        ERR("block out of bounds: %d\n", block);
        return false;
    }
    return true;
}

//TODO 判断是否处于尾部块
static bool is_trailer_block(uint32_t uiBlock) {
    // 测试我们处于小区块还是大扇区
    if (uiBlock < 128)
        return ((uiBlock + 1) % 4 == 0);
    else
        return ((uiBlock + 1) % 16 == 0);
}

//TODO 扇区转块,返回当前扇区的第一个块
static uint8_t sectorToBlock(int sectorIndex) {

    if (!validateSector(sectorIndex)) {
        return -1;
    }

    if (sectorIndex < 32) {
        return (uint8_t) (sectorIndex * 4);
    } else {
        return (uint8_t) (32 * 4 + (sectorIndex - 32) * 16);
    }
}

//TODO 获得此块所在扇区的尾部块（秘钥与控制位所在块）
static uint32_t get_trailer_block(uint32_t uiFirstBlock) {
    // Test if we are in the small or big sectors
    uint32_t trailer_block = 0;
    if (uiFirstBlock < 128) {
        trailer_block = uiFirstBlock + (3 - (uiFirstBlock % 4));
    } else {
        trailer_block = uiFirstBlock + (15 - (uiFirstBlock % 16));
    }
    return trailer_block;
}

static bool authenticate(uint32_t uiBlock) {
    mifare_cmd mc;
    // 设置验证信息（UID之类的）
    memcpy(mp.mpa.abtAuthUid, nt.nti.nai.abtUid, nt.nti.nai.szUidLen);
    // 我们应该使用哪个Key验证扇区 A或者B？
    mc = (bUseKeyA) ? MC_AUTH_A : MC_AUTH_B;
    //尝试验证当前扇区秘钥
    if (nfc_initiator_mifare_cmd(pnd, mc, (const uint8_t) get_trailer_block(uiBlock), &mp))
        return true;
    return false;
}

static bool transmit_bits(const uint8_t *pbtTx, const size_t szTxBits) {
    //接收到的位大小
    //int szRxBits;
    // Show transmitted command
    //LOGD("Sent bits:     ");
    //print_hex_bits(pbtTx, szTxBits);
    // Transmit the bit frame command, we don't use the arbitrary parity feature
    if ((/*szRxBits = */nfc_initiator_transceive_bits(pnd, pbtTx, szTxBits, NULL, abtRx,
                                                      sizeof(abtRx),
                                                      NULL)) < 0)
        return false;

    // Show received answer
    //LOGD("Received bits: ");
    //print_hex_bits(abtRx, szRxBits);
    // Succesful transfer
    return true;
}

static bool transmit_bytes(const uint8_t *pbtTx, const size_t szTx) {
    // 显示将要进行传输的命令
    //LOGD("Sent bits:     ");
    //print_hex(pbtTx, szTx);
    //传输命令字节
    //int res;
    if ((/*res = */nfc_initiator_transceive_bytes(pnd, pbtTx, szTx, abtRx, sizeof(abtRx), 0)) < 0)
        return false;

    // 显示接收到的回应
    //LOGD("Received bits: ");
    //print_hex(abtRx, res);
    // 成功传输
    return true;
}

static bool _unlock() {
    //JNI环境初始化
    getJniEnv();

    // 配置CRC校验为关闭，注意，记得开启CRC！
    if (nfc_device_set_property_bool(pnd, NP_HANDLE_CRC, false) < 0) {
        nfc_perror(pnd, "nfc_configure");
        return unlocked;
    }

    //使用原生的发送接收函数
    if (nfc_device_set_property_bool(pnd, NP_EASY_FRAMING, false) < 0) {
        nfc_perror(pnd, "nfc_configure");
        return unlocked;
    }

    iso14443a_crc_append(abtHalt, 2);
    transmit_bytes(abtHalt, 4);

    // 现在发送解锁命令
    if (!transmit_bits(abtUnlock1, 7)) {
        ERR("第一段后门开启命令执行失败!\n");
        unlocked = false;
    } else {
        printf("第一段后门开启命令执行成功!\n");
        if (transmit_bytes(abtUnlock2, 1)) {
            printf("第二段后门开启命令执行成功!\n");
            printf("Card unlocked");
            unlocked = true;
        } else {
            ERR("第二段后门开启命令执行失败!\n");
            unlocked = false;
        }
    }

    //TODO 重置读卡器参数,相当重要，如果不重置那么将会影响验证密码等操作
    // 配置CRC校验
    if (nfc_device_set_property_bool(pnd, NP_HANDLE_CRC, true) < 0) {
        nfc_perror(pnd, "nfc_device_set_property_bool");
        return unlocked;
    }

    //关闭原生发送接收函数
    if (nfc_device_set_property_bool(pnd, NP_EASY_FRAMING, true) < 0) {
        nfc_perror(pnd, "nfc_device_set_property_bool");
        return unlocked;
    }
    return unlocked;
}

static bool _uplock() {
    //判断当前有没有开启了后门
    if (!unlocked) {
        return false;
    } else {
        //JNI环境初始化
        getJniEnv();

        // 配置CRC校验为关闭，注意，记得开启CRC！
        if (nfc_device_set_property_bool(pnd, NP_HANDLE_CRC, false) < 0) {
            nfc_perror(pnd, "nfc_configure");
            return false;
        }

        //使用原生的发送接收函数
        if (nfc_device_set_property_bool(pnd, NP_EASY_FRAMING, false) < 0) {
            nfc_perror(pnd, "nfc_configure");
            return false;
        }

        //发送第一条指令
        if (transmit_bytes(abtUplock1, sizeof(abtUplock1))) {
            printf("第一段封后门开启命令执行成功!\n");
            //尝试发送第二条以彻底封卡
            if (transmit_bytes(abtUplock2, sizeof(abtUplock2))) {
                printf("第二段封后门开启命令执行成功!\n");
            } else {
                ERR("第二段封后门开启命令执行失败!\n");
                return false;
            }
        } else {
            ERR("第一段封后门开启命令执行失败!\n");
            return false;
        }

        //TODO 重置读卡器参数,相当重要，如果不重置那么将会影响验证密码等操作
        // 配置CRC校验
        if (nfc_device_set_property_bool(pnd, NP_HANDLE_CRC, true) < 0) {
            nfc_perror(pnd, "nfc_device_set_property_bool");
            return false;
        }

        //关闭原生发送接收函数
        if (nfc_device_set_property_bool(pnd, NP_EASY_FRAMING, true) < 0) {
            nfc_perror(pnd, "nfc_device_set_property_bool");
            return false;
        }
        return true;
    }
}

static bool _scanning() {
    JNIEnv *tmpJniEnv = getJniEnv();

    //执行前先检查异常
    if ((*tmpJniEnv)->ExceptionCheck(tmpJniEnv)) {
        (*tmpJniEnv)->ExceptionDescribe(tmpJniEnv);
        (*tmpJniEnv)->ExceptionClear(tmpJniEnv);
        return false;
    }

    //初始化上下文
    if (context == NULL) {
        nfc_init(&context);
    }

    if (context == NULL) {
        ERR("Unable to init libnfc (malloc)\n");
        return false;
    }

    // 打开设备
    pnd = nfc_open(context, NULL);

    if (pnd == NULL) {
        ERR("Error opening NFC reader\n");
        nfc_exit(context);
        //设备打不开则再次检查异常
        if ((*tmpJniEnv)->ExceptionCheck(tmpJniEnv)) {
            (*tmpJniEnv)->ExceptionDescribe(tmpJniEnv);
            (*tmpJniEnv)->ExceptionClear(tmpJniEnv);
        }
        return false;
    }

    //初始化为发起者模式
    if (nfc_initiator_init(pnd) < 0) {
        nfc_perror(pnd, "nfc_initiator_init");
        nfc_close(pnd);
        nfc_exit(context);
        return false;
    };

    //配置一下防冲撞，使该函数只选一张卡
    // Let the reader only try once to find a tag
    if (nfc_device_set_property_bool(pnd, NP_INFINITE_SELECT, false) < 0) {
        nfc_perror(pnd, "nfc_device_set_property_bool");
        return false;
    }

    //关闭1443_4自动切换
    if (nfc_device_set_property_bool(pnd, NP_AUTO_ISO14443_4, false) < 0) {
        nfc_perror(pnd, "nfc_device_set_property_bool");
        return false;
    }

    int tagCount = 0;
    tagCount = nfc_initiator_select_passive_target(pnd, nmMifare, NULL, 0,
                                                   &nt);
    //发现被动标签
    if (tagCount <= 0) {
        nfc_perror(pnd, "nfc_initiator_select_passive_target");
        return false;
    }

    //取消选择
    if (nfc_initiator_deselect_target(pnd) < 0) {
        nfc_perror(pnd, "nfc_initiator_deselect_target");
        return false;
    }
    return true;
}

static bool _connect() {
    //JNI环境初始化
    getJniEnv();

    //连接标签
    if (nfc_initiator_select_passive_target(pnd, nmMifare, nt.nti.nai.abtUid,
                                            nt.nti.nai.szUidLen, &nt) <= 0) {
        WARN("Warnning: connect to tag is fail, please make sure tag is existing");
        return false;
    }

    // 测试如果我们处理的是MifareClassic兼容性标签
    if ((nt.nti.nai.btSak & 0x08) == 0 || (nt.nti.nai.btSak & 0x19) == 0) {
        WARN("Warning: tag is probably not a MFC!\n");
    }

    //从当前标签获得信息
    pbtUID = nt.nti.nai.abtUid;

    //日志中打印相关信息
    //LOGD("\nFound MIFARE Classic card:\n");
    //print_nfc_target(&nt, false);

    // 判断容量
    switch (nt.nti.nai.btSak) {
        case 0x01:
        case 0x08:
        case 0x19:
        case 0x28:
        case 0x88:
            if (get_rats()) {
                //LOGD("Found Mifare Plus 2k tag");
                mft.uiSectors = NR_TRAILERS_2k;
                mft.uiBlocks = NR_BLOCKS_2k;
            } else {
                //LOGD("发现了1K卡");
                mft.uiSectors = NR_TRAILERS_1k;
                mft.uiBlocks = NR_BLOCKS_1k;
            }
            break;
        case 0x09:
            //LOGD("发现了迷你卡");
            mft.uiSectors = NR_TRAILERS_MINI;
            mft.uiBlocks = NR_BLOCKS_MINI;
            break;
        case 0x18:
            //LOGD("发现了4K卡");
            mft.uiSectors = NR_TRAILERS_4k;
            mft.uiBlocks = NR_BLOCKS_4k;
            break;
        default:
            LOGD("不能判断卡的类型");
            return false;
    }
    printf("Guessing size: seems to be a %lu-byte card\n",
           (mft.uiBlocks + 1) * sizeof(mifare_classic_block));
    return true;
}

static jbyteArray _getUid() {
    //JNI环境初始化
    JNIEnv *tmpJniEnv = getJniEnv();

    if (NULL != pbtUID) {
        int uidLen = nt.nti.nai.szUidLen;
        jbyteArray byteArray = (*tmpJniEnv)->NewByteArray(tmpJniEnv, uidLen);
        (*tmpJniEnv)->SetByteArrayRegion(tmpJniEnv, byteArray, 0, uidLen,
                                         (const jbyte *) pbtUID);
        return byteArray;
    } else {
        return NULL;
    }
}

static jbyteArray _getAtqa() {
    //JNI环境初始化
    JNIEnv *tmpJniEnv = getJniEnv();

    int atqaLen = sizeof(nt.nti.nai.abtAtqa);
    jbyteArray byteArray = (*tmpJniEnv)->NewByteArray(tmpJniEnv, atqaLen);
    (*tmpJniEnv)->SetByteArrayRegion(tmpJniEnv, byteArray, 0, atqaLen,
                                     (const jbyte *) nt.nti.nai.abtAtqa);
    return byteArray;
}

static jbyteArray _getSak() {
    //JNI环境初始化
    JNIEnv *tmpJniEnv = getJniEnv();
    jbyte sakArray[] = {(const jbyte) nt.nti.nai.btSak};
    int sakArrayLen = sizeof(sakArray);
    jbyteArray byteArray = (*tmpJniEnv)->NewByteArray(tmpJniEnv, sakArrayLen);
    (*tmpJniEnv)->SetByteArrayRegion(tmpJniEnv, byteArray, 0, sakArrayLen, sakArray
    );
    return byteArray;
}

static jint _getSize() {

    //JNI环境初始化
    getJniEnv();

    switch (mft.uiSectors) {
        case NR_TRAILERS_1k:
            return 1024;
        case NR_TRAILERS_2k:
            return 2048;
        case NR_TRAILERS_4k:
            return 4096;
        case NR_TRAILERS_MINI:
            return 320;
        default:
            return -1;
    }
}

static jint _getSectorCount() {
    return mft.uiSectors;
}

static jint _getBlockCount() {
    return mft.uiBlocks + 1;
}

static jboolean _isUnlock() {
    return (jboolean) unlocked;
}

static jboolean _isEmulated() {
    return (jboolean) magic2;
}

static jboolean _disconnect() {
    //断开连接
    if (nfc_initiator_deselect_target(pnd) <= 0) {
        ERR("deselect target fail");
        return false;
    } else {
        return true;
    }
}

static jboolean _authWithKeyA(JNIEnv *env, jobject obj, jint sector, jbyteArray keyA_) {
    //TODO 重置读卡器参数,相当重要，如果不重置那么将会影响验证密码等操作

    // 配置CRC校验
    if (nfc_device_set_property_bool(pnd, NP_HANDLE_CRC, true) < 0) {
        nfc_perror(pnd, "nfc_device_set_property_bool");
        return false;
    }

    //关闭原生发送接收函数
    if (nfc_device_set_property_bool(pnd, NP_EASY_FRAMING, true) < 0) {
        nfc_perror(pnd, "nfc_device_set_property_bool");
        return false;
    }

    //重新连接
    _disconnect();

    jbyte *keyA = (*env)->GetByteArrayElements(env, keyA_, NULL);
    //指定标志位，使用秘钥A验证密码
    bUseKeyA = true;
    // 传递参数，该使用哪个秘钥验证?
    memcpy(mp.mpa.abtKey, keyA, sizeof(mp.mpa.abtKey));
    //调用秘钥验证函数验证扇区秘钥!
    int block = sectorToBlock(sector);
    jboolean result = (jboolean) authenticate((uint32_t) block);
    //释放内存，返回结果
    (*env)->ReleaseByteArrayElements(env, keyA_, keyA, 0);
    return result;
}

static jboolean _authWithKeyB(JNIEnv *env, jobject obj, jint sector, jbyteArray keyB_) {

    //TODO 重置读卡器参数,相当重要，如果不重置那么将会影响验证密码等操作
    // 配置CRC校验
    if (nfc_device_set_property_bool(pnd, NP_HANDLE_CRC, true) < 0) {
        nfc_perror(pnd, "nfc_device_set_property_bool");
        return false;
    }

    //关闭原生发送接收函数
    if (nfc_device_set_property_bool(pnd, NP_EASY_FRAMING, true) < 0) {
        nfc_perror(pnd, "nfc_device_set_property_bool");
        return false;
    }

    //重新连接
    _disconnect();

    jbyte *keyB = (*env)->GetByteArrayElements(env, keyB_, NULL);
    //指定标志位，使用秘钥B验证密码
    bUseKeyA = false;
    // 传递参数，该使用哪个秘钥验证?
    memcpy(mp.mpa.abtKey, keyB, sizeof(mp.mpa.abtKey));
    //调用秘钥验证函数验证扇区秘钥!
    int block = sectorToBlock(sector);
    jboolean result = (jboolean) authenticate((uint32_t) block);
    //释放内存，返回结果
    (*env)->ReleaseByteArrayElements(env, keyB_, keyB, 0);
    return result;
}

static void _setWriteUID(JNIEnv *env, jobject obj, jboolean trueIsWrite) {
    bWriteZero = trueIsWrite;
}

//TODO 读卡实现
static bool read_card(int block) {
    if (magic2) {
        ERR("zhush: This card does not require an unlocked write (R)\n");
    }
    if (nfc_initiator_mifare_cmd(pnd, MC_READ, block, &mp)) {
        return true;
    } else {
        ERR("failed to read block 0x%02x\n", block);
        return false;
    }
}

static jbyteArray _readBlock(JNIEnv *env, jobject obj, jint block) {
    //JNI环境初始化
    JNIEnv *tmpJniEnv = getJniEnv();
    //调用基础读取函数直接读取!
    if (read_card(block)) {
        int dataLen = sizeof(mp.mpd.abtData);
        jbyteArray byteArray = (*tmpJniEnv)->NewByteArray(tmpJniEnv, dataLen);
        (*tmpJniEnv)->SetByteArrayRegion(tmpJniEnv, byteArray, 0, dataLen,
                                         (const jbyte *) mp.mpd.abtData
        );
        return byteArray;
    } else {
        return NULL;
    }
}

static bool _write_card(int block, uint8_t *data) {
    if (magic2) {
        WARN("Note: This card does not require an unlocked write (W) \n");
    }
    if (!validateBlock(block)) {
        return false;
    }
    //检测我们是否在写尾部块
    if (is_trailer_block((uint32_t) block)) {

        memcpy(mp.mpt.abtKeyA, data, sizeof(mp.mpt.abtKeyA));
        //内存地址增加
        memcpy(mp.mpt.abtAccessBits, data + sizeof(mp.mpt.abtKeyA), sizeof(mp.mpt.abtAccessBits));
        //内存地址增加
        memcpy(mp.mpt.abtKeyB, data + sizeof(mp.mpt.abtKeyA) + sizeof(mp.mpt.abtAccessBits),
               sizeof(mp.mpt.abtKeyB));

        // 尝试写尾部块
        if (nfc_initiator_mifare_cmd(pnd, MC_WRITE, block, &mp) == false) {
            ERR("failed to write trailer block %d \n", block);
            return false;
        }
    } else {
        // Try to write the data block
        memcpy(mp.mpd.abtData, data, sizeof(mp.mpd.abtData));
        // 不要用不正确的BCC写块0——卡片将会无效！
        if (block == 0) {
            //是否允许写0扇区0块
            if (!bWriteZero) {
                ERR("必须要开启写0块");
                return false;
            }
            //判断0扇区0块BCC是否有效
            if ((mp.mpd.abtData[0] ^ mp.mpd.abtData[1] ^ mp.mpd.abtData[2] ^
                 mp.mpd.abtData[3] ^ mp.mpd.abtData[4]) != 0x00 && !magic2) {
                ERR("Expecting BCC=%02X",
                    mp.mpd.abtData[0] ^ mp.mpd.abtData[1] ^ mp.mpd.abtData[2] ^
                    mp.mpd.abtData[3]);
                return false;
            }
        }
        if (!nfc_initiator_mifare_cmd(pnd, MC_WRITE, (const uint8_t) block, &mp))
            return false;
    }
    return true;
}

static bool __write_card(JNIEnv *env, jobject obj, jint block, jbyteArray data_) {
    //JNI环境初始化
    JNIEnv *tmpJniEnv = getJniEnv();
    jbyte *data = (*tmpJniEnv)->GetByteArrayElements(tmpJniEnv, data_, NULL);
    bool result = _write_card(block, data);
    (*tmpJniEnv)->ReleaseByteArrayElements(tmpJniEnv, data_, data, 0);
    return (jboolean) result;
}

//TODO 最终实现注册关联两端函数
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {

    JNIEnv *jniEnv = NULL;

    //得到当前的jvm环境指针
    if ((*vm)->GetEnv(vm, (void **) &jniEnv, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }

    //在此处重定向printf之类的输出,因为这个时自定义API，因此不可以混合到其他控制台输出中
    /*freopen("/sdcard/NfcTools/pn53x/pn53x_mf_o.txt", "w", stdout);
    freopen("/sdcard/NfcTools/pn53x/pn53x_mf_e.txt", "w", stderr);
    setbuf(stdout, NULL);
    setbuf(stderr, NULL);*/

    //初始化全局的jvm函数指针
    (*jniEnv)->GetJavaVM(jniEnv, &g_JavaVM);

    //得到native函数的定义类
    jclass clazz = (*jniEnv)->FindClass(jniEnv, "com/rfidresearchgroup/natives/SpclMf");
    if (clazz == NULL) {
        return -1;
    }

    //构建和初始化函数结构体,分别是java层的函数名称，签名，对应的函数指针
    JNINativeMethod methods[] = {

            {"scanning",       "()Z",    (void *) _scanning},
            {"con",            "()Z",    (void *) _connect},
            {"unlock",         "()Z",    (void *) _unlock},
            {"uplock",         "()Z",    (void *) _uplock},

            {"getUid",         "()[B",   (void *) _getUid},
            {"getAtqa",        "()[B",   (void *) _getAtqa},
            {"getSak",         "()[B",   (void *) _getSak},
            {"getSize",        "()I",    (void *) _getSize},
            {"getSectorCount", "()I",    (void *) _getSectorCount},
            {"getBlockCount",  "()I",    (void *) _getBlockCount},

            {"isUnlock",       "()Z",    (void *) _isUnlock},
            {"isEmulated",     "()Z",    (void *) _isEmulated},


            {"readBlock",      "(I)[B",  (void *) _readBlock},
            {"writeBlock",     "(I[B)Z", (void *) __write_card},

            {"authWithKeyA",   "(I[B)Z", (void *) _authWithKeyA},
            {"authWithKeyB",   "(I[B)Z", (void *) _authWithKeyB},

            {"setWriteUID",    "(Z)V",   (void *) _setWriteUID},
            {"disconnect",     "()Z",    (void *) _disconnect}

    };

    //注册函数
    if ((*jniEnv)->RegisterNatives(jniEnv, clazz, methods, sizeof(methods) / sizeof(methods[0])) !=
        JNI_OK) {
        return -1;
    }
    //最后一定要返回jni的版本。
    return JNI_VERSION_1_4;
}