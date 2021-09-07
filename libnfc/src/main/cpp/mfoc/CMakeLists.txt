cmake_minimum_required (VERSION 2.6)

project (mfoc C)

#添加动态库定义
set(SOURCE_FILES
        crapto1.c
        crypto1.c
        nfc-utils.c
        mfoc.c
        mifare.c)

#添加头文件配置
include_directories(
        ../libnfc/include/
        ${CMAKE_CURRENT_SOURCE_DIR}/)

set(OUTPUT_NAME lib${PROJECT_NAME}.so)

add_executable(${OUTPUT_NAME} ${SOURCE_FILES})

target_link_libraries(${OUTPUT_NAME} nfc)