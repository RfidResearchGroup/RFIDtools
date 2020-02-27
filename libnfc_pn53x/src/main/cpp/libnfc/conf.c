#include "conf.h"

#ifdef HAVE_CONFIG_H

#  include "config.h"

#endif // HAVE_CONFIG_H

#ifdef CONFFILES

#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <string.h>

#include <nfc/nfc.h>
#include "nfc-internal.h"
#include "log.h"

#define LOG_CATEGORY "libnfc"
#define LOG_GROUP    NFC_LOG_GROUP_CONFIG

#ifndef LIBNFC_SYSCONFDIR
// If this define does not already exists, we build it using SYSCONFDIR
#ifndef SYSCONFDIR
#error "SYSCONFDIR is not defined but required."
#endif // SYSCONFDIR
#define CONF_FILE "libnfc.conf"
#define LIBNFC_SYSCONFDIR SYSCONFDIR"/NfcTools/pn53x/"
#define LIBNFC_CONF_FILE LIBNFC_SYSCONFDIR CONF_FILE
#endif // LIBNFC_SYSCONFDIR

void conf_load(nfc_context *data) {
    /*
     * TODO 待实现!
     * 加载配置文件从指定的目录下
     * */
    FILE *fp = NULL;
    if ((fp = fopen(LIBNFC_CONF_FILE, "r")) == NULL) {
        //在文件异常的情况下默认532
        strcpy(data->user_defined_devices[0].name, "PN532");
        LOGD("文件异常，匹配失败!");
    } else {
        //读出内容，判断定义的类型
        char name[12];
        fgets(name, 10, fp);
        //name[strlen(name) - 1] = '\0';
        //LOGD("内容: %s", name);
        if (strcmp(name, "PN532") == 0) {
            LOGD("匹配到模式: 532");
            strcpy(data->user_defined_devices[0].name, "PN532");
        } else if (strcmp(name, "ACR122") == 0) {
            LOGD("匹配到模式: 122");
            strcpy(data->user_defined_devices[0].name, "ACR122");
        } else {
            //当任何情况都不匹配时，默认清空文件，写入内容: "PN532"
            fclose(fp);
            //LOGD("写入文件!");
            fp = fopen(LIBNFC_CONF_FILE, "w");
            fprintf(fp, "%s", "PN532");
            fflush(fp);
            strcpy(data->user_defined_devices[0].name, "PN532");
        }
        //关闭文件
        fclose(fp);
    }
    strcpy(data->user_defined_devices[0].connstring, "dxl");
}

#endif // CONFFILES
