#添加动态库定义
add_library(nfclist SHARED nfclist.c)

#添加头文件配置
target_include_directories(nfclist PRIVATE
        ../libnfc/include/)

#添加动态库链接!
target_link_libraries(nfclist nfc android log)