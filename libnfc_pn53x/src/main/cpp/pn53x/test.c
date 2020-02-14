//
// Created by DXL on 2019/2/11.
//
#include <stdio.h>
#include <nfc/nfc.h>
#include <zconf.h>
#include "tools.h"

jboolean openDev(JNIEnv *env, jobject instance) {
    nfc_context *context;
    nfc_device *pnd;
    nfc_init(&context);
    if (context == NULL) {
        LOGE("Unable to init libnfc (malloc)");
        return false;
    }
    //先检查是否有异常发生
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        (*env)->ThrowNew(env,
                         (*env)->FindClass(env, "java/io/IOException"), "操作出错!");
        nfc_exit(context);
        return false;
    }

    // Try to open the NFC reader
    pnd = nfc_open(context, NULL);
    if (pnd == NULL) {
        LOGE("Error opening NFC reader");
        //设备打不开则再次检查异常
        if ((*env)->ExceptionCheck(env)) {
            nfc_close(pnd);
            nfc_exit(context);
            (*env)->ExceptionDescribe(env);
            (*env)->ExceptionClear(env);
            (*env)->ThrowNew(env, (*env)->FindClass(env, "java/io/IOException"), "操作出错!");
        }
        return false;
    }
    //打开之后必须关闭!
    nfc_close(pnd);
    nfc_exit(context);
    //TODO 漏了这一句。。导致没有正确的返回值，
    //导致就算实际上连接了也无法判断连接不连接
    return true;
}

jboolean closeDev(JNIEnv *env, jobject instance) {
    return true;
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
    jclass clazz = (*jniEnv)->FindClass(jniEnv, "cn/rrg/devices/PN53X");
    if (clazz == NULL) {
        return -1;
    }
    //构建和初始化函数结构体,分别是java层的函数名称，签名，对应的函数指针
    JNINativeMethod methods[] = {
            {"testPN53x",  "()Z", (void *) openDev},
            {"closePN53x", "()Z", closeDev}
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
