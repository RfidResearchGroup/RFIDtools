#需要的最cmake版本
cmake_minimum_required(VERSION 3.4.1)

#设置全局宏定义，开启NDK配置定义!
add_definitions("-D__ANDROID_NDK__")

#添加预编译宏定义参数，此次的作用是开启配置文件的引入!
add_definitions(-DANDROID -D "getlocaledecpoint()='.'")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99 -O3 -fvisibility=hidden -w")

#缩短路径定义
set(PM3_ROOT ${CMAKE_CURRENT_SOURCE_DIR})

#添加动态库定义
add_library(pm3_flasher SHARED
        ${PM3_ROOT}/common/util_posix.c
        ${PM3_ROOT}/common/crc.c
        ${PM3_ROOT}/common/crc16.c
        ${PM3_ROOT}/common/crc32.c
        ${PM3_ROOT}/common/crc64.c
        ${PM3_ROOT}/common/parity.c
        ${PM3_ROOT}/common/commonutil.c
        ${PM3_ROOT}/common/bucketsort.c
        ${PM3_ROOT}/client/uart/uart_posix.c
        ${PM3_ROOT}/client/ui.c
        ${PM3_ROOT}/client/tools.c
        ${PM3_ROOT}/client/util.c
        ${PM3_ROOT}/client/comms.c
        ${PM3_ROOT}/client/proxmark3.c
        ${PM3_ROOT}/client/flash.c
        )

#添加头文件配置
target_include_directories(pm3_flasher PRIVATE
        ${PM3_ROOT}/
        ${PM3_ROOT}/include/
        ${PM3_ROOT}/common
        ${PM3_ROOT}/client
        ${PM3_ROOT}/client/uart)

#声明变量
set(COMMAPPING_FILE ${PROJECTDIR}/communication/src/main/jniLibs/${ANDROID_ABI}/libcommapping.so)

#添加动态库链接!
target_link_libraries(pm3_flasher ${COMMAPPING_FILE} android log z)