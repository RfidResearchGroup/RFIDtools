//-----------------------------------------------------------------------------
// Copyright (C) 2009 Michael Gernoth <michael at gernoth.net>
// Copyright (C) 2010 iZsh <izsh at fail0verflow.com>
//
// This code is licensed to you under the terms of the GNU GPL, version 2 or,
// at your option, any later version. See the LICENSE.txt file for the text of
// the license.
//-----------------------------------------------------------------------------
// Main binary
//-----------------------------------------------------------------------------

#include "proxmark3.h"

#include <stdlib.h>
#include <stdio.h>         // for Mingw readline
#include <limits.h>
#include <unistd.h>
#include <ctype.h>
/*#include <readline/readline.h>
#include <readline/history.h>*/
#include "usart_defs.h"
#include "util_posix.h"
#include "proxgui.h"
#include "cmdmain.h"
#include "ui.h"
#include "cmdhw.h"
#include "whereami.h"
#include "comms.h"
#include "fileutils.h"
#include "flash.h"
#include "tools.h"


#ifdef ANDROID
// Number of trailers == number of sectors
// Mifare Classic 1k 16x64b = 16
#define NR_TRAILERS_1k  (16)
// Mifare Classic Mini
#define NR_TRAILERS_MINI (5)
// Mifare Classic 4k 32x64b + 8*256b = 40
#define NR_TRAILERS_4k  (40)
// Mifare Classic 2k 32x64b
#define NR_TRAILERS_2k  (32)

// Number of blocks
// Mifare Classic 1k
#define NR_BLOCKS_1k 0x3f
// Mifare Classic Mini
#define NR_BLOCKS_MINI 0x13
// Mifare Classic 4k
#define NR_BLOCKS_4k 0xff
// Mifare Classic 2k
#define NR_BLOCKS_2k 0x7f

#define MAX_FRAME_LEN 264

//声明静态的相关变量
static bool bWriteZero = false;
static bool magic2 = false;
static bool unlocked = false;
static bool verbose = false;

//储存标签扇区和块总数的数据结构
static struct MifareClassicTAG {
    // 储存的是块总数
    uint8_t uiBlocks;
    // 储存的是扇区总数
    int32_t uiSectors;
    // 储存的是块数据
    uint8_t data[16];
};

// 储存标准14A的信息!
static iso14a_card_select_t card;
static mf_readblock_t readblock;
static uint64_t select_status;

static struct MifareClassicTAG mft;

//特殊卡解锁命令
uint8_t abtUnlock1[1] = {0x40};
uint8_t abtUnlock2[1] = {0x43};

//特殊卡上锁命令
uint8_t abtUplock1[] = {0xe1, 0x00, 0xe1, 0xee};
uint8_t abtUplock2[] = {0x85, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x08,
                        0x18, 0x47};

static void showBanner(void) {
    g_printAndLog = PRINTANDLOG_PRINT;

    PrintAndLogEx(NORMAL, "\n");
#if defined(__linux__) || (__APPLE__) || (_WIN32)
    PrintAndLogEx(NORMAL, "  " _BLUE_("██████╗ ███╗   ███╗ ████╗ "));
    PrintAndLogEx(NORMAL, "  " _BLUE_("██╔══██╗████╗ ████║   ══█║"));
    PrintAndLogEx(NORMAL, "  " _BLUE_("██████╔╝██╔████╔██║ ████╔╝"));
    PrintAndLogEx(NORMAL, "  " _BLUE_("██╔═══╝ ██║╚██╔╝██║   ══█║") "    iceman@icesql.net");
    PrintAndLogEx(NORMAL, "  " _BLUE_(
            "██║     ██║ ╚═╝ ██║ ████╔╝") "   https://github.com/rfidresearchgroup/proxmark3/");
    PrintAndLogEx(NORMAL, "  " _BLUE_("╚═╝     ╚═╝     ╚═╝ ╚═══╝ ") "pre-release v4.0");
#else
    PrintAndLogEx(NORMAL, "  ======. ===.   ===. ====.");
    PrintAndLogEx(NORMAL, "  ==...==.====. ====.   ..=.");
    PrintAndLogEx(NORMAL, "  ======..==.====.==. ====..");
    PrintAndLogEx(NORMAL, "  ==..... ==..==..==.   ..=.    iceman@icesql.net");
    PrintAndLogEx(NORMAL, "  ==.     ==. ... ==. ====..   https://github.com/rfidresearchgroup/proxmark3/");
    PrintAndLogEx(NORMAL, "  ...     ...     ... .....  pre-release v4.0");
#endif
//    PrintAndLogEx(NORMAL, "\nSupport iceman on patreon - https://www.patreon.com/iceman1001/");
//    PrintAndLogEx(NORMAL, "                 on paypal - https://www.paypal.me/iceman1001");
//    printf("\nMonero: 43mNJLpgBVaTvyZmX9ajcohpvVkaRy1kbZPm8tqAb7itZgfuYecgkRF36rXrKFUkwEGeZedPsASRxgv4HPBHvJwyJdyvQuP");
    PrintAndLogEx(NORMAL, "");
    fflush(stdout);
    g_printAndLog = PRINTANDLOG_PRINT | PRINTANDLOG_LOG;
}

static int check_comm(void) {
    // If communications thread goes down. Device disconnected then this should hook up PM3 again.
    if (IsCommunicationThreadDead() && session.pm3_present) {
        /*rl_set_prompt(PROXPROMPT_OFFLINE);
        rl_forced_update_display();*/
        CloseProxmark();
        PrintAndLogEx(INFO,
                      "Running in " _YELLOW_("OFFLINE") "mode. Use \"hw connect\" to reconnect\n");
    }
    return 0;
}

// first slot is always NULL, indicating absence of script when idx=0
FILE *cmdscriptfile[MAX_NESTED_CMDSCRIPT + 1] = {0};
uint8_t cmdscriptfile_idx = 0;
bool cmdscriptfile_stayafter = false;

int push_cmdscriptfile(char *path, bool stayafter) {
    if (cmdscriptfile_idx == MAX_NESTED_CMDSCRIPT) {
        PrintAndLogEx(ERR, "Too many nested scripts, skipping %s\n", path);
        return PM3_EMALLOC;
    }
    FILE *tmp = fopen(path, "r");
    if (tmp == NULL)
        return PM3_EFILE;
    if (cmdscriptfile_idx == 0)
        cmdscriptfile_stayafter = stayafter;
    cmdscriptfile[++cmdscriptfile_idx] = tmp;
    return PM3_SUCCESS;
}

static FILE *current_cmdscriptfile() {
    return cmdscriptfile[cmdscriptfile_idx];
}

static bool pop_cmdscriptfile() {
    fclose(cmdscriptfile[cmdscriptfile_idx]);
    cmdscriptfile[cmdscriptfile_idx--] = NULL;
    if (cmdscriptfile_idx == 0)
        return cmdscriptfile_stayafter;
    else
        return true;
}

static void dumpAllHelp(int markdown) {
    session.help_dump_mode = true;
    PrintAndLogEx(NORMAL, "\n%sProxmark3 command dump%s\n\n", markdown ? "# " : "",
                  markdown ? "" : "\n======================");
    PrintAndLogEx(NORMAL,
                  "Some commands are available only if a Proxmark3 is actually connected.%s\n",
                  markdown ? "  " : "");
    PrintAndLogEx(NORMAL, "Check column \"offline\" for their availability.\n");
    PrintAndLogEx(NORMAL, "\n");
    command_t *cmds = getTopLevelCommandTable();
    dumpCommandsRecursive(cmds, markdown);
    session.help_dump_mode = false;
}

static char *my_executable_path = NULL;
static char *my_executable_directory = NULL;

const char *get_my_executable_path(void) {
    return my_executable_path;
}

const char *get_my_executable_directory(void) {
    return my_executable_directory;
}

static void set_my_executable_path(void) {
    int path_length = wai_getExecutablePath(NULL, 0, NULL);
    if (path_length == -1)
        return;

    my_executable_path = (char *) calloc(path_length + 1, sizeof(uint8_t));
    int dirname_length = 0;
    if (wai_getExecutablePath(my_executable_path, path_length, &dirname_length) != -1) {
        my_executable_path[path_length] = '\0';
        my_executable_directory = (char *) calloc(dirname_length + 2, sizeof(uint8_t));
        strncpy(my_executable_directory, my_executable_path, dirname_length + 1);
        my_executable_directory[dirname_length + 1] = '\0';
    }
}

static const char *my_user_directory = NULL;

const char *get_my_user_directory(void) {
    return my_user_directory;
}

static void set_my_user_directory(void) {
    my_user_directory = getenv("HOME");
    // if not found, default to current directory
    if (my_user_directory == NULL)
        my_user_directory = ".";
}

static void show_help(bool showFullHelp, char *exec_name) {

    PrintAndLogEx(NORMAL, "\nsyntax: %s [-h|-t|-m]", exec_name);
    PrintAndLogEx(NORMAL,
                  "        %s [[-p] <port>] [-b] [-w] [-f] [-c <command>]|[-l <lua_script_file>]|[-s <cmd_script_file>] [-i] [-d <0|1|2>]",
                  exec_name);
    PrintAndLogEx(NORMAL,
                  "        %s [-p] <port> --flash [--unlock-bootloader] [--image <imagefile>]+ [-w] [-f] [-d <0|1|2>]",
                  exec_name);

    if (showFullHelp) {

        PrintAndLogEx(NORMAL, "\nCommon options:");
        PrintAndLogEx(NORMAL, "      -h/--help                           this help");
        PrintAndLogEx(NORMAL, "      -v/--version                        print client version");
        PrintAndLogEx(NORMAL,
                      "      -p/--port                           serial port to connect to");
        PrintAndLogEx(NORMAL,
                      "      -w/--wait                           20sec waiting the serial port to appear in the OS");
        PrintAndLogEx(NORMAL,
                      "      -f/--flush                          output will be flushed after every print");
        PrintAndLogEx(NORMAL, "      -d/--debug <0|1|2>                  set debugmode");
        PrintAndLogEx(NORMAL, "\nOptions in client mode:");
        PrintAndLogEx(NORMAL,
                      "      -t/--text                           dump all interactive command's help at once");
        PrintAndLogEx(NORMAL,
                      "      -m/--markdown                       dump all interactive help at once in markdown syntax");
        PrintAndLogEx(NORMAL,
                      "      -b/--baud                           serial port speed (only needed for physical UART, not for USB-CDC or BT)");
        PrintAndLogEx(NORMAL,
                      "      -c/--command <command>              execute one Proxmark3 command (or several separated by ';').");
        PrintAndLogEx(NORMAL, "      -l/--lua <lua script file>          execute lua script.");
        PrintAndLogEx(NORMAL,
                      "      -s/--script-file <cmd_script_file>  script file with one Proxmark3 command per line");
        PrintAndLogEx(NORMAL,
                      "      -i/--interactive                    enter interactive mode after executing the script or the command");
        PrintAndLogEx(NORMAL, "\nOptions in flasher mode:");
        PrintAndLogEx(NORMAL,
                      "      --flash                             flash Proxmark3, requires at least one --image");
        PrintAndLogEx(NORMAL,
                      "      --unlock-bootloader                 Enable flashing of bootloader area *DANGEROUS* (need --flash or --flash-info)");
        PrintAndLogEx(NORMAL,
                      "      --image <imagefile>                 image to flash. Can be specified several times.");
        PrintAndLogEx(NORMAL, "\nExamples:");
        PrintAndLogEx(NORMAL, "\n  to run Proxmark3 client:\n");
        PrintAndLogEx(NORMAL,
                      "      %s "SERIAL_PORT_EXAMPLE_H"                       -- runs the pm3 client",
                      exec_name);
        PrintAndLogEx(NORMAL,
                      "      %s "SERIAL_PORT_EXAMPLE_H" -f                    -- flush output everytime",
                      exec_name);
        PrintAndLogEx(NORMAL,
                      "      %s "SERIAL_PORT_EXAMPLE_H" -w                    -- wait for serial port",
                      exec_name);
        PrintAndLogEx(NORMAL,
                      "      %s                                    -- runs the pm3 client in OFFLINE mode",
                      exec_name);
        PrintAndLogEx(NORMAL, "\n  to execute different commands from terminal:\n");
        PrintAndLogEx(NORMAL,
                      "      %s "SERIAL_PORT_EXAMPLE_H" -c \"hf mf chk 1* ?\"   -- execute cmd and quit client",
                      exec_name);
        PrintAndLogEx(NORMAL,
                      "      %s "SERIAL_PORT_EXAMPLE_H" -l hf_read            -- execute lua script " _YELLOW_(
                              "`hf_read`")"and quit client", exec_name);
        PrintAndLogEx(NORMAL,
                      "      %s "SERIAL_PORT_EXAMPLE_H" -s mycmds.txt         -- execute each pm3 cmd in file and quit client",
                      exec_name);
        PrintAndLogEx(NORMAL, "\n  to flash fullimage and bootloader:\n");
        PrintAndLogEx(NORMAL,
                      "      %s "SERIAL_PORT_EXAMPLE_H" --flash --unlock-bootloader --image bootrom.elf --image fullimage.elf",
                      exec_name);
#ifdef __linux__
        PrintAndLogEx(NORMAL,
                      "\nNote (Linux):\nif the flasher gets stuck in 'Waiting for Proxmark3 to reappear on <DEVICE>',");
        PrintAndLogEx(NORMAL,
                      "you need to blacklist Proxmark3 for modem-manager - see documentation for more details:");
        PrintAndLogEx(NORMAL,
                      "* https://github.com/RfidResearchGroup/proxmark3/blob/master/doc/md/Installation_Instructions/ModemManager-Must-Be-Discarded.md");
        PrintAndLogEx(NORMAL,
                      "\nMore info on flashing procedure from the official Proxmark3 wiki:");
        PrintAndLogEx(NORMAL, "* https://github.com/Proxmark/proxmark3/wiki/Gentoo%%20Linux");
        PrintAndLogEx(NORMAL, "* https://github.com/Proxmark/proxmark3/wiki/Ubuntu%%20Linux");
        PrintAndLogEx(NORMAL, "* https://github.com/Proxmark/proxmark3/wiki/OSX\n");
#endif
    }
}

static int flash_pm3(char *serial_port_name, uint8_t num_files, char *filenames[FLASH_MAX_FILES],
                     bool can_write_bl) {

    int ret = PM3_EUNDEF;
    flash_file_t files[FLASH_MAX_FILES];
    memset(files, 0, sizeof(files));
    char *filepaths[FLASH_MAX_FILES] = {0};

    if (serial_port_name == NULL) {
        PrintAndLogEx(ERR, "You must specify a port.\n");
        return PM3_EINVARG;
    }

    for (int i = 0; i < num_files; ++i) {
        char *path;
        ret = searchFile(&path, FIRMWARES_SUBDIR, filenames[i], ".elf", true);
        if (ret != PM3_SUCCESS) {
            ret = searchFile(&path, BOOTROM_SUBDIR, filenames[i], ".elf", true);
        }
        if (ret != PM3_SUCCESS) {
            // Last try, let the error msg be displayed if not found
            ret = searchFile(&path, FULLIMAGE_SUBDIR, filenames[i], ".elf", false);
        }
        if (ret != PM3_SUCCESS) {
            goto finish2;
        }
        filepaths[i] = path;
    }

    PrintAndLogEx(SUCCESS, "About to use the following file%s:", num_files > 1 ? "s" : "");
    for (int i = 0; i < num_files; ++i) {
        PrintAndLogEx(SUCCESS, "    %s", filepaths[i]);
    }

    if (OpenProxmark(serial_port_name, true, 60, true, FLASHMODE_SPEED)) {
        PrintAndLogEx(NORMAL, _GREEN_("Found"));
    } else {
        PrintAndLogEx(ERR, "Could not find Proxmark3 on " _RED_("%s") ".\n", serial_port_name);
        ret = PM3_ETIMEOUT;
        goto finish2;
    }

    uint32_t max_allowed = 0;
    ret = flash_start_flashing(can_write_bl, serial_port_name, &max_allowed);
    if (ret != PM3_SUCCESS) {
        goto finish;
    }

    if (num_files == 0)
        goto finish;

    for (int i = 0; i < num_files; ++i) {
        ret = flash_load(&files[i], filepaths[i], can_write_bl, max_allowed * ONE_KB);
        if (ret != PM3_SUCCESS) {
            goto finish;
        }
        PrintAndLogEx(NORMAL, "");
    }

    PrintAndLogEx(SUCCESS, "\n" _BLUE_("Flashing..."));

    for (int i = 0; i < num_files; i++) {
        ret = flash_write(&files[i]);
        if (ret != PM3_SUCCESS) {
            goto finish;
        }
        flash_free(&files[i]);
        PrintAndLogEx(NORMAL, "\n");
    }

    finish:
    ret = flash_stop_flashing();
    CloseProxmark();
    finish2:
    for (int i = 0; i < num_files; ++i) {
        if (filepaths[i] != NULL)
            free(filepaths[i]);
    }
    if (ret == PM3_SUCCESS)
        PrintAndLogEx(SUCCESS, _BLUE_("All done."));
    else
        PrintAndLogEx(ERR, "Aborted on error.");
    PrintAndLogEx(NORMAL, "\nHave a nice day!");
    return ret;
}

// Check if windows AnsiColor Support is enabled in the registery
// [HKEY_CURRENT_USER\Console]
//     "VirtualTerminalLevel"=dword:00000001
// 2nd Key needs to be enabled...  This key takes the console out of legacy mode.
// [HKEY_CURRENT_USER\Console]
//     "ForceV2"=dword:00000001
static bool DetectWindowsAnsiSupport(void) {
    bool ret = false;
#if defined(_WIN32)
    HKEY hKey = NULL;
    bool virtualTerminalLevelSet = false;
    bool forceV2Set = false;

    if (RegOpenKeyA(HKEY_CURRENT_USER, "Console", &hKey) == ERROR_SUCCESS) {
        DWORD dwType = REG_SZ;
        BYTE KeyValue[sizeof(dwType)];
        DWORD len = sizeof(KeyValue);

        if (RegQueryValueEx(hKey, "VirtualTerminalLevel", NULL, &dwType, KeyValue, &len) != ERROR_FILE_NOT_FOUND) {
            uint8_t i;
            uint32_t Data = 0;
            for (i = 0; i < 4; i++)
                Data += KeyValue[i] << (8 * i);

            if (Data == 1) { // Reg key is set to 1, Ansi Color Enabled
                virtualTerminalLevelSet = true;
            }
        }
        RegCloseKey(hKey);
    }

    if (RegOpenKeyA(HKEY_CURRENT_USER, "Console", &hKey) == ERROR_SUCCESS) {
        DWORD dwType = REG_SZ;
        BYTE KeyValue[sizeof(dwType)];
        DWORD len = sizeof(KeyValue);

        if (RegQueryValueEx(hKey, "ForceV2", NULL, &dwType, KeyValue, &len) != ERROR_FILE_NOT_FOUND) {
            uint8_t i;
            uint32_t Data = 0;
            for (i = 0; i < 4; i++)
                Data += KeyValue[i] << (8 * i);

            if (Data == 1) { // Reg key is set to 1, Not using legacy Mode.
                forceV2Set = true;
            }
        }
        RegCloseKey(hKey);
    }
    // If both VirtualTerminalLevel and ForceV2 is set, AnsiColor should work
    ret = virtualTerminalLevelSet && forceV2Set;
#endif
    return ret;
}

//TODO 验证一下扇区的数量的准确性
static bool validateSector(int sector) {
    // Do not be too strict on upper bounds checking, since some cards
    // have more addressable memory than they report. For example,
    // MIFARE Plus 2k cards will appear as MIFARE Classic 1k cards when in
    // MIFARE Classic compatibility mode.
    // Note that issuing a command to an out-of-bounds block is safe - the
    // tag should report error causing IOException. This validation is a
    // helper to guard against obvious programming mistakes.
    if (sector < 0 || sector >= NR_TRAILERS_4k) {
        LOGD("sector out of bounds: %d\n", sector);
        return false;
    }
    return true;
}

//TODO 验证一下块的数量的准确性
static bool validateBlock(int block) {
    // Just looking for obvious out of bounds...
    if (block < 0 || block >= NR_BLOCKS_4k) {
        LOGD("block out of bounds: %d\n", block);
        return false;
    }
    return true;
}

//TODO 判断是否处于尾部块
static bool is_trailer_block(uint32_t uiBlock) {
    // 测试我们处于小区块还是大扇区
    if (uiBlock < 128)
        return ((uiBlock + 1) % 4 == 0);
    else
        return ((uiBlock + 1) % 16 == 0);
}

//TODO 扇区转块,返回当前扇区的第一个块
static uint8_t sectorToBlock(int sectorIndex) {

    if (!validateSector(sectorIndex)) {
        return -1;
    }

    if (sectorIndex < 32) {
        return (uint8_t) (sectorIndex * 4);
    } else {
        return (uint8_t) (32 * 4 + (sectorIndex - 32) * 16);
    }
}

//TODO 获得此块所在扇区的尾部块（秘钥与控制位所在块）
static uint32_t get_trailer_block(uint32_t uiFirstBlock) {
    // Test if we are in the small or big sectors
    uint32_t trailer_block = 0;
    if (uiFirstBlock < 128) {
        trailer_block = uiFirstBlock + (3 - (uiFirstBlock % 4));
    } else {
        trailer_block = uiFirstBlock + (15 - (uiFirstBlock % 16));
    }
    return trailer_block;
}

static jboolean authenticate(uint32_t uiBlock) {
    uint8_t keyBlock[6] = {
            readblock.key[0],
            readblock.key[1],
            readblock.key[2],
            readblock.key[3],
            readblock.key[4],
            readblock.key[5],
    };
    uint64_t tmp = (uint64_t) -1;
    uint64_t *key = &tmp;
    int ret;
    if ((ret = mfCheckKeys((uint8_t) uiBlock, readblock.keytype - 0x60, true, 1, keyBlock, key)) ==
        0) {
        if (*key != -1) {
            LOGD("验证成功");
            return true;
        }
    }
    LOGD("mfCheckKeys() return: %d", ret);
    return false;
}

static jboolean _unlock(JNIEnv *env, jobject thiz) {
    //JNI环境初始化
    getJniEnv();
    // TODO wait impl
    return unlocked;
}

static jboolean _uplock(JNIEnv *env, jobject thiz) {
    // TODO wait impl
    return false;
}

static jboolean _scanning(JNIEnv *env, jobject thiz) {
    JNIEnv *tmpJniEnv = getJniEnv();
    //执行前先检查异常
    if ((*tmpJniEnv)->ExceptionCheck(tmpJniEnv)) {
        (*tmpJniEnv)->ExceptionDescribe(tmpJniEnv);
        (*tmpJniEnv)->ExceptionClear(tmpJniEnv);
        return false;
    }

    // 先清空buffer!
    clearCommandBuffer();
    // 发送连接14a读卡器命令，连接卡片!
    SendCommandMIX(CMD_HF_ISO14443A_READER, ISO14A_CONNECT | ISO14A_NO_DISCONNECT, 0, 0, NULL,
                   0);
    PacketResponseNG resp;
    if (!WaitForResponseTimeout(CMD_ACK, &resp, 2500)) {
        if (verbose) PrintAndLogEx(WARNING, "iso14443a card select failed");
        // 关闭射频!
        DropField();
        return false;
    }

    // cache information to global.
    memcpy(&card, (iso14a_card_select_t *) resp.data.asBytes, sizeof(iso14a_card_select_t));

    /*
        0: couldn't read
        1: OK, with ATS
        2: OK, no ATS
        3: proprietary Anticollision
    */
    select_status = resp.oldarg[0];

    if (select_status == 0) {
        if (verbose) LOGD("iso14443a card select failed");
        DropField();
        return false;
    }

    if (select_status == 3) {
        LOGD("Card doesn't support standard iso14443-3 anticollision");
        LOGD("ATQA : %02x %02x", card.atqa[1], card.atqa[0]);
        DropField();
        return false;
    }

    LOGD(" UID : %s", sprint_hex(card.uid, card.uidlen));
    LOGD("ATQA : %02x %02x", card.atqa[1], card.atqa[0]);
    LOGD(" SAK : %02x [%"
                 PRIu64
                 "]", card.sak, resp.oldarg[0]);
    return true;
}

static jboolean _connect(JNIEnv *env, jobject thiz) {
    //JNI环境初始化
    getJniEnv();
    PacketResponseNG resp;
    bool isMifareClassic = true;
    magic2 = false;
    switch (card.sak) {
        case 0x00:
            isMifareClassic = false;

            // ******** is card of the MFU type (UL/ULC/NTAG/ etc etc)
            DropField();

            magic2 = true;

            uint32_t tagT = GetHF14AMfU_Type();
            if (tagT != UL_ERROR)
                ul_print_type(tagT, 0);
            else
                LOGD("TYPE: Possible AZTEK (iso14443a compliant)");

            // reconnect for further tests
            clearCommandBuffer();
            SendCommandMIX(CMD_HF_ISO14443A_READER, ISO14A_CONNECT | ISO14A_NO_DISCONNECT, 0, 0,
                           NULL, 0);
            WaitForResponse(CMD_ACK, &resp);

            memcpy(&card, (iso14a_card_select_t *) resp.data.asBytes,
                   sizeof(iso14a_card_select_t));

            select_status = resp.oldarg[0]; // 0: couldn't read, 1: OK, with ATS, 2: OK, no ATS

            if (select_status == 0) {
                DropField();
                return false;
            }
            break;
        case 0x01:
            LOGD("TYPE : NXP TNP3xxx Activision Game Appliance");
            break;
        case 0x04:
            LOGD("TYPE : NXP MIFARE (various !DESFire !DESFire EV1)");
            isMifareClassic = false;
            break;
        case 0x08:
            LOGD("TYPE : NXP MIFARE CLASSIC 1k | Plus 2k SL1 | 1k Ev1");
            break;
        case 0x09:
            LOGD("TYPE : NXP MIFARE Mini 0.3k");
            break;
        case 0x0A:
            LOGD("TYPE : FM11RF005SH (Shanghai Metro)");
            break;
        case 0x10:
            LOGD("TYPE : NXP MIFARE Plus 2k SL2");
            break;
        case 0x11:
            LOGD("TYPE : NXP MIFARE Plus 4k SL2");
            break;
        case 0x18:
            LOGD("TYPE : NXP MIFARE Classic 4k | Plus 4k SL1 | 4k Ev1");
            break;
        case 0x20:
            LOGD("TYPE : NXP MIFARE DESFire 4k | DESFire EV1 2k/4k/8k | Plus 2k/4k SL3 | JCOP 31/41");
            isMifareClassic = false;
            break;
        case 0x24:
            LOGD("TYPE : NXP MIFARE DESFire | DESFire EV1");
            isMifareClassic = false;
            break;
        case 0x28:
            LOGD("TYPE : JCOP31 or JCOP41 v2.3.1");
            break;
        case 0x38:
            LOGD("TYPE : Nokia 6212 or 6131 MIFARE CLASSIC 4K");
            break;
        case 0x88:
            LOGD("TYPE : Infineon MIFARE CLASSIC 1K");
            break;
        case 0x98:
            LOGD("TYPE : Gemplus MPCOS");
            break;
        default:;
    }
    switch (card.sak) {
        case 0x01:
        case 0x08:
        case 0x19:
        case 0x28:
        case 0x88:
            if (select_status ==
                1) { // if select_status == 1, is have the ats, ats is 2k tag feature.
                LOGD("Found Mifare Plus 2k tag");
                mft.uiSectors = NR_TRAILERS_2k;
                mft.uiBlocks = NR_BLOCKS_2k;
            } else {
                LOGD("发现了1K卡");
                mft.uiSectors = NR_TRAILERS_1k;
                mft.uiBlocks = NR_BLOCKS_1k;
            }
            break;
        case 0x09:
            LOGD("发现了迷你卡");
            mft.uiSectors = NR_TRAILERS_MINI;
            mft.uiBlocks = NR_BLOCKS_MINI;
            break;
        case 0x18:
            LOGD("发现了4K卡");
            mft.uiSectors = NR_TRAILERS_4k;
            mft.uiBlocks = NR_BLOCKS_4k;
            break;
        default:
            LOGD("不能判断卡的类型");
            return false;
    }
    return isMifareClassic;
}

static jbyteArray _getUid(JNIEnv *env, jobject thiz) {
    //JNI环境初始化
    JNIEnv *tmpJniEnv = getJniEnv();
    int uidLen = card.uidlen;
    jbyteArray byteArray = (*tmpJniEnv)->NewByteArray(tmpJniEnv, uidLen);
    (*tmpJniEnv)->SetByteArrayRegion(tmpJniEnv, byteArray, 0, uidLen,
                                     (const jbyte *) card.uid);
    return byteArray;
}

static jbyteArray _getAtqa(JNIEnv *env, jobject thiz) {
    //JNI环境初始化
    JNIEnv *tmpJniEnv = getJniEnv();
    int atqaLen = sizeof(card.atqa);
    jbyteArray byteArray = (*tmpJniEnv)->NewByteArray(tmpJniEnv, atqaLen);
    (*tmpJniEnv)->SetByteArrayRegion(tmpJniEnv, byteArray, 0, atqaLen,
                                     (const jbyte *) card.atqa);
    return byteArray;
}

static jbyteArray _getSak(JNIEnv *env, jobject thiz) {
    //JNI环境初始化
    JNIEnv *tmpJniEnv = getJniEnv();
    jbyte sakArray[] = {(const jbyte) card.sak};
    int sakArrayLen = sizeof(sakArray);
    jbyteArray byteArray = (*tmpJniEnv)->NewByteArray(tmpJniEnv, sakArrayLen);
    (*tmpJniEnv)->SetByteArrayRegion(tmpJniEnv, byteArray, 0, sakArrayLen, sakArray
    );
    return byteArray;
}

static jint _getSize(JNIEnv *env, jobject thiz) {

    //JNI环境初始化
    getJniEnv();

    switch (mft.uiSectors) {
        case NR_TRAILERS_1k:
            return 1024;
        case NR_TRAILERS_2k:
            return 2048;
        case NR_TRAILERS_4k:
            return 4096;
        case NR_TRAILERS_MINI:
            return 320;
        default:
            return -1;
    }
}

static jint _getSectorCount(JNIEnv *env, jobject thiz) {
    return mft.uiSectors;
}

static jint _getBlockCount(JNIEnv *env, jobject thiz) {
    return mft.uiBlocks + 1;
}

static jboolean _isUnlock(JNIEnv *env, jobject thiz) {
    return (jboolean) unlocked;
}

static jboolean _isEmulated(JNIEnv *env, jobject thiz) {
    return (jboolean) magic2;
}

static jboolean _disconnect(JNIEnv *env, jobject thiz) {
    // 直接关闭射频，达到断开连接的效果!
    DropField();
    return true;
}

static jboolean _authWithKeyA(JNIEnv *env, jobject obj, jint sector, jbyteArray keyA_) {
    // set 0x60 is keyA!
    readblock.keytype = 0x60;
    jbyte *keyA = (*env)->GetByteArrayElements(env, keyA_, NULL);
    // 传递参数，该使用哪个秘钥验证?
    memcpy(readblock.key, keyA, sizeof(readblock.key));
    //调用秘钥验证函数验证扇区秘钥!
    int block = sectorToBlock(sector);
    readblock.blockno = (uint8_t) block;
    jboolean result = (jboolean) authenticate((uint32_t) block);
    //释放内存，返回结果
    (*env)->ReleaseByteArrayElements(env, keyA_, keyA, 0);
    return result;
}

static jboolean _authWithKeyB(JNIEnv *env, jobject obj, jint sector, jbyteArray keyB_) {
    // set 0x61 is keyB!
    readblock.keytype = 0x61;
    jbyte *keyB = (*env)->GetByteArrayElements(env, keyB_, NULL);
    // 传递参数，该使用哪个秘钥验证?
    memcpy(readblock.key, keyB, sizeof(readblock.key));
    //调用秘钥验证函数验证扇区秘钥!
    int block = sectorToBlock(sector);
    readblock.blockno = (uint8_t) block;
    jboolean result = (jboolean) authenticate((uint32_t) block);
    //释放内存，返回结果
    (*env)->ReleaseByteArrayElements(env, keyB_, keyB, 0);
    return result;
}

static void _setWriteUID(JNIEnv *env, jobject obj, jboolean trueIsWrite) {
    bWriteZero = trueIsWrite;
}

//TODO 读卡实现
static bool read_card(int block) {
    if (magic2) {
        LOGD("dxl: This card does not require an unlocked write (R)\n");
    }
    readblock.blockno = (uint8_t) block;
    clearCommandBuffer();
    SendCommandNG(CMD_HF_MIFARE_READBL, (uint8_t *) &readblock, sizeof(mf_readblock_t));
    PacketResponseNG resp;
    if (WaitForResponseTimeout(CMD_HF_MIFARE_READBL, &resp, 1500)) {
        uint8_t *data = resp.data.asBytes;
        if (resp.status == PM3_SUCCESS) {
            LOGD("data: %s", sprint_hex(data, 16));
            // Is read success, we need to cache data to global.
            memcpy(mft.data, data, 16);
        } else {
            LOGD("failed reading block");
            return false;
        }
        if (mfIsSectorTrailer(readblock.blockno) && (data[6] || data[7] || data[8])) {
            //LOGD("Trailer decoded:");
            int bln = mfFirstBlockOfSector(mfSectorNum(readblock.blockno));
            int blinc = (mfNumBlocksPerSector(mfSectorNum(readblock.blockno)) > 4) ? 5 : 1;
            for (int i = 0; i < 4; i++) {
                /*LOGD("Access block %d%s: %s", bln,
                     ((blinc > 1) && (i < 3) ? "+" : ""),
                     mfGetAccessConditionsDesc(i, &data[6]));*/
                bln += blinc;
            }
            //LOGD("UserData: %s", sprint_hex_inrow(&data[9], 1));
        }
    } else {
        LOGD("Command execute timeout");
        return false;
    }
    return true;
}

static jbyteArray _readBlock(JNIEnv *env, jobject obj, jint block) {
    //JNI环境初始化
    JNIEnv *tmpJniEnv = getJniEnv();
    //调用基础读取函数直接读取!
    if (read_card(block)) {
        int dataLen = sizeof(mft.data);
        jbyteArray byteArray = (*tmpJniEnv)->NewByteArray(tmpJniEnv, dataLen);
        (*tmpJniEnv)->SetByteArrayRegion(tmpJniEnv, byteArray, 0, dataLen,
                                         (const jbyte *) mft.data
        );
        return byteArray;
    } else {
        return NULL;
    }
    return NULL;
}

static bool _write_card(int block, uint8_t *data) {
    if (!validateBlock(block)) {
        return false;
    }
    if (unlocked) {
        LOGD("Note: This card does not require an unlocked write (W) \n");
    }
    //是否允许写0扇区0块
    if (!bWriteZero) {
        LOGE("必须要开启写0块");
        return false;
    }
    if (block == 0) {
        //判断0扇区0块BCC是否有效
        if ((data[0] ^ data[1] ^ data[2] ^
             data[3] ^ data[4]) != 0x00 && !magic2) {
            LOGE("Expecting BCC=%02X", data[0] ^ data[1] ^ data[2] ^ data[3]);
            return false;
        }
    }
    // 直接写!
    readblock.blockno = (uint8_t) block;
    uint8_t buffer[26];
    memcpy(buffer, readblock.key, 6);
    memcpy(buffer + 10, data, 16);
    clearCommandBuffer();
    SendCommandOLD(CMD_HF_MIFARE_WRITEBL, readblock.blockno, readblock.keytype, 0, buffer,
                   sizeof(buffer));
    PacketResponseNG resp;
    if (WaitForResponseTimeout(CMD_ACK, &resp, 1500)) {
        uint8_t isOK = resp.oldarg[0] & 0xff;
        LOGD("isOk:%02x", isOK);
        return true;
    } else {
        LOGD("Command execute timeout");
    }
    return false;
}

static jboolean __write_card(JNIEnv *env, jobject obj, jint block, jbyteArray data_) {
    //JNI环境初始化
    JNIEnv *tmpJniEnv = getJniEnv();
    jbyte *data = (*tmpJniEnv)->GetByteArrayElements(tmpJniEnv, data_, NULL);
    bool result = _write_card(block, data);
    (*tmpJniEnv)->ReleaseByteArrayElements(tmpJniEnv, data_, data, 0);
    return (jboolean) result;
}

#endif

/*
 * 发送一条命令等待执行!
 * */
jint sendCMD(JNIEnv *env, jobject instance, jstring cmd_) {
    //may be pm3 not running.
    if (!conn.run) {
        if (OpenProxmark("dxl", false, 233, false, 115200) && TestProxmark() == PM3_SUCCESS) {
            PrintAndLogEx(NORMAL, "\nopen successful\n");
        } else {
            PrintAndLogEx(NORMAL, "\nopen failed\n");
        }
    }
    //无论如何，新的命令的输入了，就要换个行!
    PrintAndLogEx(NORMAL, "\n");
    char *cmd = (char *) ((*env)->GetStringUTFChars(env, cmd_, 0));
    // Many parts of the PM3 client will assume that they can read any write from pwd. So we set
    // pwd to whatever the PM3 "executable directory" is, to get consistent behaviour.
    /*int ret = chdir(get_my_executable_directory());
    if (ret == -1) {
        LOGW("Couldn't chdir(get_my_executable_directory()), errno=%s", strerror(errno));
    }
    char pwd[1024];
    memset((void *) &pwd, 0, sizeof(pwd));
    getcwd((char *) &pwd, sizeof(pwd));
    LOGI("pwd = %s", pwd);*/
    int ret = CommandReceived(cmd);
    if (ret == 99) {
        // exit / quit
        // TODO: implement this
        PrintAndLogEx(NORMAL, "Asked to exit, can't really do that yet...");
    }
    (*env)->ReleaseStringUTFChars(env, cmd_, cmd);
    return ret;
}

/*
 * 是否在执行命令
 * */
jboolean isExecuting(JNIEnv *env, jobject instance) {
    return (jboolean) ((jboolean) conn.run);
}

/*
 * 进行设备链接验证!
 * */
jboolean testPm3(JNIEnv *env, jobject instance) {
    bool ret1 = OpenProxmark("dxl", false, 233, false, 115200);
    bool ret2 = TestProxmark() == PM3_SUCCESS;
    return (jboolean) (ret1 && ret2);
}

void stopPm3(JNIEnv *env, jobject instance) {
    CloseProxmark();
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
    jclass clazz = (*jniEnv)->FindClass(jniEnv, "cn/rrg/natives/Proxmark3RRGRdv4Tools");
    if (clazz == NULL) {
        return -1;
    }
//得到PM3测试类的定义类
    jclass clz_test = (*jniEnv)->FindClass(jniEnv, "cn/rrg/devices/Proxmark3RRGRdv4");
    jclass clazz_mifare = (*jniEnv)->FindClass(jniEnv, "cn/rrg/natives/PM3Rdv4RRGMifare");
//构建和初始化函数结构体,分别是java层的函数名称，签名，对应的函数指针
    JNINativeMethod methods[] = {
            {"startExecute", "(Ljava/lang/String;)I", (void *) sendCMD},
            {"stopExecute",  "()V",                   (void *) stopPm3},
            {"isExecuting",  "()Z",                   (void *) isExecuting}
    };
    JNINativeMethod methods1[] = {
            {"testPm3", "()Z", (void *) testPm3}
    };
//构建和初始化函数结构体,分别是java层的函数名称，签名，对应的函数指针
    JNINativeMethod methods2[] = {

            {"scanning",       "()Z",    (void *) _scanning},
            {"con",            "()Z",    (void *) _connect},
            {"unlock",         "()Z",    (void *) _unlock},
            {"uplock",         "()Z",    (void *) _uplock},

            {"getUid",         "()[B",   (void *) _getUid},
            {"getAtqa",        "()[B",   (void *) _getAtqa},
            {"getSak",         "()[B",   (void *) _getSak},
            {"getSize",        "()I",    (void *) _getSize},
            {"getSectorCount", "()I",    (void *) _getSectorCount},
            {"getBlockCount",  "()I",    (void *) _getBlockCount},

            {"isUnlock",       "()Z",    (void *) _isUnlock},
            {"isEmulated",     "()Z",    (void *) _isEmulated},


            {"readBlock",      "(I)[B",  (void *) _readBlock},
            {"writeBlock",     "(I[B)Z", (void *) __write_card},

            {"authWithKeyA",   "(I[B)Z", (void *) _authWithKeyA},
            {"authWithKeyB",   "(I[B)Z", (void *) _authWithKeyB},

            {"setWriteUID",    "(Z)V",   (void *) _setWriteUID},
            {"disconnect",     "()Z",    (void *) _disconnect}

    };
//注册应用类函数
    if ((*jniEnv)->RegisterNatives(jniEnv, clazz, methods, sizeof(methods) / sizeof(methods[0])) !=
        JNI_OK) {
        return -1;
    }
//注册测试类函数
    if ((*jniEnv)->RegisterNatives(jniEnv, clz_test, methods1,
                                   sizeof(methods1) / sizeof(methods1[0])) !=
        JNI_OK) {
        return -1;
    }
//注册函数
    if ((*jniEnv)->RegisterNatives(jniEnv, clazz_mifare, methods2,
                                   sizeof(methods2) / sizeof(methods2[0])) !=
        JNI_OK) {
        return -1;
    }
    (*jniEnv)->DeleteLocalRef(jniEnv, clazz);
    (*jniEnv)->DeleteLocalRef(jniEnv, clz_test);
    return JNI_VERSION_1_4;
}
