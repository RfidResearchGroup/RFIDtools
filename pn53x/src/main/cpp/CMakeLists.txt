#########################################################
#                                                       #
#                   Main build script                   #
#                      @author DXL                      #
#                   @date 2019/04/22                    #
#                                                       #
#########################################################

#需要的最cmake版本
cmake_minimum_required(VERSION 3.4.1)

#提供啰嗦(详细)的日志输出
set(CMAKE_VERBOSE_MAKEFILE on)

#设置当前的cmake路径，以当前的cmake文件所在的路径为相对路径
set(lib_src_DIR ${CMAKE_CURRENT_SOURCE_DIR})

#设置全局宏定义，开启NDK配置定义!
add_definitions("-D__ANDROID_NDK__")

#目录分级,进行子项目录添加，分别编译自身(注意编译顺序)!
add_subdirectory(${lib_src_DIR}/libnfc)
add_subdirectory(${lib_src_DIR}/emulate)
add_subdirectory(${lib_src_DIR}/hard)
add_subdirectory(${lib_src_DIR}/mfclassic)
add_subdirectory(${lib_src_DIR}/mfoc)
add_subdirectory(${lib_src_DIR}/mfcuk)
add_subdirectory(${lib_src_DIR}/nfclist)
add_subdirectory(${lib_src_DIR}/pn53x)