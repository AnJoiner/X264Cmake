#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include "android/log.h"

extern "C" {
#include "h264-encode.h"
#include "x264.h"
#include "jni.h"
#include "safe_queue.h"
#include "x264_encode.h"
#include "faac_encode.h"
//#include "fdk-aac_encode.h"
}

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "h264-encode", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , "h264-encode", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "h264-encode", __VA_ARGS__)

// Java虚拟机
JavaVM *java_vm;
// 公用环境变量
JNIEnv *jni_env;
// 类对象
jobject c_h264_obj;
jobject c_aac_obj;

extern "C"
JNIEXPORT jint JNICALL
Java_com_coder_x264cmake_jni_X264Encode_init_1aac(JNIEnv *env, jobject thiz, jint sample_rate,
                                                  jint channel, jint bitrate, jstring aac_path) {
    jni_env = env;
    c_aac_obj = jni_env->NewGlobalRef(thiz);
    const char *aac_file_path = env->GetStringUTFChars(aac_path, JNI_FALSE);
    return faac_enc_init(sample_rate, channel, bitrate, 16, aac_file_path);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_coder_x264cmake_jni_X264Encode_encode_1aac_1data(JNIEnv *env, jobject thiz,
                                                          jbyteArray data) {
    jbyte *bytes = env->GetByteArrayElements(data, JNI_FALSE);
    int64_t size = env->GetArrayLength(data);
    char *buffer = new char[size];
    for (int i = 0; i < size; ++i) {
        buffer[i] = bytes[i];
    }
    LOGI("incoming pcm data size %ld", strlen(buffer));
    // 释放资源
    env->ReleaseByteArrayElements(data, bytes, 0);
    int ret = faac_enc_data(buffer, size);
    delete[] buffer;
    return ret;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_coder_x264cmake_jni_X264Encode_release_1aac(JNIEnv *env, jobject thiz) {
    jni_env = env;
    jni_env->DeleteGlobalRef(c_aac_obj);
    faac_enc_release();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_coder_x264cmake_jni_X264Encode_init_1x264(JNIEnv *env, jobject thiz, jint width,
                                                   jint height,
                                                   jstring h264_path, jint yuv_csp) {
    jni_env = env;
    c_h264_obj = jni_env->NewGlobalRef(thiz);
    const char *x264_file_path = env->GetStringUTFChars(h264_path, JNI_FALSE);
    return x264_enc_init(width, height, x264_file_path, yuv_csp);
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_coder_x264cmake_jni_X264Encode_encode_1x264_1data(JNIEnv *env, jobject thiz,
                                                           jbyteArray data) {
    jbyte *bytes = env->GetByteArrayElements(data, JNI_FALSE);
    int64_t size = env->GetArrayLength(data);
    char *buffer = new char[size];
    for (int i = 0; i < size; ++i) {
        buffer[i] = bytes[i];
    }
//    char *buffer = new char[size+1];
//    memset(buffer, 0, size + 1);
//    memcpy(buffer, bytes, size);
//    //     结尾'\0'
//    buffer[size] = 0;

    LOGI("incoming yuv data size %ld", strlen(buffer));
    // 释放资源
    env->ReleaseByteArrayElements(data, bytes, 0);
    int ret = x264_enc_data(buffer, size);
    delete[] buffer;
    return ret;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_coder_x264cmake_jni_X264Encode_release_1x264(JNIEnv *env, jobject thiz) {
    jni_env = env;
    jni_env->DeleteGlobalRef(c_h264_obj);
    x264_enc_release();
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_coder_x264cmake_jni_X264Encode_encode_1x264(JNIEnv *env, jobject thiz, jint width,
                                                     jint height,
                                                     jstring yuv_path, jstring h264_path,
                                                     jint yuv_csp) {
    int ret = 0;
    if (width == 0 || height == 0) {
        LOGE("width or height cannot be zero!");
    }
    const char *yuv_file_path = env->GetStringUTFChars(yuv_path, JNI_FALSE);
    const char *h264_file_path = env->GetStringUTFChars(h264_path, JNI_FALSE);

    if (!yuv_file_path) {
        LOGE("yuv path cannot be null");
        return -1;
    }
    if (!h264_file_path) {
        LOGE("h264 path cannot be null");
        return -1;
    }
    // 打开yuv
    FILE *yuv_file = fopen(yuv_file_path, "rb");
    if (yuv_file == NULL) {
        LOGE("cannot open yuv file");
        return -1;
    }
    FILE *h264_file = fopen(h264_file_path, "wb");
    if (h264_file == NULL) {
        LOGE("cannot open h264 file");
        return -1;
    }
    // 设置x264处理的yuv格式默认为YUV420
    int csp = X264_CSP_I420;
    switch (yuv_csp) {
        case 0:
            csp = X264_CSP_I420;
            break;
        case 1:
            csp = X264_CSP_NV21;
            break;
        case 2:
            csp = X264_CSP_I422;
            break;
        case 3:
            csp = X264_CSP_I444;
            break;
        default:
            csp = X264_CSP_I420;
    }

    LOGI("the params is success:\n %dx%d %s %s:", width, height, yuv_file_path, h264_file_path);

    int frame_number = 0;
    // 处理h264单元数据
    int i_nal = 0;
    x264_nal_t *nal = NULL;
    // x264
    x264_t *h = NULL;
    x264_param_t *param = (x264_param_t *) malloc(sizeof(x264_param_t));;
    x264_picture_t *pic_in = (x264_picture_t *) (malloc(sizeof(x264_picture_t)));
    x264_picture_t *pic_out = (x264_picture_t *) (malloc(sizeof(x264_picture_t)));

    // 初始化编码参数
    x264_param_default(param);
    param->i_width = width;
    param->i_height = height;
    param->i_csp = csp;
    // 配置处理级别
    x264_param_apply_profile(param, x264_profile_names[5]);
    // 通过配置的参数打开编码器
    h = x264_encoder_open(param);

    x264_picture_init(pic_out);
    x264_picture_alloc(pic_in, param->i_csp, param->i_width, param->i_height);
    // 编码前每一帧的字节大小
    int size = param->i_width * param->i_height;

    // 计算视频帧数
    fseek(yuv_file, 0, SEEK_END);
    switch (csp) {
        case X264_CSP_I444:
            // YUV444
            frame_number = ftell(yuv_file) / (size * 3);
            break;
        case X264_CSP_I422:
            // YUV422
            frame_number = ftell(yuv_file) / (size * 2);
            break;
        case X264_CSP_I420:
            //YUV420
            frame_number = ftell(yuv_file) / (size * 3 / 2);
            break;
        case X264_CSP_NV21:
            //YUV420SP
            frame_number = ftell(yuv_file) / (size * 3 / 2);
            break;
        default:
            LOGE("Colorspace Not Support.");
            return -1;
    }
    fseek(yuv_file, 0, SEEK_SET);
    // 循环执行编码
    for (int i = 0; i < frame_number; i++) {
        switch (csp) {
            case X264_CSP_I444:
                fread(pic_in->img.plane[0], size, 1, yuv_file);
                fread(pic_in->img.plane[1], size, 1, yuv_file);
                fread(pic_in->img.plane[2], size, 1, yuv_file);
                break;
            case X264_CSP_I422:
                fread(pic_in->img.plane[0], size, 1, yuv_file);
                fread(pic_in->img.plane[1], size / 2, 1, yuv_file);
                fread(pic_in->img.plane[2], size / 2, 1, yuv_file);
                break;
            case X264_CSP_I420:
                fread(pic_in->img.plane[0], size, 1, yuv_file);
                fread(pic_in->img.plane[1], size / 4, 1, yuv_file);
                fread(pic_in->img.plane[2], size / 4, 1, yuv_file);
                break;
            case X264_CSP_NV21:
                // 只有两个planar，即是 y + vu
                fread(pic_in->img.plane[0], size, 1, yuv_file);
                fread(pic_in->img.plane[1], size / 2, 1, yuv_file);
                break;
        }
        pic_in->i_pts = i;
        // 对每一帧执行编码
        ret = x264_encoder_encode(h, &nal, &i_nal, pic_in, pic_out);
        if (ret < 0) {
            LOGE("x264 encode error");
            return -1;
        }
        LOGI("encode frame: %d\n", i);
        // 将编码数据循环写入目标文件
        for (int j = 0; j < i_nal; ++j) {
            if (nal[j].i_type == NAL_SPS) {
                LOGI("this nal is sps, count is: %d", nal[j].i_payload);
            }
            if (nal[j].i_type == NAL_PPS) {
                LOGI("this nal is pps, count is: %d", nal[j].i_payload);
            }
            fwrite(nal[j].p_payload, 1, nal[j].i_payload, h264_file);
        }
    }

    // 冲刷缓冲区，不执行可能造成数据不完整
    int i = 0;
    while (1) {
        ret = x264_encoder_encode(h, &nal, &i_nal, NULL, pic_out);
        if (ret == 0) {
            break;
        }
        LOGD("flush %d frame", i);
        // 将编码数据循环写入目标文件
        for (int j = 0; j < i_nal; ++j) {
            fwrite(nal[j].p_payload, 1, nal[j].i_payload, h264_file);
        }
        i++;
    }

    x264_picture_clean(pic_in);
    x264_encoder_close(h);
    // 释放分配的空间
    free(pic_in);
    free(pic_out);
    free(param);
    // 关闭文件输入
    fclose(yuv_file);
    fclose(h264_file);

    return ret;
}

void call_java_encode_h264(unsigned char *data, int size) {
    if (!c_h264_obj || !jni_env) {
        LOGE("Jni env is NULL, Check it!");
        return;
    }
    jclass cls = jni_env->GetObjectClass(c_h264_obj);
    if (!cls) {
        LOGE("Class is not found, Check it!");
        return;
    }
    // 获取到当前方法
    jmethodID method_id = jni_env->GetMethodID(cls, "onEncodeH264", "([BI)V");
    jbyteArray byte_array = jni_env->NewByteArray(size);
    jni_env->SetByteArrayRegion(byte_array, 0, size, (jbyte *)data);
    jni_env->CallVoidMethod(c_h264_obj, method_id, byte_array, size);
}


void call_java_encode_aac(unsigned char *data, int size) {
//    if (!c_aac_obj || !jni_env) {
//        LOGE("Jni env is NULL, Check it!");
//        return;
//    }
//    jclass cls = jni_env->GetObjectClass(c_aac_obj);
////    jclass cls = jni_env->FindClass("com/coder/x264cmake/jni/X264Encode");
//    if (!cls) {
//        LOGE("Class is not found, Check it!");
//        return;
//    }
//    // 获取到当前方法
//    jmethodID method_id = jni_env->GetMethodID(cls, "onEncodeAAC","([BI)V");
//    jbyteArray byte_array = jni_env->NewByteArray(size);
//    jni_env->SetByteArrayRegion(byte_array, 0, size, (jbyte *)data);
//    jni_env->CallVoidMethod(c_aac_obj, method_id, byte_array, size);
}