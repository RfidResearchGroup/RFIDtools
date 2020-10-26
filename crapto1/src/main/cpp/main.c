#include <stdbool.h>
#include <ctype.h>
#include <inttypes.h>
#include <crapto1.h>
#include <stdio.h>
#include <malloc.h>
#include <string.h>
#include <jni.h>

//转为小写字符串!
char *strupr(char *str) {
    char *ret = str;
    char *tmp = ret;
    //测试长度!
    size_t len = strlen(tmp);
    while (len != 0 && tmp != NULL) {
        *str = (char) toupper(*tmp);
        --len;
        ++tmp;
        ++str;
    }
    return ret;
}

struct CHexMap {
    char chr;
    int value;
};
const int HexMapL = 16;
struct CHexMap HexMap[HexMapL] = {
        {'0', 0},
        {'1', 1},
        {'2', 2},
        {'3', 3},
        {'4', 4},
        {'5', 5},
        {'6', 6},
        {'7', 7},
        {'8', 8},
        {'9', 9},
        {'A', 10},
        {'B', 11},
        {'C', 12},
        {'D', 13},
        {'E', 14},
        {'F', 15}
};

//16进制字符串转整形!
uint32_t httoi(const char *value) {
    //拷贝一份新的!
    char *newCs = strdup(value);
    char *s = strupr(newCs);;
    uint32_t result = 0;
    if (*s == '0' && *(s + 1) == 'X') s += 2;
    bool firsttime = true;
    while (*s != '\0') {
        bool found = false;
        for (int i = 0; i < HexMapL; i++) {
            if (*s == HexMap[i].chr) {
                if (!firsttime) result <<= 4;
                result |= HexMap[i].value;
                found = true;
                break;
            }
        }
        if (!found) break;
        s++;
        firsttime = false;
    }
    //LOGD("尝试输出字符值: %s", mstr);
    free(newCs);
    return result;
}

//主函数，进行最终的线性反馈寄存器算法回滚!
uint64_t finalKey(const char *id, const char *tc, const char *rc, const char *rr, const char *tr) {
    struct Crypto1State *revstate;
    uint64_t lfsr;
    //获得输入框得UID
    uint32_t uid = httoi(id);
    //LOGD("测试输出ID ： %u", uid);
    //获得TAG得收集信息!
    uint32_t tag_challenge = httoi(tc);
    //LOGD("测试输出TC ： %u", tag_challenge);
    //获得ENC
    uint32_t nr_enc = httoi(rc);
    //LOGD("测试输出RC ： %u", nr_enc);
    //获得来自于读卡器得应答
    uint32_t reader_response = httoi(rr);
    //LOGD("测试输出RR ： %u", reader_response);
    //获得来自于标签得应答
    uint32_t tag_response = httoi(tr);
    //LOGD("测试输出TR ： %u", tag_response);
    //进行ks计算
    uint32_t ks2 = reader_response ^prng_successor(tag_challenge, 64);
    uint32_t ks3 = tag_response ^prng_successor(tag_challenge, 96);
    //调用线性反馈寄存器关键函数进行计算
    revstate = lfsr_recovery(ks2, ks3);
    //尝试回滚
    lfsr_rollback(revstate, 0, 0);
    //第二次尝试回滚
    lfsr_rollback(revstate, 0, 0);
    //通过nr尝试回滚
    lfsr_rollback(revstate, nr_enc, 1);
    //通过uid与关键信息进行回滚
    lfsr_rollback(revstate, uid ^ tag_challenge, 0);
    //得到关键密钥
    crypto1_get_lfsr(revstate, &lfsr);
    //回收内存!
    crypto1_destroy(revstate);
    //返回最终结果
    return lfsr;
}

//映射函数，两端映射，类型转换!
jstring
final(JNIEnv *env, jobject instance, jstring hexID, jstring hexTC, jstring hexRC, jstring hexRR,
      jstring hexTR) {
    if (hexID == NULL || hexTC == NULL || hexRC == NULL || hexRR == NULL || hexTR == NULL) {
        return NULL;
    }
    jboolean isCopy = false;
    //获得字符数组!
    const char *id = (*env)->GetStringUTFChars(env, hexID, &isCopy);
    //const jint idLen = (*env)->GetStringUTFLength(env, hexID);
    // LOGD("final ID: %s", id);
    const char *tc = (*env)->GetStringUTFChars(env, hexTC, &isCopy);
    //const jint tcLen = (*env)->GetStringUTFLength(env, hexTC);
    //LOGD("final TC: %s", tc);
    const char *rc = (*env)->GetStringUTFChars(env, hexRC, &isCopy);
    //const jint rcLen = (*env)->GetStringUTFLength(env, hexRC);
    //LOGD("final RC: %s", rc);
    const char *rr = (*env)->GetStringUTFChars(env, hexRR, &isCopy);
    //const jint rrLen = (*env)->GetStringUTFLength(env, hexRR);
    //LOGD("final RR: %s", rr);
    const char *tr = (*env)->GetStringUTFChars(env, hexTR, &isCopy);
    //const jint trLen = (*env)->GetStringUTFLength(env, hexTR);
    //LOGD("final TR: %s", tr);
    //调用主要的逻辑函数，进行计算!
    uint64_t key = finalKey(id, tc, rc, rr, tr);
    //谨记释放!
    (*env)->ReleaseStringUTFChars(env, hexID, id);
    (*env)->ReleaseStringUTFChars(env, hexTC, tc);
    (*env)->ReleaseStringUTFChars(env, hexRC, rc);
    (*env)->ReleaseStringUTFChars(env, hexRR, rr);
    (*env)->ReleaseStringUTFChars(env, hexTR, tr);
    //进行格式化转换!
    char keyChars[13] = {0};
    memset(keyChars, 0, sizeof(keyChars));
    sprintf(keyChars, "%012"PRIX64, key);
    //LOGD("输出测试的64位值: %lu", key);
    //LOGD("输出测试的密钥: %s", keyChars);
    return (*env)->NewStringUTF(env, keyChars);
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
    //得到native函数的定义类
    jclass clazz = (*jniEnv)->FindClass(jniEnv, "cn/dxl/crapto1/Crapto1");
    if (clazz == NULL) {
        return -1;
    }
    //构建和初始化函数结构体,分别是java层的函数名称，签名，对应的函数指针
    JNINativeMethod methods[] = {
            {"finalKeyNative", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", (void *) final},
    };
    //注册函数
    if ((*jniEnv)->RegisterNatives(jniEnv, clazz, methods,
                                   sizeof(methods) / sizeof(methods[0])) !=
        JNI_OK) {
        return -1;
    }
    //最后一定要返回jni的版本。
    return JNI_VERSION_1_4;
}