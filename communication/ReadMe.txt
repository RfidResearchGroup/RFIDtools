/**
 * @author DXL
 * Created by DXL on 2019/8/26.
 */

此模块用于解决底层数据与上层数据的映射传输需求。
工作原理：
    利用JNI在Jvm与Native内存中间分配两段大小为1MB的缓冲区，上层读到的数据放在recv缓冲区，
底层需要读取的数据间接从此缓冲区读取! 底层将需要发送的数据放到send缓冲区，上层从此缓冲区读取数据出来，送到到相关的通信接口！

使用步骤：
    1、引入module, 设置好需要使用的abi版本!

    2、拷贝module中include文件夹里面的头文件到需要使用映射的目标模块

    3、配置依赖者module的参数传递，在externalNativeBuild -> cmake区域：  
	String projectDirString = getRootDir().getPath().replaceAll("\\\\","/")
                arguments "-DPROJECTDIR=${projectDirString}"

    4、在依赖者module中的cmake配置文件里配置so路径，参考：
	#声明变量
	set(DEPENDENT ${PROJECTDIR}/libdependent/src/main/jniLibs/${ANDROID_ABI}/libdependent.so)

	#进行最终动态库的链接!
	target_link_libraries(soname ${DEPENDENT} android log)
