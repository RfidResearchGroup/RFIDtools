#需要的最cmake版本
cmake_minimum_required(VERSION 3.4.1)

#设置全局宏定义，开启NDK配置定义!
add_definitions("-D__ANDROID_NDK__")

#添加预编译宏定义参数，此次的作用是开启配置文件的引入!
add_definitions(-DANDROID -D "getlocaledecpoint()='.'")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99 -O3 -fvisibility=hidden -w")

#缩短路径定义
set(PM3_ROOT ${CMAKE_CURRENT_SOURCE_DIR})

#添加动态库定义
add_library(pm3 SHARED
        ${PM3_ROOT}/uart/uart_posix.c
        ${PM3_ROOT}/common/crapto1/crapto1.c
        ${PM3_ROOT}/common/crapto1/crypto1.c
        ${PM3_ROOT}/common/crc.c
        ${PM3_ROOT}/common/crc16.c
        ${PM3_ROOT}/common/crc32.c
        ${PM3_ROOT}/common/crc64.c
        ${PM3_ROOT}/common/parity.c
        ${PM3_ROOT}/common/lfdemod.c
        ${PM3_ROOT}/common/iso14443crc.c
        ${PM3_ROOT}/common/iso15693tools.c
        ${PM3_ROOT}/common/protocols.c
        ${PM3_ROOT}/common/mbedtls/aes.c
        ${PM3_ROOT}/common/mbedtls/base64.c
        ${PM3_ROOT}/common/mbedtls/rsa.c
        ${PM3_ROOT}/common/mbedtls/rsa_internal.c
        ${PM3_ROOT}/common/mbedtls/arc4.c
        ${PM3_ROOT}/common/mbedtls/bignum.c
        ${PM3_ROOT}/common/mbedtls/asn1parse.c
        ${PM3_ROOT}/common/mbedtls/asn1write.c
        ${PM3_ROOT}/common/mbedtls/blowfish.c
        ${PM3_ROOT}/common/mbedtls/camellia.c
        ${PM3_ROOT}/common/mbedtls/certs.c
        ${PM3_ROOT}/common/mbedtls/des.c
        ${PM3_ROOT}/common/mbedtls/ecdsa.c
        ${PM3_ROOT}/common/mbedtls/ecp.c
        ${PM3_ROOT}/common/mbedtls/ecp_curves.c
        ${PM3_ROOT}/common/mbedtls/entropy.c
        ${PM3_ROOT}/common/mbedtls/entropy_poll.c
        ${PM3_ROOT}/common/mbedtls/error.c
        ${PM3_ROOT}/common/mbedtls/md.c
        ${PM3_ROOT}/common/mbedtls/md5.c
        ${PM3_ROOT}/common/mbedtls/md_wrap.c
        ${PM3_ROOT}/common/mbedtls/sha1.c
        ${PM3_ROOT}/common/mbedtls/sha256.c
        ${PM3_ROOT}/common/mbedtls/sha512.c
        ${PM3_ROOT}/common/mbedtls/timing.c
        ${PM3_ROOT}/common/mbedtls/cmac.c
        ${PM3_ROOT}/common/mbedtls/oid.c
        ${PM3_ROOT}/common/mbedtls/pem.c
        ${PM3_ROOT}/common/mbedtls/pk.c
        ${PM3_ROOT}/common/mbedtls/pk_wrap.c
        ${PM3_ROOT}/common/mbedtls/pkcs5.c
        ${PM3_ROOT}/common/mbedtls/pkcs12.c
        ${PM3_ROOT}/common/mbedtls/pkparse.c
        ${PM3_ROOT}/common/mbedtls/pkwrite.c
        ${PM3_ROOT}/common/mbedtls/x509.c
        ${PM3_ROOT}/common/mbedtls/x509_crl.c
        ${PM3_ROOT}/common/mbedtls/x509_crt.c
        ${PM3_ROOT}/common/mbedtls/ctr_drbg.c
        ${PM3_ROOT}/common/mbedtls/cipher_wrap.c
        ${PM3_ROOT}/common/mbedtls/cipher.c
        ${PM3_ROOT}/common/mbedtls/platform_util.c
        ${PM3_ROOT}/client/crypto/libpcrypto.c
        ${PM3_ROOT}/client/crypto/asn1utils.c
        ${PM3_ROOT}/client/crypto/asn1dump.c
        ${PM3_ROOT}/client/jansson/utf.c
        ${PM3_ROOT}/client/jansson/dump.c
        ${PM3_ROOT}/client/jansson/path.c
        ${PM3_ROOT}/client/jansson/load.c
        ${PM3_ROOT}/client/jansson/error.c
        ${PM3_ROOT}/client/jansson/value.c
        ${PM3_ROOT}/client/jansson/memory.c
        ${PM3_ROOT}/client/jansson/hashtable_seed.c
        ${PM3_ROOT}/client/jansson/strbuffer.c
        ${PM3_ROOT}/client/jansson/strconv.c
        ${PM3_ROOT}/client/jansson/hashtable.c
        ${PM3_ROOT}/client/loclass/cipherutils.c
        ${PM3_ROOT}/client/loclass/fileutils.c
        ${PM3_ROOT}/client/loclass/cipher.c
        ${PM3_ROOT}/client/loclass/ikeys.c
        ${PM3_ROOT}/client/loclass/elite_crack.c
        ${PM3_ROOT}/client/cliparser/cliparser.c
        ${PM3_ROOT}/client/cliparser/argtable3.c
        ${PM3_ROOT}/client/emv/emvcore.c
        ${PM3_ROOT}/client/emv/cmdemv.c
        ${PM3_ROOT}/client/emv/tlv.c
        ${PM3_ROOT}/client/emv/dol.c
        ${PM3_ROOT}/client/emv/emv_tags.c
        ${PM3_ROOT}/client/emv/emv_roca.c
        ${PM3_ROOT}/client/emv/dump.c
        ${PM3_ROOT}/client/emv/crypto_polarssl.c
        ${PM3_ROOT}/client/emv/crypto.c
        ${PM3_ROOT}/client/emv/emv_pk.c
        ${PM3_ROOT}/client/emv/emv_pki.c
        ${PM3_ROOT}/client/emv/emvjson.c
        ${PM3_ROOT}/client/emv/apduinfo.c
        ${PM3_ROOT}/client/emv/test/cryptotest.c
        ${PM3_ROOT}/client/emv/test/sda_test.c
        ${PM3_ROOT}/client/emv/test/dda_test.c
        ${PM3_ROOT}/client/emv/test/cda_test.c
        ${PM3_ROOT}/client/emv/test/crypto_test.c
        ${PM3_ROOT}/client/fido/additional_ca.c
        ${PM3_ROOT}/client/fido/cbortools.c
        ${PM3_ROOT}/client/fido/cose.c
        ${PM3_ROOT}/client/fido/fidocore.c
        ${PM3_ROOT}/client/tinycbor/cborencoder.c
        ${PM3_ROOT}/client/tinycbor/cborencoder_close_container_checked.c
        ${PM3_ROOT}/client/tinycbor/cborerrorstrings.c
        ${PM3_ROOT}/client/tinycbor/cborparser.c
        ${PM3_ROOT}/client/tinycbor/cborparser_dup_string.c
        ${PM3_ROOT}/client/tinycbor/cborpretty.c
        ${PM3_ROOT}/client/tinycbor/cborpretty_stdio.c
        ${PM3_ROOT}/client/tinycbor/cbortojson.c
        ${PM3_ROOT}/client/tinycbor/cborvalidation.c
        ${PM3_ROOT}/client/tinycbor/open_memstream.c
        ${PM3_ROOT}/client/mifarehost.c
        ${PM3_ROOT}/client/graph.c
        ${PM3_ROOT}/client/ui.c
        ${PM3_ROOT}/client/tools.c
        ${PM3_ROOT}/client/util.c
        ${PM3_ROOT}/client/util_posix.c
        ${PM3_ROOT}/client/comms.c
        ${PM3_ROOT}/client/cmddata.c
        ${PM3_ROOT}/client/cmdhf.c
        ${PM3_ROOT}/client/cmdhf14a.c
        ${PM3_ROOT}/client/cmdhf14b.c
        ${PM3_ROOT}/client/cmdhf15.c
        ${PM3_ROOT}/client/cmdhfepa.c
        ${PM3_ROOT}/client/cmdhflegic.c
        ${PM3_ROOT}/client/cmdhficlass.c
        ${PM3_ROOT}/client/cmdhflist.c
        ${PM3_ROOT}/client/cmdhfmf.c
        ${PM3_ROOT}/client/cmdhfmfu.c
        ${PM3_ROOT}/client/cmdhfmfp.c
        ${PM3_ROOT}/client/cmdhftopaz.c
        ${PM3_ROOT}/client/cmdhffido.c
        ${PM3_ROOT}/client/cmdhw.c
        ${PM3_ROOT}/client/cmdlf.c
        ${PM3_ROOT}/client/cmdlffdx.c
        ${PM3_ROOT}/client/cmdlfio.c
        ${PM3_ROOT}/client/cmdlfem4x.c
        ${PM3_ROOT}/client/cmdlfhid.c
        ${PM3_ROOT}/client/cmdlfhitag.c
        ${PM3_ROOT}/client/cmdlfjablotron.c
        ${PM3_ROOT}/client/cmdlfti.c
        ${PM3_ROOT}/client/cmdlfpac.c
        ${PM3_ROOT}/client/cmdlfnoralsy.c
        ${PM3_ROOT}/client/cmdlfnexwatch.c
        ${PM3_ROOT}/client/cmdlfpresco.c
        ${PM3_ROOT}/client/cmdlfindala.c
        ${PM3_ROOT}/client/cmdlfviking.c
        ${PM3_ROOT}/client/cmdlfsecurakey.c
        ${PM3_ROOT}/client/cmdlfpyramid.c
        ${PM3_ROOT}/client/cmdlfparadox.c
        ${PM3_ROOT}/client/cmdlfcotag.c
        ${PM3_ROOT}/client/cmdlfgproxii.c
        ${PM3_ROOT}/client/cmdlfawid.c
        ${PM3_ROOT}/client/cmdparser.c
        ${PM3_ROOT}/client/cmdscript.c
        ${PM3_ROOT}/client/mifare4.c
        ${PM3_ROOT}/client/cmdlfvisa2000.c
        ${PM3_ROOT}/client/whereami.c
        ${PM3_ROOT}/client/cmdmain.c
        ${PM3_ROOT}/client/mfkey.c
        ${PM3_ROOT}/client/scripting.c
        ${PM3_ROOT}/client/guidummy.c
        ${PM3_ROOT}/client/pm3_binlib.c
        ${PM3_ROOT}/client/proxmark3.c
        ${PM3_ROOT}/client/pm3_bitlib.c
        ${PM3_ROOT}/client/hidcardformats.c
        ${PM3_ROOT}/client/hidcardformatutils.c
        ${PM3_ROOT}/client/cmdlft55xx.c
        ${PM3_ROOT}/client/cmdlfpcf7931.c
        ${PM3_ROOT}/zlib/inflate.c
        ${PM3_ROOT}/zlib/inffast.c
        ${PM3_ROOT}/zlib/zutil.c
        ${PM3_ROOT}/zlib/inftrees.c
        ${PM3_ROOT}/zlib/adler32.c
        ${PM3_ROOT}/liblua/lapi.c
        ${PM3_ROOT}/liblua/ldo.c
        ${PM3_ROOT}/liblua/lgc.c
        ${PM3_ROOT}/liblua/ltm.c
        ${PM3_ROOT}/liblua/lvm.c
        ${PM3_ROOT}/liblua/lzio.c
        ${PM3_ROOT}/liblua/lcode.c
        ${PM3_ROOT}/liblua/llex.c
        ${PM3_ROOT}/liblua/liolib.c
        ${PM3_ROOT}/liblua/loslib.c
        ${PM3_ROOT}/liblua/lopcodes.c
        ${PM3_ROOT}/liblua/lmem.c
        ${PM3_ROOT}/liblua/lmathlib.c
        ${PM3_ROOT}/liblua/ldump.c
        ${PM3_ROOT}/liblua/ldblib.c
        ${PM3_ROOT}/liblua/lundump.c
        ${PM3_ROOT}/liblua/lcorolib.c
        ${PM3_ROOT}/liblua/lauxlib.c
        ${PM3_ROOT}/liblua/ltablib.c
        ${PM3_ROOT}/liblua/linit.c
        ${PM3_ROOT}/liblua/lstring.c
        ${PM3_ROOT}/liblua/lctype.c
        ${PM3_ROOT}/liblua/ltable.c
        ${PM3_ROOT}/liblua/ldebug.c
        ${PM3_ROOT}/liblua/lstate.c
        ${PM3_ROOT}/liblua/lstrlib.c
        ${PM3_ROOT}/liblua/lfunc.c
        ${PM3_ROOT}/liblua/lparser.c
        ${PM3_ROOT}/liblua/lobject.c
        ${PM3_ROOT}/liblua/loadlib.c
        ${PM3_ROOT}/liblua/lbaselib.c
        ${PM3_ROOT}/liblua/lbitlib.c
        #加入hardnested的支持!
        ${PM3_ROOT}/client/cmdhfmfhard.c
        ${PM3_ROOT}/client/hardnested/hardnested_bruteforce.c
        )

#添加头文件配置
target_include_directories(pm3 PRIVATE
        ${PM3_ROOT}/
        ${PM3_ROOT}/include/
        ${PM3_ROOT}/liblua
        ${PM3_ROOT}/zlib
        ${PM3_ROOT}/common
        ${PM3_ROOT}/uart
        ${PM3_ROOT}/client
        ${PM3_ROOT}/client/jansson
        ${PM3_ROOT}/client/fido
        ${PM3_ROOT}/client/tinycbor)

## CPU-specific code
## These are mostly for x86-based architectures, which is not useful for many Android devices.
add_library(hardnested_nosimd OBJECT
        ${PM3_ROOT}/client/hardnested/hardnested_bf_core.c
        ${PM3_ROOT}/client/hardnested/hardnested_bitarray_core.c)

target_include_directories(hardnested_nosimd PRIVATE
        ${PM3_ROOT}/common
        ${PM3_ROOT}/client
        ${PM3_ROOT}/client/hardnested)

set(X86_CPUS x86 x86_64 i686)

message(STATUS "CMAKE_SYSTEM_PROCESSOR := ${CMAKE_SYSTEM_PROCESSOR}")

if ("${CMAKE_SYSTEM_PROCESSOR}" IN_LIST X86_CPUS)
    message(STATUS "Building optimised x86/x86_64 binaries")
    target_compile_options(hardnested_nosimd BEFORE PRIVATE
            -mno-mmx -mno-sse2 -mno-avx -mno-avx2 -mno-avx512f)

    set_property(TARGET hardnested_nosimd PROPERTY POSITION_INDEPENDENT_CODE ON)

    ## x86 / MMX
    add_library(hardnested_mmx OBJECT
            ${PM3_ROOT}/client/hardnested/hardnested_bf_core.c
            ${PM3_ROOT}/client/hardnested/hardnested_bitarray_core.c)

    target_compile_options(hardnested_mmx BEFORE PRIVATE
            -mmmx -mno-sse2 -mno-avx -mno-avx2 -mno-avx512f)

    target_include_directories(hardnested_mmx PRIVATE
            ${PM3_ROOT}/common
            ${PM3_ROOT}/client
            ${PM3_ROOT}/client/hardnested)

    set_property(TARGET hardnested_mmx PROPERTY POSITION_INDEPENDENT_CODE ON)

    ## x86 / SSE2
    add_library(hardnested_sse2 OBJECT
            ${PM3_ROOT}/client/hardnested/hardnested_bf_core.c
            ${PM3_ROOT}/client/hardnested/hardnested_bitarray_core.c)

    target_compile_options(hardnested_sse2 BEFORE PRIVATE
            -mmmx -msse2 -mno-avx -mno-avx2 -mno-avx512f)

    target_include_directories(hardnested_sse2 PRIVATE
            ${PM3_ROOT}/common
            ${PM3_ROOT}/client
            ${PM3_ROOT}/client/hardnested)

    set_property(TARGET hardnested_sse2 PROPERTY POSITION_INDEPENDENT_CODE ON)

    ## x86 / AVX
    add_library(hardnested_avx OBJECT
            ${PM3_ROOT}/client/hardnested/hardnested_bf_core.c
            ${PM3_ROOT}/client/hardnested/hardnested_bitarray_core.c)

    target_compile_options(hardnested_avx BEFORE PRIVATE
            -mmmx -msse2 -mavx -mno-avx2 -mno-avx512f)

    target_include_directories(hardnested_avx PRIVATE
            ${PM3_ROOT}/common
            ${PM3_ROOT}/client
            ${PM3_ROOT}/client/hardnested)

    set_property(TARGET hardnested_avx PROPERTY POSITION_INDEPENDENT_CODE ON)

    ## x86 / AVX2
    add_library(hardnested_avx2 OBJECT
            ${PM3_ROOT}/client/hardnested/hardnested_bf_core.c
            ${PM3_ROOT}/client/hardnested/hardnested_bitarray_core.c)

    target_compile_options(hardnested_avx2 BEFORE PRIVATE
            -mmmx -msse2 -mavx -mavx2 -mno-avx512f)

    target_include_directories(hardnested_avx2 PRIVATE
            ${PM3_ROOT}/common
            ${PM3_ROOT}/client
            ${PM3_ROOT}/client/hardnested)

    set_property(TARGET hardnested_avx2 PROPERTY POSITION_INDEPENDENT_CODE ON)

    ## x86 / AVX512
    add_library(hardnested_avx512 OBJECT
            ${PM3_ROOT}/client/hardnested/hardnested_bf_core.c
            ${PM3_ROOT}/client/hardnested/hardnested_bitarray_core.c)

    target_compile_options(hardnested_avx512 BEFORE PRIVATE
            -mmmx -msse2 -mavx -mavx2 -mavx512f)

    target_include_directories(hardnested_avx512 PRIVATE
            ${PM3_ROOT}/common
            ${PM3_ROOT}/client
            ${PM3_ROOT}/client/hardnested)

    set_property(TARGET hardnested_avx512 PROPERTY POSITION_INDEPENDENT_CODE ON)

    set(SIMD_TARGETS
            $<TARGET_OBJECTS:hardnested_mmx>
            $<TARGET_OBJECTS:hardnested_sse2>
            $<TARGET_OBJECTS:hardnested_avx>
            $<TARGET_OBJECTS:hardnested_avx2>
            $<TARGET_OBJECTS:hardnested_avx512>)
else ()
    message(STATUS "Not building optimised targets")
    set(SIMD_TARGETS)
endif ()

#定义为静态库，被最终的pm3库依赖!
add_library(hardnested STATIC
        $<TARGET_OBJECTS:hardnested_nosimd>
        ${SIMD_TARGETS})

#声明变量
set(COMMAPPING_FILE ${PROJECTDIR}/commapping/src/main/jniLibs/${ANDROID_ABI}/libcommapping.so)

#添加动态库链接!
target_link_libraries(pm3 ${COMMAPPING_FILE} hardnested android log z)