//
// Created by c2yu on 2021/5/8.
//

#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include "x264.h"
#include "x264_encode.h"
#include "android/log.h"
#include "safe_queue.h"
#include "h264-encode.h"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "x264-encode", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , "x264-encode", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "x264-encode", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN  , "x264-encode", __VA_ARGS__)

// 输出文件
//FILE *x264_file;
// h264的队列
LinkedQueue *h264_queue;

uint32_t x264_csp;
uint32_t x264_width;
uint32_t x264_height;
x264_param_t *param;
// 处理h264单元数据
int i_nal = 0;
x264_nal_t *nal = NULL;
// x264
x264_t *h = NULL;
x264_picture_t *pic_in;
x264_picture_t *pic_out;
int i_frame = 0;
// x264初始化状态
int x264_enc_state = X264_ENC_UNINITIALIZED;
// x264编码状态
int x264_enc_encoding_state = X264_ENC_STOPPED;

int x264_enc_init(int width, int height, const char *x264_file_path, int yuv_format) {
    if (x264_enc_state == X264_ENC_INITIALIZED) {
        LOGE("X264 encoder has been initialized! Cannot be initialize again!");
        return X264_ENC_FAIL;
    }
    if (!x264_file_path) {
        LOGE("h264 path cannot be null");
        return X264_ENC_FAIL;
    }
//    x264_file = fopen(x264_file_path, "w+");
//    if (x264_file == NULL) {
//        LOGE("cannot open h264 file");
//        return X264_ENC_FAIL;
//    }

    h264_queue = create_queue();
    if (h264_queue == NULL) {
        LOGE("initialization h264 queue failed");
        return X264_ENC_FAIL;
    }

    x264_width = width;
    x264_height = height;

    // 重置
    i_nal = 0;
    i_frame = 0;

    // 设置x264处理的yuv格式默认为YUV420
    x264_csp = X264_CSP_I420;
    switch (yuv_format) {
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
//    x264_param_default(param);
    x264_param_default_preset(param, x264_preset_names[5], x264_tune_names[7]);

    // 配置处理级别
    x264_param_apply_profile(param, x264_profile_names[2]);
    // 编码复杂度
    param->i_level_idc = 32;

    param->i_width = width;
    param->i_height = height;
    param->i_csp = x264_csp;

    param->i_fps_num = 30;
    param->i_fps_den = 1;
    param->rc.i_rc_method = X264_RC_ABR;
    param->rc.i_bitrate = 1000;
    param->rc.i_vbv_max_bitrate = 1200;
    param->rc.i_vbv_buffer_size = 1000;


    // 通过配置的参数打开编码器
    h = x264_encoder_open(param);

    x264_picture_init(pic_out);
    x264_picture_alloc(pic_in, param->i_csp, param->i_width, param->i_height);

    LOGI("init x264 success and the params is success:\n %dx%d csp:%d:", x264_width, x264_height,
         x264_csp);

    x264_enc_state = X264_ENC_INITIALIZED;

    return X264_ENC_OK;
}

int x264_enc_encode_data() {
    if (queue_is_empty(h264_queue)) {
        LOGW("queue is empty and waiting...");
        return X264_ENC_FAIL;
    }
    char *buffer = pop_data(h264_queue);

    if (buffer == NULL) {
        LOGE("buffer is NULL");
        return X264_ENC_FAIL;
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
        return X264_ENC_FAIL;
    }
    LOGI("encode frame: %d\n", i_frame);
    // 将编码数据循环写入目标文件
    int sps_i = 0;
    for (int j = 0; j < i_nal; ++j) {
        if (nal[j].i_type == NAL_SPS) {
            LOGI("this nal is sps, count is: %d", nal[j].i_payload);
            sps_i = j;
        } else if (nal[j].i_type == NAL_PPS) {
            LOGI("this nal is pps, count is: %d", nal[j].i_payload);
            int size = nal[sps_i].i_payload + nal[j].i_payload;
            unsigned char data[size];
            memcpy(data,nal[sps_i].p_payload,nal[sps_i].i_payload);
            int pps_i = 0;
            for (int i = nal[sps_i].i_payload; i < size; ++i) {
                data[i] = nal[j].p_payload[pps_i];
                pps_i++;
            }
            call_java_encode_h264(data, size);
        } else {
            // 回调java层x264编码结果
            call_java_encode_h264(nal[j].p_payload, nal[j].i_payload);
//        fwrite(nal[j].p_payload, 1, nal[j].i_payload, x264_file);
        }
    }
    i_frame++;
    return X264_ENC_OK;
}


void x264_enc_release_data() {
    x264_picture_clean(pic_in);
    x264_encoder_close(h);
    // 释放分配的空间
    free(pic_in);
    free(pic_out);
    free(param);
    // 关闭文件输入
//    fclose(x264_file);

    LOGI("Succeed to release x264!");
}

int x264_enc_data(char *buffer) {
    // 初始化之后才能进行编码
    if (x264_enc_state == X264_ENC_UNINITIALIZED) {
        LOGE(" X264 encoder can be used After it is initialized!");
        return X264_ENC_FAIL;
    }
    if (h264_queue == NULL) {
        LOGE("X264 data queue can be used After it is initialized!");
        return X264_ENC_FAIL;
    }
    // 将编码前的数据放入缓存
    push_data(h264_queue, buffer);
    // 如果编码已经停止，需重新启动
    if (x264_enc_encoding_state == X264_ENC_STOPPED) {
        do {
            int ret = x264_enc_encode_data();
            // 被用户释放之后，释放资源并跳出循环编码
            if (x264_enc_state == X264_ENC_UNINITIALIZED) {
                x264_enc_release_data();
                break;
            }
            if (ret == X264_ENC_FAIL) {
                x264_enc_encoding_state = X264_ENC_STOPPED;
            } else {
                x264_enc_encoding_state = X264_ENC_ENCODING;
            }
        } while (x264_enc_encoding_state == X264_ENC_ENCODING);
    }
    return X264_ENC_OK;
}


void x264_enc_release() {
    if (x264_enc_encoding_state == X264_ENC_STOPPED) {
        // 当前编码已经停止
        x264_enc_release_data();
    }

    x264_enc_state = X264_ENC_UNINITIALIZED;
    x264_enc_encoding_state = X264_ENC_STOPPED;
}


// 冲刷缓冲区，不执行可能造成数据不完整
//    int i = 0;
//    while (1) {
//        int ret = x264_encoder_encode(h, &nal, &i_nal, NULL, pic_out);
//        if (ret == 0) {
//            break;
//        }
//        LOGI("flush %d frame",i);
//        // 将编码数据循环写入目标文件
//        for (int j = 0; j < i_nal; ++j) {
//            fwrite(nal[j].p_payload, 1, nal[j].i_payload, x264_file);
//        }
//        i++;
//    }
//    LOGI("flush over");
