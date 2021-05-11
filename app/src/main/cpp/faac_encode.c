//
// Created by c2yu on 2021/5/10.
//

#include <stdio.h>
#include <time.h>
#include "faac_encode.h"
#include "faac.h"
#include "safe_queue.h"
#include "android/log.h"
#include "h264-encode.h"
#include "rtmp_core.h"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "faac-encode", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , "faac-encode", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "faac-encode", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN  , "faac-encode", __VA_ARGS__)

uint64_t in_samples = 0;
uint64_t max_out_bytes = 0;
// faac实例句柄
faacEncHandle faac_enc_handle;
// 输出的aac文件
FILE *faac_file;
// h264的队列
LinkedQueue *faac_queue;
// fdk-aac初始化状态
int faac_enc_state = FAAC_ENC_UNINITIALIZED;
// fdk-aac编码状态
int faac_enc_encoding_state = FAAC_ENC_STOPPED;

int faac_enc_init(uint64_t sample_rate, uint64_t channel, uint64_t bit_rate, uint64_t pcm_bit_size,
                  const char *aac_path) {

    if (faac_enc_state == FAAC_ENC_INITIALIZED) {
        LOGE("Faac encoder has been initialized! Cannot be initialize again!");
        return FAAC_ENC_FAIL;
    }
    if (!aac_path) {
        LOGE("AAC path cannot be NULL!");
        return FAAC_ENC_FAIL;
    }
    faac_file = fopen(aac_path, "wb");
    if (faac_file == NULL) {
        LOGE("Cannot open aac file");
        return FAAC_ENC_FAIL;
    }

    faac_queue = create_queue();
    if (faac_queue == NULL) {
        LOGE("Failed to initialize aac queue!");
        return FAAC_ENC_FAIL;
    }

    int input_format = FAAC_INPUT_16BIT;
    switch (pcm_bit_size) {
        case 16:
            input_format = FAAC_INPUT_16BIT;
            break;
        case 24:
            input_format = FAAC_INPUT_24BIT;
            break;
        case 32:
            input_format = FAAC_INPUT_32BIT;
            break;
        default:
            input_format = FAAC_INPUT_16BIT;
            break;
    }

    faac_enc_handle = faacEncOpen(sample_rate, channel, &in_samples, &max_out_bytes);
    if (faac_enc_handle == NULL) {
        LOGE("Failed to open faac enc!");
        return FAAC_ENC_FAIL;
    }

    // faac配置
    faacEncConfigurationPtr  faac_enc_configuration_ptr = faacEncGetCurrentConfiguration(faac_enc_handle);
    // 设置输入pcm格式
    faac_enc_configuration_ptr->inputFormat = input_format;
    // 输出格式 (0 = Raw; 1 = ADTS)
    faac_enc_configuration_ptr->outputFormat = FAAC_OUT_ADTS;
    // 设置mpeg版本，此处使用4.0
    faac_enc_configuration_ptr->mpegVersion = MPEG4;
    // 低复杂度AAC编码规格，即是 AAC-LC 标准，可兼容几乎全部
    faac_enc_configuration_ptr->aacObjectType = LOW;
    // 设置0声道作为左声道
//    faac_enc_configuration_ptr->useLfe = 0;
    // 设置比特率
    faac_enc_configuration_ptr->bitRate = bit_rate;

    // 将上述所有配置设置
    faacEncSetConfiguration(faac_enc_handle, faac_enc_configuration_ptr);

    LOGI("Succeed to initialize faac enc!");
    faac_enc_state = FAAC_ENC_INITIALIZED;

    return FAAC_ENC_OK;
}

int faac_enc_encode_data(int in_size) {
    if (queue_is_empty(faac_queue)) {
        LOGW("queue is empty and waiting...");
        return FAAC_ENC_FAIL;
    }
    char *in_data = pop_data(faac_queue);
    if (in_data == NULL) {
        LOGE("buffer is NULL");
        return FAAC_ENC_FAIL;
    }
    // 定义一个输出缓存
    uint8_t out_buffer[max_out_bytes];

    LOGI("input pcm data size is %d", in_size);
    int32_t *data = (int32_t *) in_data;

    int out_byte_size = faacEncEncode(faac_enc_handle, data, in_samples, out_buffer, max_out_bytes);
    if (out_byte_size > 0) {
//        time_t now;
//        time(&now);
//        rtmp_sender_write_audio_frame(out_buffer,out_byte_size,now,0);
//        call_java_encode_aac(out_buffer,out_byte_size);
        fwrite(out_buffer, 1, out_byte_size, faac_file);
    }
    return FAAC_ENC_OK;
}

void faac_enc_release_data() {
    // 关闭文件输入
    if (faac_file!=NULL) fclose(faac_file);
    faacEncClose(faac_enc_handle);

    in_samples = 0;
    max_out_bytes = 0;

    LOGI("Succeed to release faac!");
}

int faac_enc_data(char *buffer, int size) {
    // 初始化之后才能进行编码
    if (faac_enc_state == FAAC_ENC_UNINITIALIZED) {
        LOGE(" Faac encoder can be used After it is initialized!");
        return FAAC_ENC_FAIL;
    }
    if (faac_queue == NULL) {
        LOGE("Faac data queue can be used After it is initialized!");
        return FAAC_ENC_FAIL;
    }

    // 将编码前的数据放入缓存
    push_data(faac_queue, buffer);
    // 如果编码已经停止，需重新启动
    if (faac_enc_encoding_state == FAAC_ENC_STOPPED) {
        do {
            int ret = faac_enc_encode_data(size);
            // 被用户释放之后，释放资源并跳出循环编码
            if (faac_enc_state == FAAC_ENC_UNINITIALIZED) {
                faac_enc_release_data();
                break;
            }
            if (ret == FAAC_ENC_FAIL) {
                faac_enc_encoding_state = FAAC_ENC_STOPPED;
            } else {
                faac_enc_encoding_state = FAAC_ENC_ENCODING;
            }
        } while (faac_enc_encoding_state == FAAC_ENC_ENCODING);
    }
    return FAAC_ENC_OK;
}


void faac_enc_release() {
    if (faac_enc_encoding_state == FAAC_ENC_STOPPED) {
        // 当前编码已经停止
        faac_enc_release_data();
    }

    faac_enc_state = FAAC_ENC_UNINITIALIZED;
    faac_enc_encoding_state = FAAC_ENC_STOPPED;
}

