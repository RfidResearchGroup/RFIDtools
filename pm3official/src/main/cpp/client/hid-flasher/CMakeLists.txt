#添加动态库定义
add_library(flasher SHARED
        flash.c
        flasher.c
        proxusb.c
        com.c)

#添加头文件配置
target_include_directories(flasher PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/)