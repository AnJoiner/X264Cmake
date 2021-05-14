//
// Created by c2yu on 2021/5/13.
//

extern "C" {
#include <jni.h>
#include "yuv_convert.h"
#include "yuv_rotate.h"
}


void throw_exception(JNIEnv *env, const char *exception, const char *message) {
    jclass clazz = env->FindClass(exception);
    if (NULL != clazz) {
        env->ThrowNew(clazz, message);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_coder_x264cmake_jni_YuvCore_nv21ToI420(JNIEnv *env, jobject thiz, jbyteArray src,
                                                jbyteArray dst, jint width, jint height) {
    if (src == NULL || dst == NULL) {
        throw_exception(env, "java/lang/RuntimeException", "Src or dst byte array cannot be NULL!");
    }
    if (width <= 0 || height <= 0) {
        throw_exception(env, "java/lang/RuntimeException",
                        "Width and height must be greater than 0!");
    }

    jbyte *nv21_data = env->GetByteArrayElements(src, JNI_FALSE);
    jbyte *i420_data = env->GetByteArrayElements(dst, JNI_FALSE);

    nv21_to_i420((char *) nv21_data, (char *) i420_data, width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_coder_x264cmake_jni_YuvCore_i420ToNv21(JNIEnv *env, jobject thiz, jbyteArray src,
                                                jbyteArray dst, jint width, jint height) {
    if (src == NULL || dst == NULL) {
        throw_exception(env, "java/lang/RuntimeException", "Src or dst byte array cannot be NULL!");
    }
    if (width <= 0 || height <= 0) {
        throw_exception(env, "java/lang/RuntimeException",
                        "Width and height must be greater than 0!");
    }

    jbyte *i420_data = env->GetByteArrayElements(src, JNI_FALSE);
    jbyte *nv21_data = env->GetByteArrayElements(dst, JNI_FALSE);
    i420_to_nv21((char *) i420_data, (char *) nv21_data, width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_coder_x264cmake_jni_YuvCore_rotateI420(JNIEnv *env, jobject thiz, jbyteArray src,
                                                jbyteArray dst, jint width, jint height,
                                                jint degree) {
    if (src == NULL || dst == NULL) {
        throw_exception(env, "java/lang/RuntimeException", "Src or dst byte array cannot be NULL!");
    }
    if (width <= 0 || height <= 0) {
        throw_exception(env, "java/lang/RuntimeException",
                        "Width and height must be greater than 0!");
    }

    if (degree != 0 && degree != 90 && degree != 180 && degree != 270) {
        throw_exception(env, "java/lang/RuntimeException",
                        "The degree of rotation must be one of 0, 90, 180, 270!");
    }

    jbyte *i420_src = env->GetByteArrayElements(src, JNI_FALSE);
    jbyte *i420_dst = env->GetByteArrayElements(dst, JNI_FALSE);
    rotate_i420((char *) i420_src, (char *) i420_dst, width, height, degree);
}