#include <inttypes.h>
#include "crapto1.h"
#include <stdio.h>
#include <stdlib.h>
#include <tools.h>
#include <time.h>


uint64_t recoveryKey4Int(uint32_t uid,  // serial number
                         uint32_t nt0,    // tag challenge first
                         uint32_t nt1,  // tag challenge second
                         uint32_t nr0_enc,   // first encrypted reader challenge
                         uint32_t ar0_enc,   // first encrypted reader response
                         uint32_t nr1_enc,  // second encrypted reader challenge
                         uint32_t ar1_enc,// second encrypted reader response
                         bool *isSuccess // recovery result.
) {
    struct Crypto1State *s, *t;
    uint64_t key = 0;     // recovered key
    //uint32_t ks2;     // keystream used to encrypt reader response

    //  LOGD("Recovering key for:\n");
    //  LOGD("    uid: %08x\n", uid);
    //  LOGD("   nt_0: %08x\n", nt0);
    //  LOGD(" {nr_0}: %08x\n", nr0_enc);
    //  LOGD(" {ar_0}: %08x\n", ar0_enc);
    //  LOGD("   nt_1: %08x\n", nt1);
    //  LOGD(" {nr_1}: %08x\n", nr1_enc);
    //  LOGD(" {ar_1}: %08x\n", ar1_enc);

    // Generate lfsr succesors of the tag challenge
    //LOGD("\nLFSR succesors of the tag challenge:\n");
    uint32_t p64 = prng_successor(nt0, 64);
    uint32_t p64b = prng_successor(nt1, 64);

    //LOGD("  nt': %08x\n", p64);
    //LOGD(" nt'': %08x\n", prng_successor(p64, 32));

    // Extract the keystream from the messages
    //LOGD("\nKeystream used to generate {ar} and {at}:\n");
    //ks2 = ar0_enc ^ p64;
    //LOGD("  ks2: %08x\n", ks2);

    s = lfsr_recovery32(ar0_enc ^ p64, 0);

    for (t = s; t->odd | t->even; ++t) {
        lfsr_rollback_word(t, 0, 0);
        lfsr_rollback_word(t, nr0_enc, 1);
        lfsr_rollback_word(t, uid ^ nt0, 0);
        crypto1_get_lfsr(t, &key);

        crypto1_word(t, uid ^ nt1, 0);
        crypto1_word(t, nr1_enc, 1);
        if (ar1_enc == (crypto1_word(t, 0, 0) ^ p64b)) {
            LOGD("\nFound Key: [%012"
                         PRIx64
                         "]\n\n", key);
            *isSuccess = true;
            break;
        }
    }
    free(s);
    return key;
}

uint64_t recoveryKey(const char *argv[], bool *isSuccess) {
    //struct Crypto1State *s, *t;
    //uint64_t key;     // recovered key
    uint32_t uid;     // serial number
    uint32_t nt0;      // tag challenge first
    uint32_t nt1;      // tag challenge second
    uint32_t nr0_enc; // first encrypted reader challenge
    uint32_t ar0_enc; // first encrypted reader response
    uint32_t nr1_enc; // second encrypted reader challenge
    uint32_t ar1_enc; // second encrypted reader response
    //uint32_t ks2;     // keystream used to encrypt reader response

    sscanf(argv[1], "%x", &uid);
    sscanf(argv[2], "%x", &nt0);
    sscanf(argv[3], "%x", &nr0_enc);
    sscanf(argv[4], "%x", &ar0_enc);
    sscanf(argv[5], "%x", &nt1);
    sscanf(argv[6], "%x", &nr1_enc);
    sscanf(argv[7], "%x", &ar1_enc);

    return recoveryKey4Int(uid, nt0, nt1, nr0_enc, ar0_enc, nr1_enc, ar1_enc, isSuccess);
}

jstring
decrypt4Ints(JNIEnv *env, jobject instance,
             jint uid,
             jint nt0,
             jint nr0,
             jint ar0,
             jint nt1,
             jint nr1,
             jint ar1) {
    bool isSuccess = false;
    uint64_t key = recoveryKey4Int((uint32_t) uid, (uint32_t) nt0, (uint32_t) nt1, (uint32_t) nr0,
                                   (uint32_t) ar0, (uint32_t) nr1, (uint32_t) ar1, &isSuccess);
    if (isSuccess) {
        char ret[13] = {0};
        sprintf(ret, "%012"PRIx64, key);
        return (*env)->NewStringUTF(env, ret);
    } else {
        return NULL;
    }
}

jstring
decrypt4Strs(JNIEnv *env, jobject instance,
             jstring uid,
             jstring nt0,
             jstring nr0,
             jstring ar0,
             jstring nt1,
             jstring nr1,
             jstring ar1) {
    //先将字符串转换为HEX字符!
    const char *uidChars = (*env)->GetStringUTFChars(env, uid, 0);
    const char *nt0Chars = (*env)->GetStringUTFChars(env, nt0, 0);
    const char *nr0Chars = (*env)->GetStringUTFChars(env, nr0, 0);
    const char *ar0Chars = (*env)->GetStringUTFChars(env, ar0, 0);
    const char *nt1Chars = (*env)->GetStringUTFChars(env, nt1, 0);
    const char *nr1Chars = (*env)->GetStringUTFChars(env, nr1, 0);
    const char *ar1Chars = (*env)->GetStringUTFChars(env, ar1, 0);

    const char *array[] = {
            uidChars,
            nt0Chars,
            nr0Chars,
            ar0Chars,
            nt1Chars,
            nr1Chars,
            ar1Chars
    };

    bool isSuccess = false;
    uint64_t key = recoveryKey(array, &isSuccess);
    if (isSuccess) {
        char ret[13] = {0};
        sprintf(ret, "%012"PRIx64, key);
        return (*env)->NewStringUTF(env, ret);
    } else {
        return NULL;
    }
}

/*
 * jvm加载so时的映射回调
 * */
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *jniEnv = NULL;
//得到当前的jvm环境指针
    if ((*vm)->GetEnv(vm, (void **) &jniEnv, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
//初始化全局的jvm函数指针
    (*jniEnv)->GetJavaVM(jniEnv, &g_JavaVM);
//得到native函数的定义类
    jclass clazz = (*jniEnv)->FindClass(jniEnv, "cn/dxl/mfkey/NativeMfKey32V2");
    if (clazz == NULL) {
        return -1;
    }
//构建和初始化函数结构体,分别是java层的函数名称，签名，对应的函数指针
    JNINativeMethod methods[] = {
            {"decrypt4IntParams", "(IIIIIII)Ljava/lang/String;", (void *) decrypt4Ints},
            {"decrypt4StrParams", "(Ljava/lang/String;"
                                  "Ljava/lang/String;"
                                  "Ljava/lang/String;"
                                  "Ljava/lang/String;"
                                  "Ljava/lang/String;"
                                  "Ljava/lang/String;"
                                  "Ljava/lang/String;)"
                                  "Ljava/lang/String;",          (void *) decrypt4Strs}
    };
//注册函数
    if ((*jniEnv)->RegisterNatives(jniEnv, clazz, methods, sizeof(methods) / sizeof(methods[0])) !=
        JNI_OK) {
        return -1;
    }
//最后一定要返回jni的版本。
    return JNI_VERSION_1_4;
}
