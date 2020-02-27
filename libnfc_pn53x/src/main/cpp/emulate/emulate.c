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
 * @file nfc-emulate-uid.c
 * @brief Emulates a tag which which have a "really" custom UID
 *
 * NFC devices are able to emulate passive tags but manufacturers restrict the
 * customization of UID. With PN53x, UID is only 4-byte long and the first
 * byte of emulated UID is hard-wired to 0x08 which is the standard way to say
 * this is a random UID.  This example shows how to emulate a fully customized
 * UID by "manually" replying to anti-collision process sent by the initiator.
 */

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif // HAVE_CONFIG_H

#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <stdint.h>
#include <string.h>
#include <signal.h>

#include <nfc/nfc.h>
#include <nfc/nfc-utils.h>

#define MAX_FRAME_LEN 264

static uint8_t abtRecv[MAX_FRAME_LEN];
static int szRecvBits;
static nfc_device *pnd;
static nfc_context *context;

// ISO14443A Anti-Collision response
uint8_t abtAtqa[2] = {0x04, 0x00};
uint8_t abtUidBcc[5] = {0xDE, 0xAD, 0xBE, 0xEF, 0x22};
uint8_t abtSak[9] = {0x08, 0xb6, 0xdd};


static void
print_usage(char *argv[]) {
    printf("Usage: %s [OPTIONS] [UID]\n", argv[0]);
    printf("Options:\n");
    printf("\t-h\tHelp. Print this message.\n");
    printf("\t-q\tQuiet mode. Silent output: received and sent frames will not be shown (improves timing).\n");
    printf("\n");
    printf("\t[UID]\tUID to emulate, specified as 8 HEX digits (default is DEADBEEF).\n");
}

static volatile bool isRunning = false;
static volatile bool stop_label = false;

int
main(int argc, char *argv[]) {
    uint8_t *pbtTx = NULL;
    size_t szTxBits;
    bool quiet_output = false;

    int arg, i;

    // Get commandline options
    for (arg = 1; arg < argc; arg++) {
        if (0 == strcmp(argv[arg], "-h")) {
            print_usage(argv);
            return EXIT_FAILURE;
        } else if (0 == strcmp(argv[arg], "-q")) {
            printf("Quiet mode.\n");
            quiet_output = true;
        } else if ((arg == argc - 1) &&
                   (strlen(argv[arg]) == 8)) {         // See if UID was specified as HEX string
            uint8_t abtTmp[3] = {0x00, 0x00, 0x00};
            printf("[+] Using UID: %s\n", argv[arg]);
            abtUidBcc[4] = 0x00;
            for (i = 0; i < 4; ++i) {
                memcpy(abtTmp, argv[arg] + i * 2, 2);
                abtUidBcc[i] = (uint8_t) strtol((char *) abtTmp, NULL, 16);
                abtUidBcc[4] ^= abtUidBcc[i];
            }
        } else {
            ERR("%s is not supported option.", argv[arg]);
            print_usage(argv);
            return EXIT_FAILURE;
        }
    }

    nfc_init(&context);
    if (context == NULL) {
        ERR("Unable to init libnfc (malloc)");
        return EXIT_FAILURE;
    }

    // Try to open the NFC devices
    pnd = nfc_open(context, NULL);

    if (pnd == NULL) {
        ERR("Unable to open NFC devices");
        nfc_exit(context);
        return EXIT_FAILURE;
    }

    printf("\n");
    printf("NFC devices: %s opened\n", nfc_device_get_name(pnd));
    printf("[+] Try to break out the auto-emulation, this requires a second NFC devices!\n");
    printf("[+] To do this, please write any command after the anti-collision\n");
    printf("[+] For example, write a RATS command or use the \"devices-anticol\" or \"devices-list\" tool.\n");

    // Note: We have to build a "fake" nfc_target in order to do exactly the same that was done before the new nfc_target_init() was introduced.
    nfc_target nt = {
            .nm = {
                    .nmt = NMT_ISO14443A,
                    .nbr = NBR_UNDEFINED,
            },
            .nti = {
                    .nai = {
                            .abtAtqa = {0x04, 0x00},
                            .abtUid = {0x08, 0xad, 0xbe, 0xef},
                            .btSak = 0x020,
                            .szUidLen = 4,
                            .szAtsLen = 0,
                    },
            },
    };
    if ((szRecvBits = nfc_target_init(pnd, &nt, abtRecv, sizeof(abtRecv), 5000)) < 0) {
        nfc_perror(pnd, "nfc_target_init");
        ERR("Could not come out of auto-emulation, no command was received");
        nfc_close(pnd);
        nfc_exit(context);
        return EXIT_FAILURE;
    }
    printf("[+] Received initiator command: ");
    print_hex_bits(abtRecv, (size_t) szRecvBits);
    printf("[+] Configuring communication\n");
    if ((nfc_device_set_property_bool(pnd, NP_HANDLE_CRC, false) < 0) ||
        (nfc_device_set_property_bool(pnd, NP_HANDLE_PARITY, true) < 0)) {
        nfc_perror(pnd, "nfc_device_set_property_bool");
        nfc_close(pnd);
        nfc_exit(context);
        return EXIT_FAILURE;
    }
    printf("[+] Done, the emulated tag is initialized with UID: %02X%02X%02X%02X\n\n", abtUidBcc[0],
           abtUidBcc[1],
           abtUidBcc[2], abtUidBcc[3]);

    while (!stop_label) {
        // Test if we received a frame
        if ((szRecvBits = nfc_target_receive_bits(pnd, abtRecv, sizeof(abtRecv), 0)) > 0) {
            // Prepare the command to write back for the anti-collision request
            switch (szRecvBits) {
                case 7:                  // Request or Wakeup
                    pbtTx = abtAtqa;
                    szTxBits = 16;
                    // New anti-collsion session started
                    if (!quiet_output)
                        printf("\n");
                    break;

                case 16:                 // Select All
                    pbtTx = abtUidBcc;
                    szTxBits = 40;
                    break;

                case 72:                 // Select Tag
                    pbtTx = abtSak;
                    szTxBits = 24;
                    break;

                default:                 // unknown length?
                    szTxBits = 0;
                    break;
            }

            if (!quiet_output) {
                printf("R: ");
                print_hex_bits(abtRecv, (size_t) szRecvBits);
            }
            // Test if we know how to respond
            if (szTxBits) {
                // Send and print the command to the screen
                if (nfc_target_send_bits(pnd, pbtTx, szTxBits, NULL) < 0) {
                    nfc_perror(pnd, "nfc_target_send_bits");
                    nfc_close(pnd);
                    nfc_exit(context);
                    return EXIT_FAILURE;
                }
                if (!quiet_output) {
                    printf("T: ");
                    print_hex_bits(pbtTx, szTxBits);
                }
            }
        }
    }
    if (pnd != NULL) {
        printf("\nAborting current command...\n");
        nfc_abort_command(pnd);
        nfc_close(pnd);
    }
    nfc_exit(context);
    return EXIT_SUCCESS;
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
        print_usage(cmd->cmd);
        return EXIT_FAILURE;
    }
    //释放内存
    (*tmpJniEnv)->ReleaseStringUTFChars(tmpJniEnv, command_, command);
    recovery_getopt_opt();
    isRunning = true;
    stop_label = false;
    int ret = main(cmd->len, cmd->cmd);
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
    /*
     * TODO 重定向输出!
     * */
    //在此处重定向printf之类的输出
    /*freopen("/sdcard/NfcTools/pn53x/pn53x_forward_o.txt", "w", stdout);
    freopen("/sdcard/NfcTools/pn53x/pn53x_forward_e.txt", "w", stderr);
    setbuf(stdout, NULL);
    setbuf(stderr, NULL);*/

    JNIEnv *jniEnv = NULL;
    //得到当前的jvm环境指针
    if ((*vm)->GetEnv(vm, (void **) &jniEnv, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    //初始化全局的jvm函数指针
    (*jniEnv)->GetJavaVM(jniEnv, &g_JavaVM);
    //得到native函数的定义类
    jclass clazz = (*jniEnv)->FindClass(jniEnv, "cn/rrg/natives/EmulateTools");
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