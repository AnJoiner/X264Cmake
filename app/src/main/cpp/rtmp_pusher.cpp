//
// Created by c2yu on 2021/5/10.
//

#include "android/log.h"

extern "C" {
#include "jni.h"
#include "rtmp_core.h"
}

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "rtmp-pusher", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , "rtmp-pusher", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "rtmp-pusher", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN  , "rtmp-pusher", __VA_ARGS__)

extern "C"
JNIEXPORT jint JNICALL
Java_com_coder_x264cmake_jni_RtmpPusher_rtmp_1pusher_1open(JNIEnv *env, jobject thiz, jstring url,
                                                           jint width, jint height) {
    char *rtmp_url = const_cast<char *>(env->GetStringUTFChars(url, JNI_FALSE));
//    int ret = rtmp_sender_alloc(rtmp_url);
//    if (ret == RTMP_OK){
//        rtmp_sender_start_publish(width,height);
//    }

    return rtmp_pusher_open(rtmp_url,width,height);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_coder_x264cmake_jni_RtmpPusher_rtmp_1pusher_1close(JNIEnv *env, jobject thiz) {
    return rtmp_sender_close();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_coder_x264cmake_jni_RtmpPusher_rtmp_1pusher_1is_1connected(JNIEnv *env, jobject thiz) {
    return rtmp_is_connected();
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_coder_x264cmake_jni_RtmpPusher_rtmp_1pusher_1push_1audio(JNIEnv *env, jobject thiz,
                                                                  jbyteArray data, jint size,
                                                                  jlong timestamp) {
    jbyte *bytes = env->GetByteArrayElements(data, JNI_FALSE);
    unsigned char *buffer = new unsigned char[size];
    for (int i = 0; i < size; ++i) {
        buffer[i] = bytes[i];
    }
    if (!rtmp_is_connected()){
        LOGE("Failed to connect the rtmp server!");
        return -1;
    }
    int ret = rtmp_sender_write_audio_frame(buffer,size,timestamp,0);
    delete[] buffer;
    return ret;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_coder_x264cmake_jni_RtmpPusher_rtmp_1pusher_1push_1video(JNIEnv *env, jobject thiz,
                                                                  jbyteArray data, jint size,
                                                                  jlong timestamp) {
    jbyte *bytes = env->GetByteArrayElements(data, JNI_FALSE);
    unsigned char *buffer = new unsigned char[size];
    for (int i = 0; i < size; ++i) {
        buffer[i] = bytes[i];
    }
    int ret = rtmp_sender_write_video_frame(buffer,size,timestamp,0);
    delete[] buffer;
    return ret;
}



