# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("x264cmake")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

set( JNI_LIBS_DIR src/main/nativeLibs)


add_library(
            h264
            STATIC
            IMPORTED
)

SET_TARGET_PROPERTIES(
        h264
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/${JNI_LIBS_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libx264.a
)

include_directories(src/main/cpp/libx264)

add_library(
        rtmp
        SHARED
        IMPORTED )


SET_TARGET_PROPERTIES(
        rtmp
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/${JNI_LIBS_DIR}/${CMAKE_ANDROID_ARCH_ABI}/librtmp.so
)

include_directories(src/main/cpp/librtmp)


#add_library(
#        rtmp-core
#        SHARED
#        src/main/cpp/rtmp_core.c
#)

add_library(
        fdk-aac
        STATIC
        IMPORTED )


SET_TARGET_PROPERTIES(
        fdk-aac
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/${JNI_LIBS_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libfdk-aac.a
)

include_directories(src/main/cpp/libfdk-aac)

add_library(
        faac
        SHARED
        IMPORTED )


SET_TARGET_PROPERTIES(
        faac
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/${JNI_LIBS_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libfaac.so
)

include_directories(src/main/cpp/libfaac)

add_library(
        h264-encode
        SHARED
        src/main/cpp/h264-encode.cpp
        src/main/cpp/rtmp_pusher.cpp
        src/main/cpp/x264_encode.c
        src/main/cpp/safe_queue.c
        src/main/cpp/fdk-aac_encode.c
        src/main/cpp/rtmp_core.c
        src/main/cpp/faac_encode.c
)


#add_library(
#        fdk-aac-encode
#        SHARED
#        src/main/cpp/fdk-aac_core.c src/main/cpp/safe_queue.c
#)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.


find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       h264-encode
                       h264
                       rtmp
                       fdk-aac
                       faac
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )