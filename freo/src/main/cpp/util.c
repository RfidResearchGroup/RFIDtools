//
// Created by DXL on 2019/3/4.
//
#include <stdio.h>
#include <stdbool.h>
#include <zconf.h>
#include <malloc.h>
#include <jni.h>

/*
 * 全局的重定向文件句柄缓存!
 * */
static FILE *ofp;
static FILE *efp;
static FILE *ifp;

/*
 * 从文件指针获得文件路径!
 * *//*
static char *GetPathFromFP(FILE *FP) {
    if (!FP) return NULL;
    //取得文件描述符!
    int fdi = fileno(FP);
    char proclnk[512] = {0};
    char path[512] = {0};
    //进行链接路径拼接!
    sprintf(proclnk, "/proc/self/fd/%d", fdi);
    ssize_t r = readlink(proclnk, path, sizeof(path));
    //LOGD("底层打印ResetStdEO路径: %s", path);
    if (r <= 0) {
        //LOGD("GetPathFromFP获取路径失败!");
        return NULL;
    }
    return strdup(path);
}*/

/*
 * 标准输出重定向!
 * */
jboolean SetStdEO(JNIEnv *env, jclass clz, jstring file, jint type) {
    //判断传入的参数的可用性!
    if (!file) return false;
    jboolean isCopy = false;
    //解析为char *;
    const char *_file = (*env)->GetStringUTFChars(env, file, &isCopy);
    //尝试打开文件
    switch (type) {
        case 0:
            //尝试关闭句柄!
            if (ofp) {
                fclose(ofp);
                ofp = NULL;
            }
            if (!(ofp = freopen(_file, "w+", stdout))) {
                //谨记释放内存!
                (*env)->ReleaseStringUTFChars(env, file, _file);
                return false;
            }
            //TODO 非常重要，必须清空文件!
            ftruncate(fileno(ofp), 0);
            setbuf(stdout, NULL);
            break;
        case 1:
            //尝试关闭句柄!
            if (efp) {
                fclose(efp);
                efp = NULL;
            }
            if (!(efp = freopen(_file, "w+", stderr))) {
                //谨记释放内存!
                (*env)->ReleaseStringUTFChars(env, file, _file);
                return false;
            }
            //TODO 非常重要，必须清空文件!
            ftruncate(fileno(efp), 0);
            setbuf(stderr, NULL);
            break;
        default:
            //谨记释放内存!
            (*env)->ReleaseStringUTFChars(env, file, _file);
            return false;
    }
    //谨记释放内存!
    (*env)->ReleaseStringUTFChars(env, file, _file);
    return true;
}

/*
 * 标准输出重定向!
 * */
jboolean SetStdIN(JNIEnv *env, jclass clz, jstring file) {
    //判断传入的参数的可用性!
    if (!file) return false;
    jboolean isCopy = false;
    //解析为char *;
    const char *_file = (*env)->GetStringUTFChars(env, file, &isCopy);
    //尝试打开文件
    //尝试关闭句柄!
    if (ifp) {
        fclose(ifp);
        ifp = NULL;
    }
    if (!(ifp = freopen(_file, "w+", stdin))) {
        //谨记释放内存!
        (*env)->ReleaseStringUTFChars(env, file, _file);
        return false;
    }
    //TODO 非常重要，必须清空文件!
    ftruncate(fileno(ifp), 0);
    setbuf(stdin, NULL);
    //谨记释放内存!
    (*env)->ReleaseStringUTFChars(env, file, _file);
    return true;
}

/*
 * 标准输出重定向!
 * */
jboolean Close(JNIEnv *env, jclass clz, jstring file, jint type) {
    //判断传入的参数的可用性!
    if (!file) return false;
    jboolean isCopy = false;
    //解析为char *;
    const char *_file = (*env)->GetStringUTFChars(env, file, &isCopy);
    bool ret = true;
    //尝试打开文件
    switch (type) {
        case 0:
            //尝试关闭句柄!
            if (ofp) {
                if (fclose(ofp) == 0) {
                    ret = false;
                } else {
                    ret = true;
                }
                ofp = NULL;
            }
            break;
        case 1:
            //尝试关闭句柄!
            if (efp) {
                if (fclose(efp) == 0) {
                    ret = false;
                } else {
                    ret = true;
                }
                efp = NULL;
            }
            break;
        case 2:
            //尝试关闭句柄!
            if (ifp) {
                if (fclose(ifp) == 0) {
                    ret = false;
                } else {
                    ret = true;
                }
                ifp = NULL;
            }
            break;
        default:
            //谨记释放内存!
            (*env)->ReleaseStringUTFChars(env, file, _file);
            return false;
    }
    //谨记释放内存!
    (*env)->ReleaseStringUTFChars(env, file, _file);
    return (jboolean) ret;
}

//清空标准输入缓冲区!
void clearStdIN(JNIEnv *env, jclass clz) {
    if (ifp) {
        //在循环中读取有效字符，相当于清空缓冲区!
        char tmp[1];
        int fd = fileno(ifp);
        if (fd != -1) {
            while (read(fd, tmp, 1) > 0);
        }
    }
}

/*
 * jvm加载so时的映射回调
 * */
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *jniEnv = NULL;
    //得到当前的jvm环境指针
    if ((*vm)->GetEnv(vm, (void **) &jniEnv, JNI_VERSION_1_4) != JNI_OK) return -1;
    //初始化全局的jvm函数指针
    (*jniEnv)->GetJavaVM(jniEnv, &vm);
    //得到native函数的定义类
    jclass clazz = (*jniEnv)->FindClass(jniEnv, "cn/rrg/freo/Freopen");
    if (clazz == NULL) {
        return -1;
    }
    //构建和初始化函数结构体,分别是java层的函数名称，签名，对应的函数指针
    JNINativeMethod methods[] = {
            {"setStdEO",   "(Ljava/lang/String;I)Z", (void *) SetStdEO},
            {"setStdIN",   "(Ljava/lang/String;)Z",  (void *) SetStdIN},
            {"clearStdIN", "()V",                    (void *) clearStdIN},
            {"close",      "(Ljava/lang/String;I)Z", (void *) Close},
    };
    //注册函数
    if ((*jniEnv)->RegisterNatives(jniEnv, clazz, methods, sizeof(methods) / sizeof(methods[0])) !=
        JNI_OK) {
        return -1;
    }
    //最后一定要返回jni的版本。
    return JNI_VERSION_1_4;
}