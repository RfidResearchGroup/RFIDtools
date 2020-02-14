#添加动态库定义
add_library(mfoc SHARED
        crapto1.c
        crypto1.c
        parity.c
        mfoc.c
        mifare.c)

#添加头文件配置
target_include_directories(mfoc PRIVATE
        ../libnfc/include/
        ${CMAKE_CURRENT_SOURCE_DIR}/include/)

#添加动态库链接!
target_link_libraries(mfoc nfc android log)