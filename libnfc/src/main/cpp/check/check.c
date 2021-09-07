//
// Created by DXL on 2019/2/11.
//
#include <stdio.h>
#include <nfc/nfc.h>
#include <zconf.h>
#include "tools.h"
#include "crapto1.h"

#define MAX_FRAME_LEN 264
#define MIFARE_CLASSIC_1K                       0x08    // MF1ICS50 Functional Specifications - 0x08
#define MIFARE_CLASSIC_1K_19                    0x19    // MF1ICS50 Functional Specifications - 0x19
#define MIFARE_CLASSIC_4K                       0x18    // MF1ICS70 Functional Specifications - 0x18
#define MIFARE_CLASSIC_1K_RATB                  0x88    // Infineon Licensed Mifare 1K = 0x88 (thanks JPS)
#define MIFARE_CLASSIC_4K_SKGT                  0x98    // Infineon Licensed Mifare 4K = 0x98???

#define IS_MIFARE_CLASSIC_1K(ats_sak)           ( ((ats_sak) == MIFARE_CLASSIC_1K) || ((ats_sak) == MIFARE_CLASSIC_1K_19) || ((ats_sak) == MIFARE_CLASSIC_1K_RATB) )
#define IS_MIFARE_CLASSIC_4K(ats_sak)           ( ((ats_sak) == MIFARE_CLASSIC_4K) || ((ats_sak) == MIFARE_CLASSIC_4K_SKGT) )

static const nfc_modulation nmMifare = {
        .nmt = NMT_ISO14443A,
        .nbr = NBR_106,
};

static bool select_tag(nfc_device *pnd, int iSleepAtFieldOFF, int iSleepAfterFieldON,
                       nfc_target_info *ti) {
    nfc_target ti_tmp;

    if (!pnd || !ti) {
        LOGE("some parameter are NULL");
        return false;
    }

    // Drop the field for a while, so the card can reset
    if (0 > nfc_device_set_property_bool(pnd, NP_ACTIVATE_FIELD, false)) {
        LOGE("configuring NP_ACTIVATE_FIELD");
        return false;
    }

    // {WPMCC09} 2.4. Tag nonces: "drop the field (for approximately 30us) to discharge all capacitors"
    sleep(iSleepAtFieldOFF);

    // Let the reader only try once to find a tag
    if (0 > nfc_device_set_property_bool(pnd, NP_INFINITE_SELECT, false)) {
        LOGE("configuring NP_INFINITE_SELECT");
        return false;
    }

    // Configure the CRC and Parity settings
    if (0 > nfc_device_set_property_bool(pnd, NP_HANDLE_CRC, true)) {
        LOGE("configuring NP_HANDLE_CRC");
        return false;
    }

    if (0 > nfc_device_set_property_bool(pnd, NP_HANDLE_PARITY, true)) {
        LOGE("configuring NP_HANDLE_PARITY");
        return false;
    }

    // Enable field so more power consuming cards can power themselves up
    if (0 > nfc_device_set_property_bool(pnd, NP_ACTIVATE_FIELD, true)) {
        LOGE("configuring NP_ACTIVATE_FIELD");
        return false;
    }

    // Switch the field back on, and wait for a constant amount of time before authenticating
    sleep(iSleepAfterFieldON);

    // Poll for a ISO14443A (MIFARE) tag
    if (0 >= nfc_initiator_select_passive_target(pnd, nmMifare, NULL, 0, &ti_tmp)) {
        LOGE("ERROR: connecting to MIFARE Classic tag");
        return false;
    }

    memcpy(ti, &ti_tmp, sizeof(ti_tmp));

    return true;
}

static uint32_t bswap_32_pu8(uint8_t *pu8) {
    // TODO: This function need to be tested on both endianness machine types
    return pu8[0] << 24 | pu8[1] << 16 | pu8[2] << 8 | pu8[3];
}

jboolean testDarkside(JNIEnv *env, jobject instance) {
    // libnfc related
    nfc_context *context;
    nfc_device *pnd;
    nfc_target target;

    // Try to open the NFC reader
    nfc_init(&context);
    pnd = nfc_open(context, NULL);

    // 判断设备是否可用!
    if (!pnd) {
        LOGE("pnd is invalid.");
        nfc_exit(context);
        return false;
    }

    LOGD("pnd is open succeed.");

    if (0 > nfc_initiator_init(pnd)) {
        LOGE("initializing NFC reader: %s\n", nfc_device_get_name(pnd));
        nfc_close(pnd);
        nfc_exit(context);
        return false;
    }

    LOGD("pnd is init succeed.");

    // Select tag and get tag info
    if (!select_tag(pnd, 0, 0, &target.nti)) {
        LOGE("selecting tag failed on the reader %s\n", nfc_device_get_name(pnd));
        nfc_close(pnd);
        nfc_exit(context);
        return false;
    }

    LOGD("tag is select succeed.");

    // 后判断卡的类型!
    if (!IS_MIFARE_CLASSIC_1K(target.nti.nai.btSak) &&
        !IS_MIFARE_CLASSIC_4K(target.nti.nai.btSak)) {
        LOGE("tagType is invalid.");
        nfc_close(pnd);
        nfc_exit(context);
        return false;
    }

    // 上述参数无异常，开始定义变量!
    uint8_t mf_auth[] = {0x60, 0x00, 0x00, 0x00};
    uint8_t mf_nr_ar[] = {0, 0, 0, 0, 0, 0, 0, 0};
    uint8_t abtRx[MAX_FRAME_LEN];
    uint8_t receivedAnswerPar[3] = {0x00};
    uint8_t par[1] = {0};

    // append crc to auth cmd!
    iso14443a_crc_append(mf_auth, 2);
    // init raw mode
    nfc_device_set_property_bool(pnd, NP_HANDLE_CRC, false);
    nfc_device_set_property_bool(pnd, NP_EASY_FRAMING, false);
    //request and get nonce!
    if (nfc_initiator_transceive_bytes(pnd, mf_auth, sizeof(mf_auth), abtRx, 4,
                                       -1)) {
        if (!validate_prng_nonce(bswap_32_pu8(abtRx))) {
            printf("PRNG no supported！");
            nfc_close(pnd);
            nfc_exit(context);
            return false;
        }
    } else {
        printf("\n\nFailed to get TAG NONCE!!!\n\n");
        nfc_close(pnd);
        nfc_exit(context);
        return false;
    }
    nfc_device_set_property_bool(pnd, NP_EASY_FRAMING, true);
    nfc_device_set_property_bool(pnd, NP_HANDLE_PARITY, false);
    //request "nack" if exists
    if ((nfc_initiator_transceive_bits(pnd, mf_nr_ar, 64, par, abtRx, sizeof(abtRx),
                                       receivedAnswerPar)) != 4) {
        LOGE("\n\nFailed to get TAG NACK!!!\n\n");
        LOGE("PRNG.NACK no supported！");
        nfc_close(pnd);
        nfc_exit(context);
        return false;
    }
    nfc_close(pnd);
    nfc_exit(context);
    return true;
}

jboolean testNested(JNIEnv *env, jobject instance) {
    // libnfc related
    nfc_context *context;
    nfc_device *pnd;
    nfc_target target;

    // Try to open the NFC reader
    nfc_init(&context);
    pnd = nfc_open(context, NULL);

    // 判断设备是否可用!
    if (!pnd) {
        LOGE("pnd is invalid.");
        nfc_exit(context);
        return false;
    }

    if (0 > nfc_initiator_init(pnd)) {
        LOGE("initializing NFC reader: %s\n", nfc_device_get_name(pnd));
        nfc_exit(context);
        nfc_close(pnd);
        return false;
    }

    // Select tag and get tag info
    if (!select_tag(pnd, 0, 0, &target.nti)) {
        LOGE("selecting tag on the reader %s\n", nfc_device_get_name(pnd));
        nfc_exit(context);
        nfc_close(pnd);
        return false;
    }

    // 后判断卡的类型!
    if (!IS_MIFARE_CLASSIC_1K(target.nti.nai.btSak) &&
        !IS_MIFARE_CLASSIC_4K(target.nti.nai.btSak)) {
        LOGE("tagType is invalid.");
        nfc_exit(context);
        nfc_close(pnd);
        return false;
    }
    // 上述参数无异常，开始定义变量!
    uint8_t mf_auth[] = {0x60, 0x00, 0x00, 0x00};
    uint8_t abtRx[MAX_FRAME_LEN];

    // append crc to auth cmd!
    iso14443a_crc_append(mf_auth, 2);
    // init raw mode
    nfc_device_set_property_bool(pnd, NP_HANDLE_CRC, false);
    nfc_device_set_property_bool(pnd, NP_EASY_FRAMING, false);
    //request and get nonce!
    if (nfc_initiator_transceive_bytes(pnd, mf_auth, sizeof(mf_auth), abtRx, 4,
                                       -1)) {
        if (!validate_prng_nonce(bswap_32_pu8(abtRx))) {
            LOGE("PRNG no supported！");
            nfc_exit(context);
            nfc_close(pnd);
            return false;
        }
    } else {
        LOGE("\n\nFailed to get TAG NONCE!!!\n\n");
        nfc_exit(context);
        nfc_close(pnd);
        return false;
    }
    nfc_exit(context);
    nfc_close(pnd);
    return true;
}

jboolean testHardnested(JNIEnv *env, jobject instance) {
    return (jboolean) !testNested(env, instance);
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *jniEnv = NULL;
    //得到当前的jvm环境指针
    if ((*vm)->GetEnv(vm, (void **) &jniEnv, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    //初始化全局的jvm函数指针
    (*jniEnv)->GetJavaVM(jniEnv, &g_JavaVM);
    //得到native函数的定义类
    jclass clazz = (*jniEnv)->FindClass(jniEnv, "com/rfidresearchgroup/natives/PN53XTagLeaksAdapter");
    if (clazz == NULL) {
        return -1;
    }
    //构建和初始化函数结构体,分别是java层的函数名称，签名，对应的函数指针
    JNINativeMethod methods[] = {
            {"isDarksideSupported",   "()Z", testDarkside},
            {"isNestedSupported",     "()Z", testNested},
            {"isHardnestedSupported", "()Z", testHardnested}
    };
    //注册函数
    if ((*jniEnv)->RegisterNatives(jniEnv, clazz, methods, sizeof(methods) / sizeof(methods[0])) !=
        JNI_OK) {
        return -1;
    }
    //LOGD("test注册完成!");
    //最后一定要返回jni的版本。
    return JNI_VERSION_1_4;
}
