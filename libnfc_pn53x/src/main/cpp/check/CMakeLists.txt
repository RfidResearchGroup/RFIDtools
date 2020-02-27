#添加动态库定义
add_library(pn53x_prng_check SHARED
        crapto1.c
        crypto1.c
        check.c)

#添加头文件配置
target_include_directories(pn53x_prng_check PRIVATE
        ../libnfc/include/
        ${CMAKE_CURRENT_SOURCE_DIR}/)

#添加动态库链接!
target_link_libraries(pn53x_prng_check nfc android log)