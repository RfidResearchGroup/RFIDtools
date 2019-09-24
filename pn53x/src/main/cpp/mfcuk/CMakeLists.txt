#添加动态库定义
add_library(mfcuk SHARED
        crapto1.c
        crypto1.c
        parity.c
        mfcuk.c
        mifare.c
        mfcuk_utils.c
        mfcuk_mifare.c
        mfcuk_finger.c)

#添加头文件配置
target_include_directories(mfcuk PRIVATE
        ../libnfc/include/
        ${CMAKE_CURRENT_SOURCE_DIR}/include/)

#添加动态库链接!
target_link_libraries(mfcuk nfc android log)