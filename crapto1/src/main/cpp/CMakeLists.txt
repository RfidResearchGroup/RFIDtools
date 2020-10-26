#需要的最cmake版本
cmake_minimum_required(VERSION 3.4.1)

#进行库(动态)的添加,在之后的参数中引入cpp/c文件
add_library(
        crapto1 SHARED
        crapto1.c
        crypto1.c
        main.c)

#进行头文件的添加!
target_include_directories(crapto1 PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/include)

#进行最终动态库的链接!
target_link_libraries(
        crapto1
        android
        log)