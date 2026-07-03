cmake_minimum_required(VERSION 3.22.1)
project(ivd_analyzer_android)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

if(NOT DEFINED OpenCV_DIR)
    message(FATAL_ERROR "OpenCV_DIR is not set. Add OpenCV_DIR=/path/to/OpenCV-android-sdk/sdk/native/jni to gradle.properties or local.properties")
endif()

find_package(OpenCV REQUIRED)

add_library(
    ivd_analyzer
    SHARED
        android_jni/src/ivd_strip_analyzer.cpp
        android_jni/src/ivd_strip_analyzer_c_api.cpp
        android_jni/native_bridge.cpp
)

target_include_directories(
    ivd_analyzer
    PRIVATE
        android_jni/include
        ${OpenCV_INCLUDE_DIRS}
)

find_library(log-lib log)
find_library(jnigraphics-lib jnigraphics)

target_link_libraries(
    ivd_analyzer
    ${OpenCV_LIBS}
    ${log-lib}
    ${jnigraphics-lib}
)
