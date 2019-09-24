#添加动态库定义
add_library(hard SHARED
        craptev1/craptev1.c
        crapto1/crapto1.c
        crapto1/crypto1.c
        crypto1_bs.c
        crypto1_bs_crack.c
        libnfc_crypto1_crack.c)

#添加头文件配置
target_include_directories(hard PRIVATE
        ../libnfc/include/
        ${CMAKE_CURRENT_SOURCE_DIR}/
        ${CMAKE_CURRENT_SOURCE_DIR}/craptev1/
        ${CMAKE_CURRENT_SOURCE_DIR}/crapto1/)

#添加动态库链接!
target_link_libraries(hard nfc android log)