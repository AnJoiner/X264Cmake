//
// Created by c2yu on 2021/5/7.
//
#include "aacenc_lib.h"
#include "android/log.h"
#include "fdk-aac_core.h"
#include "safe_queue.h"
#include <malloc.h>

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "fdk-aac-core", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , "fdk-aac-core", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "fdk-aac-core", __VA_ARGS__)

HANDLE_AACENCODER aac_encoder_handler;
// 编码信息
AACENC_InfoStruct aac_enc_info;
// 输出的aac文件
FILE *aac_file;
// 编码后最大的长度
UINT encode_max_frame;
// h264的队列
LinkedQueue *aac_queue;

int fdk_aac_open(int sample_rate, int channel, int bitrate, char *aac_path) {
    if (!aac_path) {
        LOGE("aac path cannot be null!");
        return -1;
    }
    aac_file = fopen(aac_path, "w+");
    if (aac_file == NULL) {
        LOGE("cannot open aac file");
        return -1;
    }

    aac_queue = create_queue();
    if (aac_queue == NULL) {
        LOGE("initialization aac queue failed");
        return -1;
    }

    int ret = aacEncOpen(&aac_encoder_handler, 0, 0);
    if (ret != AACENC_OK) {
        LOGE("failed to open aac encoder!");
        return -1;
    }
    // 设置编码规格 - 低延迟
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_AOT, AOT_ER_AAC_LD);
    if (ret != AACENC_OK) {
        LOGE("failed to set aac encode aot!");
        return -1;
    }
    // 设置采样率 - 默认48000
    if (sample_rate <= 0) {
        sample_rate = 48000;
    }
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_SAMPLERATE, sample_rate);
    if (ret != AACENC_OK) {
        LOGE("failed to set aac encode sample rate!");
        return -1;
    }
    // 设置声道数
    if (channel <= 0) {
        channel = MODE_2;
    }
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_CHANNELMODE, channel);
    if (ret != AACENC_OK) {
        LOGE("failed to set aac encode channel mode!");
        return -1;
    }
    // 声道顺序
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_CHANNELORDER, 1);
    if (ret != AACENC_OK) {
        LOGE("failed to set aac encode channel order!");
        return -1;
    }
    // 设置码率模式
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_BITRATEMODE, 0);
    if (ret != AACENC_OK) {
        LOGE("failed to set aac encode bitrate mode!");
        return -1;
    }
    // 设置码率
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_BITRATE, bitrate);
    if (ret != AACENC_OK) {
        LOGE("failed to set aac encode bitrate!");
        return -1;
    }
    // 设置封装格式 mpeg4/latm
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_TRANSMUX, TT_MP4_LATM_MCP1);
    if (ret != AACENC_OK) {
        LOGE("failed to set aac encode muxer!");
        return -1;
    }
    // 设置元数据长度
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_GRANULE_LENGTH, 480);
    if (ret != AACENC_OK) {
        LOGE("failed to set aac encode granule length!");
        return -1;
    }
    // 设置编码复用个数
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_TPSUBFRAMES, 2);
    if (ret != AACENC_OK) {
        LOGE("failed to set aac encode sub frames!");
        return -1;
    }
    // 初始化编码器
    ret = aacEncEncode(aac_encoder_handler, NULL, NULL, NULL, NULL);
    if (ret != AACENC_OK) {
        LOGE("failed to init aac encoder!");
        return -1;
    }
    // 获取编码信息
    ret = aacEncInfo(aac_encoder_handler, &aac_enc_info);
    if (ret != AACENC_OK) {
        LOGE("failed to get aac encode info!");
        return -1;
    }
    encode_max_frame = aac_enc_info.maxOutBufBytes;


    return ret;
}

int fdk_aac_encode_data(char *in_data, int size) {
    // 编码的输出缓存
    uint8_t aac_out_buffer[encode_max_frame];
    // 初始化编码相关参数
    AACENC_BufDesc in_buf = {0}, out_buf = {0};
    AACENC_InArgs in_args = {0};
    AACENC_OutArgs out_args = {0};

    void *data = in_data;
    // 音频输入缓冲器
    int in_identifier = IN_AUDIO_DATA;
    int in_elem_size = 2;
    // 有效输入样本
    in_args.numInSamples = size / 2;  //size为pcm字节数

    in_buf.numBufs = 1;
    in_buf.bufs = &data;  //data为pcm数据指针
    in_buf.bufferIdentifiers = &in_identifier;
    in_buf.bufSizes = &size;
    in_buf.bufElSizes = &in_elem_size;

    int out_identifier = OUT_BITSTREAM_DATA;
    void *out_ptr = aac_out_buffer;
    int out_size = encode_max_frame;
    int out_elem_size = 1;

    out_buf.numBufs = 1;
    out_buf.bufs = &out_ptr;
    out_buf.bufferIdentifiers = &out_identifier;
    out_buf.bufSizes = &out_size;
    out_buf.bufElSizes = &out_elem_size;

    int ret = aacEncEncode(aac_encoder_handler, &in_buf, &out_buf, &in_args, &out_args);
    if (aac_file != NULL) {
        fwrite(out_buf.bufs, 1, out_args.numOutBytes, aac_file);
    }
    if (ret != AACENC_OK) {
        LOGE("failed to encode pcm data to aac data!");
        return -1;
    }
    free(&aac_out_buffer);
    return ret;
}

int fdk_aac_encode(char *in_data, int size) {
    if (aac_queue == NULL){
        return -1;
    }
    LOGI("input pcm data size is %d",size);
    push_data(aac_queue, in_data);
    return 0;
}


int fdk_aac_close() {
    int ret = aacEncClose(&aac_encoder_handler);
    if (ret != AACENC_OK) {
        return -1;
    }
    return ret;
}


