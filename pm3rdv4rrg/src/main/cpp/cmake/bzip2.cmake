get_filename_component(BZIP2_ROOT bzip2 ABSOLUTE)

add_library(pm3rrg_rdv4_bzip2 STATIC
        ${BZIP2_ROOT}/blocksort.c
        ${BZIP2_ROOT}/bzlib.c
        ${BZIP2_ROOT}/compress.c
        ${BZIP2_ROOT}/crctable.c
        ${BZIP2_ROOT}/decompress.c
        ${BZIP2_ROOT}/huffman.c
        ${BZIP2_ROOT}/randtable.c
        )

target_include_directories(pm3rrg_rdv4_bzip2 INTERFACE bzip2)
