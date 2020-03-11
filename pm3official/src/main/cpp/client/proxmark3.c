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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <unistd.h>
#include <errno.h>
#include <tools.h>

#include "util_posix.h"
#include "proxgui.h"
#include "cmdmain.h"
#include "ui.h"
#include "util.h"
#include "cmdparser.h"
#include "cmdhw.h"
#include "whereami.h"
#include "comms.h"
#include "tools.h"

#define SERIAL_PORT_H "port"

bool is_executing = false;

void
#ifdef __has_attribute
#if __has_attribute(force_align_arg_pointer)
__attribute__((force_align_arg_pointer))
#endif
#endif
main_loop(char *script_cmds_file, char *script_cmd, bool usb_present) {
    char *cmd = NULL;
    bool execCommand = (script_cmd != NULL);
    bool stdinOnPipe = !isatty(STDIN_FILENO);

    if (usb_present) {
        SetOffline(false);
        // cache Version information now:
        CmdVersion(NULL);
    } else {
        SetOffline(true);
    }


    // file with script
    FILE *script_file = NULL;
    // iceman, needs lua script the same file_path_buffer as the rest
    char script_cmd_buf[256] = {0};

    if (script_cmds_file) {
        script_file = fopen(script_cmds_file, "r");
        if (script_file) {
            printf("executing commands from file: %s\n", script_cmds_file);
        }
    }

    while (1) {
        // If there is a script file
        if (script_file) {
            memset(script_cmd_buf, 0, sizeof(script_cmd_buf));
            if (!fgets(script_cmd_buf, sizeof(script_cmd_buf), script_file)) {
                fclose(script_file);
                script_file = NULL;
            } else {
                strcleanrn(script_cmd_buf, sizeof(script_cmd_buf));

                if ((cmd = strmcopy(script_cmd_buf)) != NULL) {
                    printf(PROXPROMPT"%s\n", cmd);
                }
            }
        } else {
            // If there is a script command
            if (execCommand) {
                if ((cmd = strmcopy(script_cmd)) != NULL) {
                    printf(PROXPROMPT"%s\n", cmd);
                }

                execCommand = false;
            } else {
                // exit after exec command
                if (script_cmd)
                    break;

                // if there is a pipe from stdin
                if (stdinOnPipe) {
                    memset(script_cmd_buf, 0, sizeof(script_cmd_buf));
                    if (!fgets(script_cmd_buf, sizeof(script_cmd_buf), stdin)) {
                        printf("\nStdin end. Exit...\n");
                        break;
                    }
                    strcleanrn(script_cmd_buf, sizeof(script_cmd_buf));

                    if ((cmd = strmcopy(script_cmd_buf)) != NULL) {
                        printf(PROXPROMPT"%s\n", cmd);
                    }

                } else {
                    // read command from command prompt
                    //cmd = readline(PROXPROMPT);
                }
            }
        }

        // execute command
        if (cmd) {

            while (cmd[strlen(cmd) - 1] == ' ')
                cmd[strlen(cmd) - 1] = 0x00;

            if (cmd[0] != 0x00) {
                int ret = CommandReceived(cmd);
                if (ret == 99) {  // exit or quit
                    break;
                }
            }
            free(cmd);
            cmd = NULL;
        } else {
            printf("\n");
            break;
        }
    }

    if (script_file) {
        fclose(script_file);
        script_file = NULL;
    }
}

static void dumpAllHelp(int markdown) {
    printf("\n%sProxmark3 command dump%s\n\n", markdown ? "# " : "",
           markdown ? "" : "\n======================");
    printf("Some commands are available only if a Proxmark is actually connected.%s\n",
           markdown ? "  " : "");
    printf("Check column \"offline\" for their availability.\n");
    printf("\n");
    command_t *cmds = getTopLevelCommandTable();
    dumpCommandsRecursive(cmds, markdown);
}

static char *my_executable_path = NULL;
static char *my_executable_directory = NULL;

const char *get_my_executable_path(void) {
    return "/sdcard/NfcTools/pm3/";
}

const char *get_my_executable_directory(void) {
    return "/sdcard/NfcTools/pm3/";
}

static void set_my_executable_path(void) {
    int path_length = wai_getExecutablePath(NULL, 0, NULL);
    if (path_length != -1) {
        my_executable_path = (char *) malloc(path_length + 1);
        int dirname_length = 0;
        if (wai_getExecutablePath(my_executable_path, path_length, &dirname_length) != -1) {
            my_executable_path[path_length] = '\0';
            my_executable_directory = (char *) malloc(dirname_length + 2);
            strncpy(my_executable_directory, my_executable_path, dirname_length + 1);
            my_executable_directory[dirname_length + 1] = '\0';
        }
    }
}

static void show_help(bool showFullHelp, char *command_line) {
    printf("syntax: %s <port> [-h|-help|-m|-f|-flush|-w|-wait|-c|-command|-l|-lua] [cmd_script_file_name] [command][lua_script_name]\n",
           command_line);
    printf("\texample: %s "SERIAL_PORT_H"\n\n", command_line);

    if (showFullHelp) {
        printf("help: <-h|-help> Dump all interactive command's help at once.\n");
        printf("\t%s  -h\n\n", command_line);
        printf("markdown: <-m> Dump all interactive help at once in markdown syntax\n");
        printf("\t%s -m\n\n", command_line);
        printf("flush: <-f|-flush> Output will be flushed after every print.\n");
        printf("\t%s -f\n\n", command_line);
        printf("wait: <-w|-wait> 20sec waiting the serial port to appear in the OS\n");
        printf("\t%s "SERIAL_PORT_H" -w\n\n", command_line);
        printf("script: A script file with one proxmark3 command per line.\n\n");
        printf("command: <-c|-command> Execute one proxmark3 command.\n");
        printf("\t%s "SERIAL_PORT_H" -c \"hf mf chk 1* ?\"\n", command_line);
        printf("\t%s "SERIAL_PORT_H" -command \"hf mf nested 1 *\"\n\n", command_line);
        printf("lua: <-l|-lua> Execute lua script.\n");
        printf("\t%s "SERIAL_PORT_H" -l hf_read\n\n", command_line);
    }
}

int main(int argc, char *argv[]) {
    srand(time(0));

    bool usb_present = false;
    bool waitCOMPort = false;
    bool executeCommand = false;
    bool addLuaExec = false;
    char *script_cmds_file = NULL;
    char *script_cmd = NULL;

    if (argc < 2) {
        show_help(true, argv[0]);
        return 1;
    }

    for (int i = 1; i < argc; i++) {
        if (strcmp(argv[i], "-h") == 0 || strcmp(argv[i], "-help") == 0) {
            show_help(false, argv[0]);
            dumpAllHelp(0);
            return 0;
        }

        if (strcmp(argv[i], "-m") == 0) {
            dumpAllHelp(1);
            return 0;
        }

        if (strcmp(argv[i], "-f") == 0 || strcmp(argv[i], "-flush") == 0) {
            printf("Output will be flushed after every print.\n");
            SetFlushAfterWrite(true);
        }

        if (strcmp(argv[i], "-w") == 0 || strcmp(argv[i], "-wait") == 0) {
            waitCOMPort = true;
        }

        if (strcmp(argv[i], "-c") == 0 || strcmp(argv[i], "-command") == 0) {
            executeCommand = true;
        }

        if (strcmp(argv[i], "-l") == 0 || strcmp(argv[i], "-lua") == 0) {
            executeCommand = true;
            addLuaExec = true;
        }
    }

    // If the user passed the filename of the 'script' to execute, get it from last parameter
    if (argc > 2 && argv[argc - 1] && argv[argc - 1][0] != '-') {
        if (executeCommand) {
            script_cmd = argv[argc - 1];

            while (script_cmd[strlen(script_cmd) - 1] == ' ')
                script_cmd[strlen(script_cmd) - 1] = 0x00;

            if (strlen(script_cmd) == 0) {
                script_cmd = NULL;
            } else {
                if (addLuaExec) {
                    // add "script run " to command
                    char *ctmp = NULL;
                    int len = strlen(script_cmd) + 11 + 1;
                    if ((ctmp = (char *) malloc(len)) != NULL) {
                        memset(ctmp, 0, len);
                        strcpy(ctmp, "script run ");
                        strcpy(&ctmp[11], script_cmd);
                        script_cmd = ctmp;
                    }
                }
                printf("Execute command from commandline: %s\n", script_cmd);
            }
        } else {
            script_cmds_file = argv[argc - 1];
        }
    }

    // check command
    if (executeCommand && (!script_cmd || strlen(script_cmd) == 0)) {
        printf("ERROR: execute command: command not found.\n");
        return 2;
    }

    // set global variables
    set_my_executable_path();

    // try to open USB connection to Proxmark
    usb_present = OpenProxmark(argv[1], waitCOMPort, 20, false);

    main_loop(script_cmds_file, script_cmd, usb_present);

    // Clean up the port
    if (usb_present) {
        CloseProxmark();
    }

    exit(0);
}

int startPm3(JNIEnv *env, jobject instance, jstring args_) {
    //jboolean类型，控制是否为拷贝
    jboolean isCopy = 1;
    const char *command = (*env)->GetStringUTFChars(env, args_, &isCopy);
    //解析从JAVA端传入的命令
    CMD *cmd = parse_command_line(command);
    //重置参数解析函数的全局缓存!
    recovery_getopt_opt();
    //谨记释放内存，删除引用!
    (*env)->ReleaseStringUTFChars(env, args_, command);
    //执行main!
    int ret = main(cmd->len, cmd->cmd);
    free_command_line(cmd);
    return ret;
}

jboolean stopPm3(JNIEnv *env, jobject instance, jstring args_) {
    CloseProxmark();
}

/*
 * 发送一条命令等待执行!
 * */
jint sendCMD(JNIEnv *env, jobject instance, jstring cmd_) {
    //可能PM3不在运行，此时我们需要开启
    if (!isProxmarkRunning()) {
        PrintAndLog("\nProxmark3 is Stoped,we trying auto open...\n");
        if (OpenProxmark(NULL, false, 233, false)) {
            PrintAndLog("\n\topen successful\n");
        } else {
            PrintAndLog("\n\topen failed\n");
        }
    }
    //无论如何，新的命令的输入了，就要换个行!
    PrintAndLog("\n");
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
        PrintAndLog("Asked to exit, can't really do that yet...");
    }
    (*env)->ReleaseStringUTFChars(env, cmd_, cmd);
    return ret;
}


/*
 * 是否在执行命令
 * */
jboolean isExecuting(JNIEnv *env, jobject instance) {
    return (jboolean) ((jboolean) isProxmarkRunning() && (jboolean)is_executing);
}

/*
 * 进行设备链接验证!
 * */
jboolean testPm3(JNIEnv *env, jobject instance) {
    return (jboolean) OpenProxmark(NULL, false, 233, false);
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
    jclass clazz = (*jniEnv)->FindClass(jniEnv, "cn/rrg/natives/Proxmark3Tools");
    if (clazz == NULL) {
        return -1;
    }
    //得到PM3测试类的定义类
    jclass clz_test = (*jniEnv)->FindClass(jniEnv, "cn/rrg/devices/Proxmark3Official");
    //构建和初始化函数结构体,分别是java层的函数名称，签名，对应的函数指针
    JNINativeMethod methods[] = {
            {"startExecute", "(Ljava/lang/String;)I", (void *) sendCMD},
            {"stopExecute",  "()V",                   (void *) stopPm3},
            {"isExecuting",  "()Z",                   (void *) isExecuting}
    };
    JNINativeMethod methods1[] = {
            {"testPm3", "()Z", (void *) testPm3}
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
    //释放内存
    (*jniEnv)->DeleteLocalRef(jniEnv, clazz);
    (*jniEnv)->DeleteLocalRef(jniEnv, clz_test);
    //LOGD("pm3注册结束!");
    //最后一定要返回jni的版本。
    return JNI_VERSION_1_4;
}