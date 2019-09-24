#define PACKAGE_NAME "libnfc"
#define PACKAGE_VERSION "1.7.1"
//#define PACKAGE_STRING "libnfc 1.7.1"
//TODO 开启配置文件加载
#define CONFFILES
//开启日志打印
#define LOG
//系统目录
#define SYSCONFDIR "/sdcard/"
//导入需要的库的头文件
#include <stdlib.h>
//配置开启了PN532_UART
#define DRIVER_PN532_UART_ENABLED
//配置开启ACR122 USB
#define DRIVER_ACR122_USB_ENABLED