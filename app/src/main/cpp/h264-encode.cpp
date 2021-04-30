#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include "android/log.h"

extern "C" {
#include "x264.h"
#include "jni.h"
#include "safe_queue.h"
}

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "h264-encode", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , "h264-encode", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "h264-encode", __VA_ARGS__)

uint32_t x264_csp;
uint32_t x264_width;
uint32_t x264_height;
FILE *x264_file;

x264_param_t *param;
// 处理h264单元数据
int i_nal = 0;
x264_nal_t *nal = NULL;
// x264
x264_t *h = NULL;
x264_picture_t *pic_in;
x264_picture_t *pic_out;
int i_frame = 0;

// h264的队列
LinkedQueue *h264_queue;
// 是否正在h264编码
int is_encoding_h264 = 0;
// 是否释放资源
int is_release = 0;

extern "C"
JNIEXPORT jint JNICALL
Java_com_coder_x264cmake_jni_X264Encode_initQueue(JNIEnv *env, jobject thiz, jbyteArray data) {
    jbyte *bytes = env->GetByteArrayElements(data, JNI_FALSE);
    int size = env->GetArrayLength(data);

    char *buffer = new char[size + 1];
    memset(buffer, 0, size + 1);
    memcpy(buffer, bytes, size);
    // 结尾'\0'
    buffer[size] = 0;
    // 释放资源
    env->ReleaseByteArrayElements(data, bytes, 0);
    LOGI("%s length is %ld", buffer, strlen(buffer));
    // 释放资源
    return 0;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_coder_x264cmake_jni_X264Encode_init(JNIEnv *env, jobject thiz, jint width, jint height,
                                             jstring h264_path, jint yuv_csp) {
    LOGI("start init x264 and set params...");
    x264_width = width;
    x264_height = height;
    if (x264_width == 0 || x264_height == 0) {
        LOGE("width or height cannot be zero!");
    }
    x264_csp = yuv_csp;

    const char *x264_file_path = env->GetStringUTFChars(h264_path, JNI_FALSE);
    if (!x264_file_path) {
        LOGE("h264 path cannot be null");
        return -1;
    }
    x264_file = fopen(x264_file_path, "wb");
    if (x264_file == NULL) {
        LOGE("cannot open h264 file");
        return -1;
    }

    h264_queue = create_queue();
    if (h264_queue == NULL) {
        LOGE("initialization h264 queue failed");
        return -1;
    }

    // 重置
    i_nal = 0;
    i_frame = 0;
    is_encoding_h264 = 0;
    is_release = 0;

    // 设置x264处理的yuv格式默认为YUV420
    x264_csp = X264_CSP_I420;
    switch (yuv_csp) {
        case 0:
            x264_csp = X264_CSP_I420;
            break;
        case 1:
            x264_csp = X264_CSP_NV21;
            break;
        case 2:
            x264_csp = X264_CSP_I422;
            break;
        case 3:
            x264_csp = X264_CSP_I444;
            break;
        default:
            x264_csp = X264_CSP_I420;
    }


    param = (x264_param_t *) malloc(sizeof(x264_param_t));
    pic_in = (x264_picture_t *) (malloc(sizeof(x264_picture_t)));
    pic_out = (x264_picture_t *) (malloc(sizeof(x264_picture_t)));

    // 初始化编码参数
    x264_param_default(param);
    param->i_width = width;
    param->i_height = height;
    param->i_csp = x264_csp;
    // 配置处理级别
    x264_param_apply_profile(param, x264_profile_names[5]);
    // 通过配置的参数打开编码器
    h = x264_encoder_open(param);

    x264_picture_init(pic_out);
    x264_picture_alloc(pic_in, param->i_csp, param->i_width, param->i_height);

    LOGI("init x264 success and the params is success:\n %dx%d csp:%d:", x264_width, x264_height, x264_csp);

    return 0;
}

int encode_core() {
    if (queue_is_empty(h264_queue)) {
        LOGE("queue is null and return -1");
        return -1;
    }
    char *buffer = pop_data(h264_queue);
    if (buffer == NULL) {
        LOGE("buffer is null and return -1");
        return -1;
    }
    uint32_t size = x264_width * x264_height;
    LOGI("y size is: %d", size);
    // 传入的视频数据就是一帧数据

    switch (x264_csp) {
        case X264_CSP_I444:
            // 三个planar，y:1 u:1 v:1
            memcpy(pic_in->img.plane[0], buffer, size);
            memcpy(pic_in->img.plane[1], buffer + size, size);
            memcpy(pic_in->img.plane[2], buffer + size * 2, size);
            break;
        case X264_CSP_I422:
            // 三个planar，y:1 u:1/2 v:1/2
            memcpy(pic_in->img.plane[0], buffer, size);
            memcpy(pic_in->img.plane[1], buffer + size, size / 2);
            memcpy(pic_in->img.plane[2], buffer + (size * 3 / 2), size / 2);
            break;
        case X264_CSP_I420:
            // 三个planar，y:1 u:1/4 v:1/4
            memcpy(pic_in->img.plane[0], buffer, size);
            memcpy(pic_in->img.plane[1], buffer + size, size / 4);
            memcpy(pic_in->img.plane[2], buffer + (size * 5 / 4), size / 4);
            break;
        case X264_CSP_NV21:
            // 只有两个planar，即是 y + vu
            memcpy(pic_in->img.plane[0], buffer, size);
            memcpy(pic_in->img.plane[1], buffer + size, size / 2);
            break;
    }

    pic_in->i_pts = i_frame;
    // 对每一帧执行编码
    int ret = x264_encoder_encode(h, &nal, &i_nal, pic_in, pic_out);
    if (ret < 0) {
        LOGE("x264 encode error");
        return -1;
    }
    LOGI("encode frame: %d\n", i_frame);
    // 将编码数据循环写入目标文件
    for (int j = 0; j < i_nal; ++j) {
        if (nal[j].i_type == NAL_SPS) {
            LOGI("this nal is sps, count is: %d", nal[j].i_payload);
        }
        if (nal[j].i_type == NAL_PPS) {
            LOGI("this nal is pps, count is: %d", nal[j].i_payload);
        }
        LOGI("this nal count is: %d", nal[j].i_payload);
        fwrite(nal[j].p_payload, 1, nal[j].i_payload, x264_file);
    }
    i_frame++;

    return 0;
}

void flush_release(){
    // 冲刷缓冲区，不执行可能造成数据不完整
    int i = 0;
    while (1) {
        int ret = x264_encoder_encode(h, &nal, &i_nal, NULL, pic_out);
        if (ret == 0) {
            break;
        }
        LOGD("flush %d frame",i);
        // 将编码数据循环写入目标文件
        for (int j = 0; j < i_nal; ++j) {
            fwrite(nal[j].p_payload, 1, nal[j].i_payload, x264_file);
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
    fclose(x264_file);
}

int execute_encode() {
    while (1) {
        LOGI("start encode...");
        int ret = encode_core();
        LOGI("stop encode : %d", ret);
        if (ret == -1) {
            is_encoding_h264 = 0;
            break;
        } else {
            is_encoding_h264 = 1;
        }
    }
    // 用户执行释放资源
    if (is_release){
        flush_release();
    }
    return is_encoding_h264;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_coder_x264cmake_jni_X264Encode_encodeData(JNIEnv *env, jobject thiz, jbyteArray data) {
    if (is_encoding_h264){
        LOGD("it's encoding h264...");
        return -1;
    }
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

    LOGD("incoming data size %ld", strlen(buffer));
    // 释放资源
    env->ReleaseByteArrayElements(data, bytes, 0);

    if (!is_release && h264_queue != NULL) {
        push_data(h264_queue, buffer);
        // 只有当停止时才重新开启循环编码
        if (is_encoding_h264 == 0) {
            execute_encode();
        }
    }
    return 0;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_coder_x264cmake_jni_X264Encode_release(JNIEnv *env, jobject thiz) {
    is_release = 1;
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_coder_x264cmake_jni_X264Encode_encode(JNIEnv *env, jobject thiz, jint width, jint height,
                                               jstring yuv_path, jstring h264_path, jint yuv_csp) {
    int ret = 0;
    // TODO: implement encode()
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
        LOGD("flush %d frame",i);
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