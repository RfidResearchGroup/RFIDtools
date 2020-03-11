#define PACKAGE_NAME "libnfc"
#define PACKAGE_VERSION "1.7.1"
//#define PACKAGE_STRING "libnfc 1.7.1"
//TODO 不开启配置文件加载,改为直接指定设备名字的方式!
// #define CONFFILES
//开启日志打印
#define LOG
//导入需要的库的头文件
#include <stdlib.h>
//配置开启了PN532_UART
#define DRIVER_PN532_UART_ENABLED
//配置开启ACR122 USB
#define DRIVER_ACR122_USB_ENABLED
// 配置开启PN53X的支持
#define DRIVER_PN53X_UART_ENABLED