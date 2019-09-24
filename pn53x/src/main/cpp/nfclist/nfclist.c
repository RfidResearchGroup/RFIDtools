//
// Created by DXL on 2019/4/4.
//
/*-
 * Free/Libre Near Field Communication (NFC) library
 *
 * Libnfc historical contributors:
 * Copyright (C) 2009      Roel Verdult
 * Copyright (C) 2009-2013 Romuald Conty
 * Copyright (C) 2010-2012 Romain Tartière
 * Copyright (C) 2010-2013 Philippe Teuwen
 * Copyright (C) 2012-2013 Ludovic Rousseau
 * See AUTHORS file for a more comprehensive list of contributors.
 * Additional contributors of this file:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1) Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  2 )Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Note that this license only applies on the examples, NFC library itself is under LGPL
 *
 */

/**
 * @file nfc-list.c
 * @brief Lists the first target present of each founded device
 */

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif // HAVE_CONFIG_H

#include <err.h>
#include <stdio.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>

#include <nfc/nfc.h>
#include <tools.h>
#include <time.h>

#include "nfc/nfc-utils.h"

#define MAX_DEVICE_COUNT 16
#define MAX_TARGET_COUNT 16

static nfc_device *pnd;

static volatile bool isRunning = false;
static volatile bool stop_label = false;

static void
print_usage(const char *progname) {
    printf("usage: %s [-v] [-t X]\n", progname);
    printf("  -v\t verbose display\n");
    printf("  -t X\t poll only for types according to bitfield X:\n");
    printf("\t   1: ISO14443A\n");
    printf("\t   2: Felica (212 kbps)\n");
    printf("\t   4: Felica (424 kbps)\n");
    printf("\t   8: ISO14443B\n");
    printf("\t  16: ISO14443B'\n");
    printf("\t  32: ISO14443B-2 ST SRx\n");
    printf("\t  64: ISO14443B-2 ASK CTx\n");
    printf("\t 128: ISO14443A-3 Jewel\n");
    printf("\t 256: ISO14443A-2 NFC Barcode\n");
    printf("\tSo 511 (default) polls for all types.\n");
    printf("\tNote that if 16, 32 or 64 then 8 is selected too.\n");
}

int
main(int argc, const char *argv[]) {
    (void) argc;
    const char *acLibnfcVersion;
    size_t i;
    bool verbose = false;
    int res = 0;
    int mask = 0x1ff;
    int arg;

    nfc_context *context;
    nfc_init(&context);
    if (context == NULL) {
        ERR("Unable to init libnfc (malloc)");
        return EXIT_FAILURE;
    }

    // Display libnfc version
    acLibnfcVersion = nfc_version();
    printf("%s uses libnfc %s\n", argv[0], acLibnfcVersion);

    // Get commandline options
    for (arg = 1; arg < argc; arg++) {
        if (0 == strcmp(argv[arg], "-h")) {
            print_usage(argv[0]);
            return EXIT_SUCCESS;
        } else if (0 == strcmp(argv[arg], "-v")) {
            verbose = true;
        } else if ((0 == strcmp(argv[arg], "-t")) && (arg + 1 < argc)) {
            arg++;
            mask = atoi(argv[arg]);
            if ((mask < 1) || (mask > 0x1ff)) {
                ERR("%i is invalid value for type bitfield.", mask);
                print_usage(argv[0]);
                return EXIT_FAILURE;
            }
            // Force TypeB for all derivatives of B
            if (mask & 0x70)
                mask |= 0x08;
        } else {
            ERR("%s is not supported option.", argv[arg]);
            print_usage(argv[0]);
            return EXIT_FAILURE;
        }
    }

    /* Lazy way to open an NFC devices */
#if 0
    pnd = nfc_open(context, NULL);
#endif

    /* Use connection string if specific devices is wanted,
     * i.e. PN532 UART devices on /dev/ttyUSB1 */
#if 0
    pnd = nfc_open(context, "pn532_uart:/dev/ttyUSB1");
#endif

    nfc_connstring connstrings[MAX_DEVICE_COUNT];
    size_t szDeviceFound = nfc_list_devices(context, connstrings, MAX_DEVICE_COUNT);

    if (szDeviceFound == 0) {
        printf("No NFC devices found.\n");
    }

    for (i = 0; i < szDeviceFound; i++) {
        if (stop_label)
            return EXIT_SUCCESS;
        nfc_target ant[MAX_TARGET_COUNT];
        pnd = nfc_open(context, connstrings[i]);

        if (pnd == NULL) {
            ERR("Unable to open NFC devices: %s", connstrings[i]);
            continue;
        }
        if (nfc_initiator_init(pnd) < 0) {
            nfc_perror(pnd, "nfc_initiator_init");
            nfc_exit(context);
            return EXIT_FAILURE;
        }

        printf("NFC devices: %s opened\n", nfc_device_get_name(pnd));

        nfc_modulation nm;

        if (mask & 0x1) {
            nm.nmt = NMT_ISO14443A;
            nm.nbr = NBR_106;
            // List ISO14443A targets
            if ((res = nfc_initiator_list_passive_targets(pnd, nm, ant, MAX_TARGET_COUNT)) >= 0) {
                int n;
                if (verbose || (res > 0)) {
                    printf("%d ISO14443A passive target(s) found%s\n", res,
                           (res == 0) ? ".\n" : ":");
                }
                for (n = 0; n < res; n++) {
                    print_nfc_target(&ant[n], verbose);
                    printf("\n");
                }
            }
        }

        if (mask & 0x02) {
            nm.nmt = NMT_FELICA;
            nm.nbr = NBR_212;
            // List Felica tags
            if ((res = nfc_initiator_list_passive_targets(pnd, nm, ant, MAX_TARGET_COUNT)) >= 0) {
                int n;
                if (verbose || (res > 0)) {
                    printf("%d Felica (212 kbps) passive target(s) found%s\n", res,
                           (res == 0) ? ".\n" : ":");
                }
                for (n = 0; n < res; n++) {
                    print_nfc_target(&ant[n], verbose);
                    printf("\n");
                }
            }
        }

        if (mask & 0x04) {
            nm.nmt = NMT_FELICA;
            nm.nbr = NBR_424;
            if ((res = nfc_initiator_list_passive_targets(pnd, nm, ant, MAX_TARGET_COUNT)) >= 0) {
                int n;
                if (verbose || (res > 0)) {
                    printf("%d Felica (424 kbps) passive target(s) found%s\n", res,
                           (res == 0) ? ".\n" : ":");
                }
                for (n = 0; n < res; n++) {
                    print_nfc_target(&ant[n], verbose);
                    printf("\n");
                }
            }
        }

        if (mask & 0x08) {
            nm.nmt = NMT_ISO14443B;
            nm.nbr = NBR_106;
            // List ISO14443B targets
            if ((res = nfc_initiator_list_passive_targets(pnd, nm, ant, MAX_TARGET_COUNT)) >= 0) {
                int n;
                if (verbose || (res > 0)) {
                    printf("%d ISO14443B passive target(s) found%s\n", res,
                           (res == 0) ? ".\n" : ":");
                }
                for (n = 0; n < res; n++) {
                    print_nfc_target(&ant[n], verbose);
                    printf("\n");
                }
            }
        }

        if (mask & 0x10) {
            nm.nmt = NMT_ISO14443BI;
            nm.nbr = NBR_106;
            // List ISO14443B' targets
            if ((res = nfc_initiator_list_passive_targets(pnd, nm, ant, MAX_TARGET_COUNT)) >= 0) {
                int n;
                if (verbose || (res > 0)) {
                    printf("%d ISO14443B' passive target(s) found%s\n", res,
                           (res == 0) ? ".\n" : ":");
                }
                for (n = 0; n < res; n++) {
                    print_nfc_target(&ant[n], verbose);
                    printf("\n");
                }
            }
        }

        if (mask & 0x20) {
            nm.nmt = NMT_ISO14443B2SR;
            nm.nbr = NBR_106;
            // List ISO14443B-2 ST SRx family targets
            if ((res = nfc_initiator_list_passive_targets(pnd, nm, ant, MAX_TARGET_COUNT)) >= 0) {
                int n;
                if (verbose || (res > 0)) {
                    printf("%d ISO14443B-2 ST SRx passive target(s) found%s\n", res,
                           (res == 0) ? ".\n" : ":");
                }
                for (n = 0; n < res; n++) {
                    print_nfc_target(&ant[n], verbose);
                    printf("\n");
                }
            }
        }

        if (mask & 0x40) {
            nm.nmt = NMT_ISO14443B2CT;
            nm.nbr = NBR_106;
            // List ISO14443B-2 ASK CTx family targets
            if ((res = nfc_initiator_list_passive_targets(pnd, nm, ant, MAX_TARGET_COUNT)) >= 0) {
                int n;
                if (verbose || (res > 0)) {
                    printf("%d ISO14443B-2 ASK CTx passive target(s) found%s\n", res,
                           (res == 0) ? ".\n" : ":");
                }
                for (n = 0; n < res; n++) {
                    print_nfc_target(&ant[n], verbose);
                    printf("\n");
                }
            }
        }

        if (mask & 0x80) {
            nm.nmt = NMT_JEWEL;
            nm.nbr = NBR_106;
            // List Jewel targets
            if ((res = nfc_initiator_list_passive_targets(pnd, nm, ant, MAX_TARGET_COUNT)) >= 0) {
                int n;
                if (verbose || (res > 0)) {
                    printf("%d ISO14443A-3 Jewel passive target(s) found%s\n", res,
                           (res == 0) ? ".\n" : ":");
                }
                for (n = 0; n < res; n++) {
                    print_nfc_target(&ant[n], verbose);
                    printf("\n");
                }
            }
        }

        if (mask & 0x100) {
            nm.nmt = NMT_BARCODE;
            nm.nbr = NBR_106;
            // List NFC Barcode targets
            if ((res = nfc_initiator_list_passive_targets(pnd, nm, ant, MAX_TARGET_COUNT)) >= 0) {
                int n;
                if (verbose || (res > 0)) {
                    printf("%d ISO14443A-2 NFC Barcode passive target(s) found%s\n", res,
                           (res == 0) ? ".\n" : ":");
                }
                for (n = 0; n < res; n++) {
                    print_nfc_target(&ant[n], verbose);
                    printf("\n");
                }
            }
        }
        nfc_close(pnd);
    }

    nfc_exit(context);
    return EXIT_SUCCESS;
}

/*
 * 执行命令!
 * */
JNIEXPORT jint JNICALL startExecute(JNIEnv *env, jobject instance, jstring command_) {
    //初始化JNIENV，这是必须的！
    JNIEnv *tmpJniEnv = getJniEnv();
    //jboolean类型，控制是否为拷贝
    jboolean isCopy = 1;
    const char *command = (*tmpJniEnv)->GetStringUTFChars(tmpJniEnv, command_, &isCopy);
    CMD *cmd = parse_command_line(command);
    if (cmd->len < 2) {
        print_usage(cmd->cmd[0]);
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
    int ret = main(cmd->len, (const char **) cmd->cmd);
    time_t end_time = time(NULL);
    printf("\nINFO: execute time(s): %lf\n", difftime(end_time, start_time));
    printf("\n------------End------------\n");
    fflush(stdout);
    fflush(stderr);
    isRunning = false;
    stop_label = false;
    free_command_line(cmd);
    return ret;
}

/*
 * 停止mfcuk
 * */
JNIEXPORT void JNICALL stopExecute(JNIEnv *env, jobject instance) {
    stop_label = true;
}

/*
 * 是否在执行当中
 * */
JNIEXPORT jboolean JNICALL isExecuting(JNIEnv *env, jobject instance) {
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
    jclass clazz = (*jniEnv)->FindClass(jniEnv, "cn/rrg/natives/NfcListTools");
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
