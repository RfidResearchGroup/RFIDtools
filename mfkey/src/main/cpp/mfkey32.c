#include <inttypes.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <jni.h>
#include "crapto1.h"
#include "mfkey.h"
#include "util_posix.h"
#include "tools.h"

jstring
decrypt4Ints(JNIEnv *env, jobject instance,
             jint uid,
             jint nt0,
             jint nr0,
             jint ar0,
             jint nt1,
             jint nr1,
             jint ar1) {
    nonces_t data = {0};
    uint64_t key;     // recovered key
    //判断是否可以忽略nt0
    bool moebius_attack = nt0 != nt1;
    data.cuid = (uint32_t) uid;
    data.nonce = (uint32_t) nt0;
    data.nonce2 = data.nonce;
    data.nr = (uint32_t) nr0;
    data.ar = (uint32_t) ar0;
    //注: 此处系命名规范问题，本人是从0下标命名!
    if (moebius_attack) {
        data.nonce2 = (uint32_t) nt1;
        data.nr2 = (uint32_t) nr1;
        data.ar2 = (uint32_t) ar1;
    } else {
        data.nr2 = (uint32_t) nr1;
        data.ar2 = (uint32_t) ar1;
    }
    bool success;
    if (moebius_attack) {
        success = mfkey32_moebius(data, &key);
    } else {
        success = mfkey32(data, &key);
    }
    if (success) {
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

    nonces_t data = {0};
    uint64_t key;     // recovered key

    //判断两个字符串是否为相同，也就是两个随机数是否相同!
    bool moebius_attack = strcmp(nt0Chars, nt1Chars) == 0;

    sscanf(uidChars, "%x", &data.cuid);
    sscanf(nt0Chars, "%x", &data.nonce);
    data.nonce2 = data.nonce;
    sscanf(nr0Chars, "%x", &data.nr);
    sscanf(ar0Chars, "%x", &data.ar);
    if (moebius_attack) {
        sscanf(nt1Chars, "%x", &data.nonce2);
        sscanf(nr1Chars, "%x", &data.nr2);
        sscanf(ar1Chars, "%x", &data.ar2);
    } else {
        sscanf(nr1Chars, "%x", &data.nr2);
        sscanf(ar1Chars, "%x", &data.ar2);
    }

    //TODO 谨记释放本地引用!避免栈溢出!
    (*env)->ReleaseStringUTFChars(env, uid, uidChars);
    (*env)->ReleaseStringUTFChars(env, nt0, nt0Chars);
    (*env)->ReleaseStringUTFChars(env, nr0, nr0Chars);
    (*env)->ReleaseStringUTFChars(env, ar0, ar0Chars);
    (*env)->ReleaseStringUTFChars(env, nt1, nt1Chars);
    (*env)->ReleaseStringUTFChars(env, nr1, nr1Chars);
    (*env)->ReleaseStringUTFChars(env, ar1, ar1Chars);

    bool success;
    if (moebius_attack) {
        success = mfkey32_moebius(data, &key);
    } else {
        success = mfkey32(data, &key);
    }
    if (success) {
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
    jclass clazz = (*jniEnv)->FindClass(jniEnv, "com/dxl/mfkey/NativeMfKey32");
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
