# version
cmake_minimum_required(VERSION 3.4.1)

# bzip2 dep
include(cmake/bzip2.cmake)
# pm3 client
add_subdirectory(proxmark3/client/android)