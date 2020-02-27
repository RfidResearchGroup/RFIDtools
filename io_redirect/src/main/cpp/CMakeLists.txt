#需要的最cmake版本
cmake_minimum_required(VERSION 3.4.1)

#添加动态库定义
add_library(freopen SHARED util.c)

#添加头文件配置
target_include_directories(freopen PRIVATE)

#添加动态库链接!
target_link_libraries(freopen android)