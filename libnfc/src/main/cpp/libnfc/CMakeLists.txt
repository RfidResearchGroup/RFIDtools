#添加预编译宏定义参数，此次的作用是开启配置文件的引入!
add_definitions(-DHAVE_CONFIG_H)

#进行库(动态)的添加,在之后的参数中引入cpp/c文件
add_library(nfc SHARED
        conf.c tools.c
        log.c log-printf.c
        chips/pn53x.c
        buses/uart_posix.c
        buses/com_uart.c
        drivers/pn532_uart.c
        drivers/acr122_usb.c
        drivers/pn53x_usb.c
        nfc.c nfc-device.c
        nfc-emulation.c nfc-internal.c
        mirror-subr.c target-subr.c
        iso14443-subr.c nfc-utils.c)
#进行头文件的添加，注意，必须要添加上一级目录中init的头文件
target_include_directories(nfc PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/buses/
        ${CMAKE_CURRENT_SOURCE_DIR}/chips/
        ${CMAKE_CURRENT_SOURCE_DIR}/drivers/
        ${CMAKE_CURRENT_SOURCE_DIR}/include/
        ${CMAKE_CURRENT_SOURCE_DIR}/include/nfc/)

#进行最终动态库的链接!
target_link_libraries(nfc android log)