//
// Created by c2yu on 2021/5/7.
//
#include "aacenc_lib.h"
#include "android/log.h"
#include <malloc.h>

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "fdk-aac-core", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , "fdk-aac-core", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "fdk-aac-core", __VA_ARGS__)

HANDLE_AACENCODER aac_encoder_handler;
AACENC_InfoStruct aac_enc_info;

int fdk_aac_open(int sample_rate, int channel, int bitrate) {
    int ret = aacEncOpen(&aac_encoder_handler, 0, 0);
    if (ret != AACENC_OK) {
        LOGE("failed to open aac encoder!");
        return -1;
    }
    // 设置编码规格
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_AOT, AOT_AAC_LC);
    if (ret != AACENC_OK) {
        LOGE("failed to set aac encode aot!");
        return -1;
    }
    // 设置采样率
    if (sample_rate <= 0) {
        sample_rate = 44100;
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
    // 初始化编码器
    ret = aacEncEncode(aac_encoder_handler, NULL,NULL,NULL,NULL);
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
    return ret;
}

int fdk_aac_encode(char *in_data, int in_size){
    AACENC_BufDesc *in_buf = malloc(sizeof(AACENC_BufDesc));
    AACENC_BufDesc *out_buf = malloc(sizeof(AACENC_BufDesc));
    AACENC_InArgs *in_args = malloc(sizeof(AACENC_InArgs));
    AACENC_OutArgs *out_args = malloc(sizeof(AACENC_OutArgs));

    in_buf->bufs = (void**)in_data;
    in_args->numInSamples = in_size;
    int ret = aacEncEncode(aac_encoder_handler,in_buf,out_buf,in_args,out_args);
//    fwrite(out_buf->bufs, 1, out_args->numOutBytes, outstream);
    if (ret != AACENC_OK){
        LOGE("failed to encode pcm data to aac data!");
        return -1;
    }
    return ret;
}


int fdk_aac_close() {
    return aacEncClose(&aac_encoder_handler);
}


