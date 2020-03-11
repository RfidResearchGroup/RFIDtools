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
#include "proxgui.h"
#include "cmdmain.h"
#include "uart.h"
#include "ui.h"
#include "util.h"
#include "cmdparser.h"
#include "cmdhw.h"
#include "whereami.h"
#include "tools.h"

#if defined (_WIN32)
#define SERIAL_PORT_H	"com3"
#elif defined(__APPLE__)
#define SERIAL_PORT_H   "/dev/cu.usbmodem888"
#else
#define SERIAL_PORT_H    "/dev/ttyACM0"
#endif

static serial_port sp;
static UsbCommand txcmd;
static char comport[255];
byte_t rx[sizeof(UsbCommand)];
byte_t *prx = rx;
volatile static bool txcmd_pending = false;
struct receiver_arg {
    int run;
};
struct receiver_arg con;
pthread_t reader_thread;

void SendCommand(UsbCommand *c) {
#if 0
    //pthread_mutex_lock(&print_lock);
    PrintAndLogEx(NORMAL, "Sending %d bytes\n", sizeof(UsbCommand));
    //pthread_mutex_unlock(&print_lock);
#endif

    if (offline) {
        PrintAndLogEx(NORMAL, "Sending bytes to proxmark failed - offline");
        return;
    }
    /**
    The while-loop below causes hangups at times, when the pm3 unit is unresponsive
    or disconnected. The main console thread is alive, but comm thread just spins here.
    Not good.../holiman
    **/
    while (txcmd_pending);

    txcmd = *c;
    __atomic_test_and_set(&txcmd_pending, __ATOMIC_SEQ_CST);
}

#if defined(__linux__) || (__APPLE__)

static void showBanner(void) {
    PrintAndLog("\n\n");
    PrintAndLog("\e[34m██████╗ ███╗   ███╗ ████╗\e[0m     ...iceman fork\n");
    PrintAndLog("\e[34m██╔══██╗████╗ ████║   ══█║\e[0m\n");
    PrintAndLog("\e[34m██████╔╝██╔████╔██║ ████╔╝\e[0m\n");
    PrintAndLog("\e[34m██╔═══╝ ██║╚██╔╝██║   ══█║\e[0m    iceman@icesql.net\n");
    PrintAndLog("\e[34m██║     ██║ ╚═╝ ██║ ████╔╝\e[0m  https://github.com/iceman1001/proxmark3\n");
    PrintAndLog("\e[34m╚═╝     ╚═╝     ╚═╝ ╚═══╝\e[0m v3.1.0\n");
    PrintAndLog(
            "\nKeep iceman fork alive with a donation!           https://paypal.me/iceman1001/");
    PrintAndLog(
            "\nMONERO: 43mNJLpgBVaTvyZmX9ajcohpvVkaRy1kbZPm8tqAb7itZgfuYecgkRF36rXrKFUkwEGeZedPsASRxgv4HPBHvJwyJdyvQuP");
    PrintAndLog("\n\n\n");
    fflush(NULL);
}

#endif

bool hookUpPM3() {
    bool ret = false;
    sp = uart_open(comport);

    //pthread_mutex_lock(&print_lock);

    if (sp == INVALID_SERIAL_PORT) {
        PrintAndLogEx(WARNING, "Reconnect failed, retrying...  (reason: invalid serial port)\n");
        sp = NULL;
        ret = false;
        offline = 1;
    } else if (sp == CLAIMED_SERIAL_PORT) {
        PrintAndLogEx(WARNING,
                      "Reconnect failed, retrying... (reason: serial port is claimed by another process)\n");
        sp = NULL;
        ret = false;
        offline = 1;
    } else {
        PrintAndLogEx(SUCCESS, "Proxmark reconnected\n");
        ret = true;
        offline = 0;
    }
    //pthread_mutex_unlock(&print_lock);
    return ret;
}

// (iceman) if uart_receiver fails a command three times,  we conside the devices to be offline.
void
#ifdef __has_attribute
#if __has_attribute(force_align_arg_pointer)
__attribute__((force_align_arg_pointer))
#endif
#endif
*uart_receiver() {
    size_t rxlen;
    bool tmpsignal;
    int counter_to_offline = 0;
    LOGD("冰人客户端通信线程已经创建!");
    while (con.run) {
        rxlen = 0;
        if (uart_receive(sp, prx, sizeof(UsbCommand) - (prx - rx), &rxlen)) {
            if (rxlen == 0) continue;
            prx += rxlen;
            if ((prx - rx) < sizeof(UsbCommand)) continue;
            UsbCommandReceived((UsbCommand *) rx);
        }
        prx = rx;
        __atomic_load(&txcmd_pending, &tmpsignal, __ATOMIC_SEQ_CST);
        if (tmpsignal) {
            bool res = uart_send(sp, (byte_t *) &txcmd, sizeof(UsbCommand));
            if (!res) {
                counter_to_offline++;
                PrintAndLogEx(NORMAL, "sending bytes to proxmark failed");
            }
            __atomic_clear(&txcmd_pending, __ATOMIC_SEQ_CST);
            // set offline flag
            if (counter_to_offline == 3) {
                __atomic_test_and_set(&offline, __ATOMIC_SEQ_CST);
                break;
            }
        }
    }
    // when this reader thread dies, we close the serial port.
    uart_close(sp);
    //线程退出前我们需要销毁相关的jni绑定!
    deatchThread();
    pthread_exit(NULL);
    return NULL;
}

void
#ifdef __has_attribute
#if __has_attribute(force_align_arg_pointer)
__attribute__((force_align_arg_pointer))
#endif
#endif
main_loop(char *script_cmds_file, char *script_cmd, bool usb_present, bool stayInCommandLoop) {

    struct receiver_arg rarg;
    char *cmd = NULL;

    bool execCommand = (script_cmd != NULL);
    bool stdinOnPipe = !isatty(STDIN_FILENO);
    FILE *sf = NULL;
    char script_cmd_buf[256] = {
            0x00};  // iceman, needs lua script the same file_path_buffer as the rest

    PrintAndLogEx(DEBUG, "ISATTY/STDIN_FILENO == %s\n", (stdinOnPipe) ? "true" : "false");

    if (usb_present) {
        rarg.run = 1;
        pthread_create(&reader_thread, NULL, &uart_receiver, &rarg);
        // cache Version information now:
        if (execCommand || script_cmds_file || stdinOnPipe)
            CmdVersion("s");
        else
            CmdVersion("");
    }

    if (script_cmds_file) {

        sf = fopen(script_cmds_file, "r");
        if (sf) {
            PrintAndLogEx(SUCCESS, "executing commands from file: %s\n", script_cmds_file);
            if (stayInCommandLoop)
                PrintAndLogEx(SUCCESS, "staying in command loop after commands execution\n");
        }
    }

    //read_history(".history");

    // loops every time enter is pressed...
    while (1) {

        // this should hook up the PM3 again.
        if (offline) {

            // sets the global variable, SP and offline)
            usb_present = hookUpPM3();

            // usb and the reader_thread is NULL,  create a new reader thread.
            if (usb_present && !offline) {
                rarg.run = 1;
                pthread_create(&reader_thread, NULL, &uart_receiver, &rarg);
                // cache Version information now:
                if (execCommand || script_cmds_file || stdinOnPipe)
                    CmdVersion("s");
                else
                    CmdVersion("");
            }
        }

        // If there is a script file
        if (sf) {

            // clear array
            memset(script_cmd_buf, 0, sizeof(script_cmd_buf));

            // read script file
            if (!fgets(script_cmd_buf, sizeof(script_cmd_buf), sf)) {
                fclose(sf);
                sf = NULL;
            } else {

                // remove linebreaks
                strcleanrn(script_cmd_buf, sizeof(script_cmd_buf));

                if ((cmd = strmcopy(script_cmd_buf)) != NULL)
                    PrintAndLogEx(NORMAL, PROXPROMPT"%s\n", cmd);
            }
        } else {
            // If there is a script command
            if (execCommand) {

                if ((cmd = strmcopy(script_cmd)) != NULL)
                    PrintAndLogEx(NORMAL, PROXPROMPT"%s", cmd);

                execCommand = false;
            } else {
                // exit after exec command
                if (script_cmd && !stayInCommandLoop)
                    break;

                // if there is a pipe from stdin
                if (stdinOnPipe) {

                    // clear array
                    memset(script_cmd_buf, 0, sizeof(script_cmd_buf));
                    // get
                    if (!fgets(script_cmd_buf, sizeof(script_cmd_buf), stdin)) {
                        PrintAndLogEx(ERR, "STDIN unexpected end, exit...");
                        break;
                    }
                    // remove linebreaks
                    strcleanrn(script_cmd_buf, sizeof(script_cmd_buf));

                    if ((cmd = strmcopy(script_cmd_buf)) != NULL)
                        PrintAndLogEx(NORMAL, PROXPROMPT"%s", cmd);

                } else {
                    //cmd = readline(PROXPROMPT);
                    fflush(NULL);
                }
            }
        }

        // execute command
        if (cmd) {

            // rtrim
            size_t l = strlen(cmd);
            if (l > 0 && isspace(cmd[l - 1]))
                cmd[l - 1] = 0x00;

            if (cmd[0] != 0x00) {
                int ret = CommandReceived(cmd);
                //add_history(cmd);

                // exit or quit
                if (ret == 99)
                    break;
            }
            free(cmd);
            cmd = NULL;
        } else {
            PrintAndLogEx(NORMAL, "\n");
            if (!stayInCommandLoop)
                break;
        }
    } // end while

    if (sf)
        fclose(sf);

    //write_history(".history");

    free(cmd);
    cmd = NULL;

    if (usb_present) {
        rarg.run = 0;
        pthread_join(reader_thread, NULL);
    }
}

static void dumpAllHelp(int markdown) {
    PrintAndLogEx(NORMAL, "\n%sProxmark3 command dump%s\n\n", markdown ? "# " : "",
                  markdown ? "" : "\n======================");
    PrintAndLogEx(NORMAL,
                  "Some commands are available only if a Proxmark is actually connected.%s\n",
                  markdown ? "  " : "");
    PrintAndLogEx(NORMAL, "Check column \"offline\" for their availability.\n");
    PrintAndLogEx(NORMAL, "\n");
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
    PrintAndLogEx(NORMAL,
                  "syntax: %s <port> [-h|-help|-m|-f|-flush|-w|-wait|-c|-command|-l|-lua|-k] [cmd_script_file_name] [command][lua_script_name]\n",
                  command_line);
    PrintAndLogEx(NORMAL, "\texample:'%s "SERIAL_PORT_H"'\n\n", command_line);

    if (showFullHelp) {
        PrintAndLogEx(NORMAL, "help: <-h|-help> Dump all interactive command's help at once.\n");
        PrintAndLogEx(NORMAL, "\t%s  -h\n\n", command_line);
        PrintAndLogEx(NORMAL,
                      "markdown: <-m> Dump all interactive help at once in markdown syntax\n");
        PrintAndLogEx(NORMAL, "\t%s -m\n\n", command_line);
        PrintAndLogEx(NORMAL, "flush: <-f|-flush> Output will be flushed after every print.\n");
        PrintAndLogEx(NORMAL, "\t%s -f\n\n", command_line);
        PrintAndLogEx(NORMAL,
                      "wait: <-w|-wait> 20sec waiting the serial port to appear in the OS\n");
        PrintAndLogEx(NORMAL, "\t%s "SERIAL_PORT_H" -w\n\n", command_line);
        PrintAndLogEx(NORMAL, "script: A script file with one proxmark3 command per line.\n\n");
        PrintAndLogEx(NORMAL, "command: <-c|-command> Execute one proxmark3 command.\n");
        PrintAndLogEx(NORMAL, "\t%s "SERIAL_PORT_H" -c \"hf mf chk 1* ?\"\n", command_line);
        PrintAndLogEx(NORMAL, "\t%s "SERIAL_PORT_H" -command \"hf mf nested 1 *\"\n\n",
                      command_line);
        PrintAndLogEx(NORMAL, "lua: <-l|-lua> Execute lua script.\n");
        PrintAndLogEx(NORMAL, "\t%s "SERIAL_PORT_H" -l hf_read\n\n", command_line);
        PrintAndLogEx(NORMAL,
                      "stay: <-k> Stay in the command loop after script/command/lua execution.\n");
        PrintAndLogEx(NORMAL, "\t%s "SERIAL_PORT_H" -k scriptfile\n\n", command_line);
    }
}

int mainAPP(int argc, char *argv[]) {
    srand(time(0));

    bool usb_present = false;
    bool waitCOMPort = false;
    bool executeCommand = false;
    bool addLuaExec = false;
    bool stayInCommandLoop = false;
    char *script_cmds_file = NULL;
    char *script_cmd = NULL;

    /* initialize history */
    //using_history();

    if (argc < 2) {
        show_help(true, argv[0]);
        return 1;
    }

    // lets copy the comport string.
    memset(comport, 0, sizeof(comport));
    memcpy(comport, argv[1], strlen(argv[1]));

    for (int i = 1; i < argc; i++) {

        // helptext
        if (strcmp(argv[i], "-h") == 0 || strcmp(argv[i], "-help") == 0) {
            show_help(false, argv[0]);
            dumpAllHelp(0);
            return 0;
        }

        // dump markup
        if (strcmp(argv[i], "-m") == 0) {
            dumpAllHelp(1);
            return 0;
        }

        // flush output
        if (strcmp(argv[i], "-f") == 0 || strcmp(argv[i], "-flush") == 0) {
            g_flushAfterWrite = 1;
            PrintAndLogEx(INFO, "Output will be flushed after every print.\n");
        }

        // wait for comport
        if (strcmp(argv[i], "-w") == 0 || strcmp(argv[i], "-wait") == 0) {
            waitCOMPort = true;
        }

        // execute pm3 command
        if (strcmp(argv[i], "-c") == 0 || strcmp(argv[i], "-command") == 0) {
            executeCommand = true;
        }

        // execute lua script
        if (strcmp(argv[i], "-l") == 0 || strcmp(argv[i], "-lua") == 0) {
            executeCommand = true;
            addLuaExec = true;
        }

        // stay in command loop
        if (strcmp(argv[i], "-k") == 0) {
            stayInCommandLoop = true;
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

                PrintAndLogEx(SUCCESS, "execute command from commandline: %s\n", script_cmd);
                if (stayInCommandLoop)
                    PrintAndLogEx(SUCCESS, "staying in command loop after command execution\n");
            }
        } else {
            script_cmds_file = argv[argc - 1];
        }
    }

    // check command
    if (executeCommand && (!script_cmd || strlen(script_cmd) == 0)) {
        PrintAndLogEx(WARNING, "ERROR: execute command: command not found.\n");
        return 2;
    }

#if defined(__linux__) || (__APPLE__)
// ascii art doesn't work well on mingw :( 

    bool stdinOnPipe = !isatty(STDIN_FILENO);
    if (!executeCommand && !script_cmds_file && !stdinOnPipe)
        showBanner();
#endif

    // set global variables
    set_my_executable_path();

    // open uart
    if (!waitCOMPort) {
        sp = uart_open(argv[1]);
    } else {
        PrintAndLogEx(SUCCESS, "waiting for Proxmark to appear on %s ", argv[1]);
        fflush(stdout);
        int openCount = 0;
        do {
            sp = uart_open(argv[1]);
            msleep(500);
            printf(".");
            fflush(stdout);
        } while (++openCount < 30 && (sp == INVALID_SERIAL_PORT || sp == CLAIMED_SERIAL_PORT));
        PrintAndLogEx(NORMAL, "\n");
    }

    // check result of uart opening
    if (sp == INVALID_SERIAL_PORT) {
        PrintAndLogEx(WARNING, "ERROR: invalid serial port");
        usb_present = false;
        offline = 1;
    } else if (sp == CLAIMED_SERIAL_PORT) {
        PrintAndLogEx(WARNING, "ERROR: serial port is claimed by another process");
        usb_present = false;
        offline = 1;
    } else {
        usb_present = true;
        offline = 0;
    }

    fflush(NULL);
    // create a mutex to avoid interlacing print commands from our different threads
    pthread_mutex_init(&print_lock, NULL);

#ifdef HAVE_GUI

#  ifdef _WIN32
    InitGraphics(argc, argv, script_cmds_file, script_cmd, usb_present, stayInCommandLoop);
    MainGraphics();
#  else
    // for *nix distro's,  check enviroment variable to verify a display
    char* display = getenv("DISPLAY");
    if (display && strlen(display) > 1) {
        InitGraphics(argc, argv, script_cmds_file, script_cmd, usb_present, stayInCommandLoop);
        MainGraphics();
    } else {
        main_loop(script_cmds_file, script_cmd, usb_present, stayInCommandLoop);
    }
#  endif

#else
    main_loop(script_cmds_file, script_cmd, usb_present, stayInCommandLoop);
#endif

    // clean up mutex
    pthread_mutex_destroy(&print_lock);

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
    int ret = mainAPP(cmd->len, cmd->cmd);
    free_command_line(cmd);
    return ret;
}

jboolean stopPm3(JNIEnv *env, jobject instance, jstring args_) {
    //关闭线程!
    con.run = false;
    return true;
}

bool OpenProxmark() {
    sp = uart_open("dxl");
    LOGD("current client is iceman!");
    fflush(NULL);
    // create a mutex to avoid interlacing print commands from our different threads
    pthread_mutex_init(&print_lock, NULL);
    if (!con.run) {
        con.run = true;
        offline = false;
        pthread_create(&reader_thread, NULL, &uart_receiver, NULL);
        // cache Version information now:
        //TODO 由于我们在连接时就已经启动PM3的轮询程序，因此第一次启动的打印实际上没用!
        //CmdVersion("s");
    }
    return true;
}

/*
 * 发送一条命令等待执行!
 * */
jint sendCMD(JNIEnv *env, jobject instance, jstring cmd_) {
    //可能PM3不在运行，此时我们需要开启
    if (!con.run) {
        PrintAndLog("\nProxmark3 is Stoped,we trying auto open...\n");
        if (OpenProxmark()) {
            PrintAndLog("\n\topen successful\n");
        } else {
            PrintAndLog("\n\topen failed\n");
        }
    }
    //TODO 每次执行之前都要进行键盘缓冲区清空，避免出现误判!
    while (getc(stdin) != EOF);
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
    return (jboolean) con.run;
}

/*
 * 进行设备链接验证!
 * */
jboolean testPm3(JNIEnv *env, jobject instance) {
    return (jboolean) OpenProxmark();
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
    jclass clazz = (*jniEnv)->FindClass(jniEnv, "cn/rrg/natives/Proxmark3IcemanTools");
    if (clazz == NULL) {
        return -1;
    }
    //得到PM3测试类的定义类
    jclass clz_test = (*jniEnv)->FindClass(jniEnv, "cn/rrg/devices/Proxmark3Iceman");
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