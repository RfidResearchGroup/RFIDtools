#include <stdio.h>
#include <stdbool.h>
#include <string.h>
#include <inttypes.h>
#include <stdlib.h>
#include <time.h>
#include "crapto1.h"
#include "tools.h"
#include "util_posix.h"

static volatile bool isRunning = false;
static volatile bool stop_label = false;

int main64(int argc, char *argv[]) {
    uint32_t uid;     // serial numDber
    uint32_t nt;      // tag challenge
    uint32_t nr_enc;  // encrypted reader challenge
    uint32_t ar_enc;  // encrypted reader response
    uint32_t at_enc;  // encrypted tag response
    uint64_t key = 0;                // recovered key
    struct Crypto1State *revstate;
    uint32_t ks2;                        // keystream used to encrypt reader response
    uint32_t ks3;                        // keystream used to encrypt tag response

    printf("MIFARE Classic key recovery - based on 64 bits of keystream\n");
    printf("Recover key from only one complete authentication!\n\n");

    if (argc < 6) {
        printf(" syntax: %s <uid> <nt> <{nr}> <{ar}> <{at}> [enc] [enc...]\n\n", argv[0]);
        return 1;
    }

    int encc = argc - 6;
    int enclen[encc];
    uint8_t enc[encc][120];

    sscanf(argv[1], "%x", &uid);
    sscanf(argv[2], "%x", &nt);
    sscanf(argv[3], "%x", &nr_enc);
    sscanf(argv[4], "%x", &ar_enc);
    sscanf(argv[5], "%x", &at_enc);
    for (int i = 0; i < encc; i++) {
        enclen[i] = strlen(argv[i + 6]) / 2;
        for (int i2 = 0; i2 < enclen[i]; i2++) {
            sscanf(argv[i + 6] + i2 * 2, "%2x", (unsigned int *) &enc[i][i2]);
        }
    }

    printf("Recovering key for:\n");
    printf("   uid: %08x\n", uid);
    printf("    nt: %08x\n", nt);
    printf("  {nr}: %08x\n", nr_enc);
    printf("  {ar}: %08x\n", ar_enc);
    printf("  {at}: %08x\n", at_enc);
    for (int i = 0; i < encc; i++) {
        printf("{enc%d}: ", i);
        for (int i2 = 0; i2 < enclen[i]; i2++) {
            printf("%02x", enc[i][i2]);
        }
        printf("\n");
    }

    printf("\nLFSR successors of the tag challenge:\n");
    printf("  nt' : %08x\n", prng_successor(nt, 64));
    printf("  nt'': %08x\n", prng_successor(nt, 96));

    // Extract the keystream from the messages
    ks2 = ar_enc ^ prng_successor(nt, 64);
    ks3 = at_enc ^ prng_successor(nt, 96);

    uint64_t start_time = msclock();
    revstate = lfsr_recovery64(ks2, ks3);
    uint64_t time_spent = msclock() - start_time;
    printf("Time spent in lfsr_recovery64(): %1.2f seconds\n", (float) time_spent / 1000.0);
    printf("\nKeystream used to generate {ar} and {at}:\n");
    printf("   ks2: %08x\n", ks2);
    printf("   ks3: %08x\n", ks3);

    // Decrypting communication using keystream if presented
    if (argc > 6) {
        printf("\nDecrypted communication:\n");
        uint8_t ks4;
        int rollb = 0;
        for (int i = 0; i < encc; i++) {
            printf("{dec%d}: ", i);
            for (int i2 = 0; i2 < enclen[i]; i2++) {
                ks4 = crypto1_byte(revstate, 0, 0);
                printf("%02x", ks4 ^ enc[i][i2]);
                rollb += 1;
            }
            printf("\n");
        }
        for (int i = 0; i < rollb; i++) {
            lfsr_rollback_byte(revstate, 0, 0);
        }
    }

    lfsr_rollback_word(revstate, 0, 0);
    lfsr_rollback_word(revstate, 0, 0);
    lfsr_rollback_word(revstate, nr_enc, 1);
    lfsr_rollback_word(revstate, uid ^ nt, 0);
    crypto1_get_lfsr(revstate, &key);
    crypto1_destroy(revstate);

    printf("\nFound Key: [%012" PRIx64"]\n\n", key);

}

/*
 * 执行命令!
 * */
jint startExecute(JNIEnv *env, jobject instance, jstring command_) {
    //初始化JNIENV，这是必须的！
    JNIEnv *tmpJniEnv = getJniEnv();
    //jboolean类型，控制是否为拷贝
    jboolean isCopy = 1;
    const char *command = (*tmpJniEnv)->GetStringUTFChars(tmpJniEnv, command_, &isCopy);
    //解析从JAVA端传入的命令
    CMD *cmd = parse_command_line(command);
    if (cmd->len < 2) {
        main64(0, cmd->cmd);
        return EXIT_FAILURE;
    }
    //释放内存
    (*tmpJniEnv)->ReleaseStringUTFChars(tmpJniEnv, command_, command);
    recovery_getopt_opt();
    isRunning = true;
    stop_label = false;
    printf("\n------------Begin------------\n");
    //TODO 记录开始执行的时间戳!
    time_t start_time = time(NULL);
    int ret = main64(cmd->len, cmd->cmd);
    time_t end_time = time(NULL);
    printf("\nINFO: execute time(s): %lf\n", difftime(end_time, start_time));
    printf("\n------------End------------\n");
    isRunning = false;
    stop_label = false;
    free_command_line(cmd);
    return ret;
}

/*
 * 停止mfcuk
 * */
void stopExecute(JNIEnv *env, jobject instance) {
    stop_label = true;
}

/*
 * 是否在执行当中
 * */
jboolean isExecuting(JNIEnv *env, jobject instance) {
    return (jboolean) isRunning;
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
    jclass clazz = (*jniEnv)->FindClass(jniEnv, "cn/rrg/mfkey/NativeMfKey64");
    if (clazz == NULL) {
        return -1;
    }
//构建和初始化函数结构体,分别是java层的函数名称，签名，对应的函数指针
    JNINativeMethod methods[] = {
            {"startExecute", "(Ljava/lang/String;)I", (void *) startExecute},
            {"isExecuting",  "()Z",                   (void *) isExecuting},
            {"stopExecute",  "()V",                   (void *) stopExecute}
    };
//注册函数
    if ((*jniEnv)->RegisterNatives(jniEnv, clazz, methods, sizeof(methods) / sizeof(methods[0])) !=
        JNI_OK) {
        return -1;
    }
//最后一定要返回jni的版本。
    return JNI_VERSION_1_4;
}