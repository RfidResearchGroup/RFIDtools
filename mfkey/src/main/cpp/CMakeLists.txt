#需要的最cmake版本
cmake_minimum_required(VERSION 3.4.1)

#添加动态库定义
add_library(mfkey32 SHARED
        mfkey32.c
        tools.c
        parity.c
        mfkey.c
        util_posix.c
        crypto1.c
        crapto1.c
        bucketsort.c
        )

#添加头文件配置
target_include_directories(mfkey32 PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/)

#添加动态库链接!
target_link_libraries(mfkey32 android log)

#添加动态库定义
add_library(mfkey64 SHARED
        mfkey64.c
        tools.c
        parity.c
        mfkey.c
        util_posix.c
        crypto1.c
        crapto1.c
        bucketsort.c
        )

#添加头文件配置
target_include_directories(mfkey64 PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/)

#添加动态库链接!
target_link_libraries(mfkey64 android log)

#添加动态库定义
add_library(mfkey32v2 SHARED
        mfkey32v2.c
        tools.c
        parity.c
        mfkey.c
        util_posix.c
        crypto1.c
        crapto1.c
        bucketsort.c
        )

#添加头文件配置
target_include_directories(mfkey32v2 PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/)

#添加动态库链接!
target_link_libraries(mfkey32v2 android log)