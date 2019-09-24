#添加动态库定义
add_library(pn53x SHARED test.c)

#添加头文件配置
target_include_directories(pn53x PRIVATE
        ../init/include/
        ../libnfc/include/)

#添加动态库链接!
target_link_libraries(pn53x nfc android log)