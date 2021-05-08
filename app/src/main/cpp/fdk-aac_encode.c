//
// Created by c2yu on 2021/5/7.
//

#include <malloc.h>
#include <string.h>
#include "aacenc_lib.h"
#include "android/log.h"
#include "fdk-aac_encode.h"
#include "safe_queue.h"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "fdkaac-encode", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , "fdkaac-encode", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "fdkaac-encode", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN  , "fdkaac-encode", __VA_ARGS__)

HANDLE_AACENCODER aac_encoder_handler;
// 编码信息
AACENC_InfoStruct aac_enc_info;
// 输出的aac文件
FILE *aac_file;
// 编码后最大的长度
int encode_max_frame;
// h264的队列
LinkedQueue *aac_queue;
// fdk-aac初始化状态
int fdkaac_enc_state = FDKAAC_ENC_UNINITIALIZED;
// fdk-aac编码状态
int fdkaac_enc_encoding_state = FDKAAC_ENC_STOPPED;


int fdk_aac_enc_init(int sample_rate, int channel, int bitrate, const char *aac_path) {
    int channel_mode = MODE_2;

    if (fdkaac_enc_state == FDKAAC_ENC_INITIALIZED) {
        LOGE("Fdk-aac encoder has been initialized! Cannot be initialize again!");
        return FDKAAC_ENC_FAIL;
    }
    if (!aac_path) {
        LOGE("AAC path cannot be NULL!");
        return FDKAAC_ENC_FAIL;
    }
    aac_file = fopen(aac_path, "wb");
    if (aac_file == NULL) {
        LOGE("Cannot open aac file");
        return FDKAAC_ENC_FAIL;
    }

    aac_queue = create_queue();
    if (aac_queue == NULL) {
        LOGE("Initialization aac queue failed");
        return FDKAAC_ENC_FAIL;
    }

    int ret = aacEncOpen(&aac_encoder_handler, 0, channel);
    if (ret != AACENC_OK) {
        LOGE("Failed to open aac encoder!");
        return FDKAAC_ENC_FAIL;
    }
    // 设置编码规格 - 低延迟
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_AOT, AOT_AAC_LC);
    if (ret != AACENC_OK) {
        LOGE("Failed to set aac encode aot!");
        return FDKAAC_ENC_FAIL;
    }
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_SAMPLERATE, sample_rate);
    if (ret != AACENC_OK) {
        LOGE("Failed to set aac encode sample rate!");
        return FDKAAC_ENC_FAIL;
    }
    // 设置声道数
    switch (channel) {
        case 1:
            channel_mode = MODE_1;
            break;
        case 2:
            channel_mode = MODE_2;
            break;
        case 3:
            channel_mode = MODE_1_2;
            break;
        case 4:
            channel_mode = MODE_1_2_1;
            break;
    }
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_CHANNELMODE, channel_mode);

    if (ret != AACENC_OK) {
        LOGE("Failed to set aac encode channel mode!");
        return FDKAAC_ENC_FAIL;
    }
    // 声道顺序
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_CHANNELORDER, 1);
    if (ret != AACENC_OK) {
        LOGE("Failed to set aac encode channel order!");
        return FDKAAC_ENC_FAIL;
    }
    // 设置码率模式
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_BITRATEMODE, 0);
    if (ret != AACENC_OK) {
        LOGE("Failed to set aac encode bitrate mode!");
        return FDKAAC_ENC_FAIL;
    }
    // 设置码率
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_BITRATE, bitrate);
    if (ret != AACENC_OK) {
        LOGE("Failed to set aac encode bitrate!");
        return FDKAAC_ENC_FAIL;
    }
    // 设置封装格式 mpeg4/latm
    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_TRANSMUX, TT_MP4_ADTS);
    if (ret != AACENC_OK) {
        LOGE("Failed to set aac encode muxer!");
        return FDKAAC_ENC_FAIL;
    }
//    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_AFTERBURNER,1);
//    if (ret != AACENC_OK) {
//        LOGE("Failed to set aac encode afterburner!");
//        return FDKAAC_ENC_FAIL;
//    }
    // 设置元数据长度
//    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_GRANULE_LENGTH, 480);
//    if (ret != AACENC_OK) {
//        LOGE("Failed to set aac encode granule length!");
//        return FDKAAC_ENC_FAIL;
//    }
    // 设置编码复用个数
//    ret = aacEncoder_SetParam(aac_encoder_handler, AACENC_TPSUBFRAMES, 2);
//    if (ret != AACENC_OK) {
//        LOGE("Failed to set aac encode sub frames!");
//        return FDKAAC_ENC_FAIL;
//    }
    // 初始化编码器
    ret = aacEncEncode(aac_encoder_handler, NULL, NULL, NULL, NULL);
    if (ret != AACENC_OK) {
        LOGE("Failed to init aac encoder!");
        return FDKAAC_ENC_FAIL;
    }
    // 获取编码信息
    ret = aacEncInfo(aac_encoder_handler, &aac_enc_info);
    if (ret != AACENC_OK) {
        LOGE("Failed to get aac encode info!");
        return FDKAAC_ENC_FAIL;
    }
    encode_max_frame = aac_enc_info.maxOutBufBytes;

    fdkaac_enc_state = FDKAAC_ENC_INITIALIZED;

    return FDKAAC_ENC_OK;
}

int fdk_aac_enc_encode_data(int in_size) {
    if (queue_is_empty(aac_queue)) {
        LOGW("queue is empty and waiting...");
        return FDKAAC_ENC_FAIL;
    }
    char *in_data = pop_data(aac_queue);
    if (in_data == NULL) {
        LOGE("buffer is NULL");
        return FDKAAC_ENC_FAIL;
    }
    LOGI("input pcm data size is %d", in_size);
    // 编码的输出缓存
    uint8_t aac_out_buffer[20480];
    // 初始化编码相关参数
    AACENC_BufDesc in_buf = {0}, out_buf = {0};
    AACENC_InArgs in_args = {0};
    AACENC_OutArgs out_args = {0};

    // 音频输入缓冲器
    int in_identifier = IN_AUDIO_DATA;
    int in_elem_size = 2;
    void *in_ptr = in_data;

    // 有效输入样本
    in_args.numInSamples = in_size <= 0 ? -1 : in_size / 2;  //size为pcm字节数

    in_buf.numBufs = 1;
    in_buf.bufs = &in_ptr;  //data为pcm数据指针
    in_buf.bufferIdentifiers = &in_identifier;
    in_buf.bufSizes = &in_size;
    in_buf.bufElSizes = &in_elem_size;

    int out_identifier = OUT_BITSTREAM_DATA;
    void *out_ptr = aac_out_buffer;
    int out_size = sizeof(aac_out_buffer);
    int out_elem_size = 1;

    out_buf.numBufs = 1;
    out_buf.bufs = &out_ptr;
    out_buf.bufferIdentifiers = &out_identifier;
    out_buf.bufSizes = &out_size;
    out_buf.bufElSizes = &out_elem_size;

    int ret = aacEncEncode(aac_encoder_handler, &in_buf, &out_buf, &in_args, &out_args);
    if (ret != AACENC_OK) {
        if (ret == AACENC_ENCODE_EOF) {
            return FDKAAC_ENC_OK;
        }
        LOGE("Failed to encode pcm data to aac data!");
        return FDKAAC_ENC_FAIL;
    }
    if (aac_file != NULL) {
        if (out_args.numOutBytes != 0) {
            LOGI("Writing %d bytes to file!", out_args.numOutBytes);
            fwrite(aac_out_buffer, 1, out_args.numOutBytes, aac_file);
        }
    }
    return FDKAAC_ENC_OK;
}

void fdk_aac_enc_release_data() {
    // 关闭文件输入
    fclose(aac_file);
    int ret = aacEncClose(&aac_encoder_handler);
    if (ret == AACENC_OK) {
        LOGI("Succeed to release fdk-aac!");
    } else {
        LOGE("Failed to release fdk-aac!");
    }
}

int fdk_aac_enc_data(char *buffer, int size) {
    // 初始化之后才能进行编码
    if (fdkaac_enc_state == FDKAAC_ENC_UNINITIALIZED) {
        LOGE(" Fdk-aac encoder can be used After it is initialized!");
        return FDKAAC_ENC_FAIL;
    }
    if (aac_queue == NULL) {
        LOGE("Fdk-aac data queue can be used After it is initialized!");
        return FDKAAC_ENC_FAIL;
    }

    // 将编码前的数据放入缓存
    push_data(aac_queue, buffer);
    // 如果编码已经停止，需重新启动
    if (fdkaac_enc_encoding_state == FDKAAC_ENC_STOPPED) {
        do {
            int ret = fdk_aac_enc_encode_data(size);
            // 被用户释放之后，释放资源并跳出循环编码
            if (fdkaac_enc_state == FDKAAC_ENC_UNINITIALIZED) {
                fdk_aac_enc_release_data();
                break;
            }
            if (ret == FDKAAC_ENC_FAIL) {
                fdkaac_enc_encoding_state = FDKAAC_ENC_STOPPED;
            } else {
                fdkaac_enc_encoding_state = FDKAAC_ENC_ENCODING;
            }
        } while (fdkaac_enc_encoding_state == FDKAAC_ENC_ENCODING);
    }
    return FDKAAC_ENC_OK;
}


void fdk_aac_enc_release() {
    if (fdkaac_enc_encoding_state == FDKAAC_ENC_STOPPED) {
        // 当前编码已经停止
        fdk_aac_enc_release_data();
    }

    fdkaac_enc_state = FDKAAC_ENC_UNINITIALIZED;
    fdkaac_enc_encoding_state = FDKAAC_ENC_STOPPED;
}




