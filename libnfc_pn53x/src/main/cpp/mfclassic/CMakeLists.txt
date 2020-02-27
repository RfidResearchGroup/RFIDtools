#添加动态库定义
add_library(mfclassic SHARED
        mifare.c
        mfclassic.c)

#添加头文件配置
target_include_directories(mfclassic PRIVATE
        ../libnfc/include/
        ${CMAKE_CURRENT_SOURCE_DIR}/include)

#添加动态库链接!
target_link_libraries(mfclassic nfc android log)