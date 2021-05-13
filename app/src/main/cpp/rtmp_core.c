//
// Created by c2yu on 2021/5/6.
//

#include <stdio.h>
#include <string.h>
#include <stdbool.h>
#include <malloc.h>

#include "librtmp/rtmp.h"
#include "android/log.h"
#include "librtmp/log.h"
#include "rtmp_core.h"

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE  , "rtmp-core", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "rtmp-core", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , "rtmp-core", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN  , "rtmp-core", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "rtmp-core", __VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL  , "rtmp-core", __VA_ARGS__)


// @brief start publish
// @param [in] rtmp_sender handler
// @param [in] flag        stream falg
// @param [in] ts_us       timestamp in us
// @return             : 0: OK; others: FAILED
static const AVal av_onMetaData = AVC("onMetaData");
static const AVal av_duration = AVC("duration");
static const AVal av_width = AVC("width");
static const AVal av_height = AVC("height");
static const AVal av_videocodecid = AVC("videocodecid");
static const AVal av_avcprofile = AVC("avcprofile");
static const AVal av_avclevel = AVC("avclevel");
static const AVal av_videoframerate = AVC("videoframerate");
static const AVal av_audiocodecid = AVC("audiocodecid");
static const AVal av_audiosamplerate = AVC("audiosamplerate");
static const AVal av_audiochannels = AVC("audiochannels");
static const AVal av_avc1 = AVC("avc1");
static const AVal av_mp4a = AVC("mp4a");
static const AVal av_onPrivateData = AVC("onPrivateData");
static const AVal av_record = AVC("record");

static void rtmp_android_log_callback(int level, const char *fmt, va_list vl) {
    char log[1024];
    vsprintf(log, fmt, vl);
    switch (level) {
        case RTMP_LOGDEBUG: LOGV("%s", log); break;
        case RTMP_LOGDEBUG2: LOGD("%s", log); break;
        case RTMP_LOGINFO: LOGI("%s", log); break;
        case RTMP_LOGWARNING: LOGW("%s", log); break;
        case RTMP_LOGERROR: LOGE("%s", log); break;
        case RTMP_LOGCRIT: LOGF("%s", log); break;
    }
}


RTMP *rtmp;
bool video_config_ok = false;
bool audio_config_ok = false;

#define AAC_ADTS_HEADER_SIZE 7
#define FLV_HEADER 9
#define FLV_TAG_HEAD_LEN 11
#define FLV_PRE_TAG_LEN 4


static uint8_t gen_audio_tag_header() {
    /*
    UB [4] Format of SoundData. The following values are defined:
    0 = Linear PCM, platform endian
    1 = ADPCM
    2 = MP3
    3 = Linear PCM, little endian
    4 = Nellymoser 16 kHz mono
    5 = Nellymoser 8 kHz mono
    6 = Nellymoser
    7 = G.711 A-law logarithmic PCM
    8 = G.711 mu-law logarithmic PCM
    9 = reserved
    10 = AAC *****************
    11 = Speex
    14 = MP3 8 kHz
    15 = Device-specific sound
   SoundRate UB [2] Sampling rate. The following values are defined:
    0 = 5.5 kHz
    1 = 11 kHz
    2 = 22 kHz
    3 = 44 kHz  ************* specification says mark it always 44khz
    SoundSize UB [1]
    to 16 bits internally.
    0 = 8-bit samples
    1 = 16-bit samples *************
    SoundType UB [1] Mono or stereo sound
    0 = Mono sound
    1 = Stereo sound ***********  specification says: even if sound is not stereo, mark as stereo
    */
    uint8_t soundType = 1; // should be always 1 - stereo --- config.channel_configuration - 1; //0 mono, 1 stero
    uint8_t soundRate = 3;  //44Khz it should be always 44Khx
    uint8_t val = 0;

    /*
    switch (config.sample_frequency_index) {
        case 10: { //11.025k
            soundRate = 1;
            break;
        }
        case 7: { //22k
            soundRate = 2;
            break;
        }
        case 4: { //44k
            soundRate = 3;
            break;
        }
        default:
        {
            return val;
        }
    }
    */
    // 0xA0 means this is AAC
    //soundrate << 2  44khz
    // 0x02 means there are 16 bit samples
    val = 0xA0 | (soundRate << 2) | 0x02 | soundType;
    return val;
}

int rtmp_pusher_open(char *url, uint32_t video_width, uint32_t video_height) {
    RTMP_LogSetLevel(RTMP_LOGINFO);
    RTMP_LogSetCallback(rtmp_android_log_callback);
    rtmp = RTMP_Alloc();
    if (rtmp == NULL) {
        LOGE("Couldn't alloc for rmtp!");
        return RTMP_ERROR;
    }

    RTMP_Init(rtmp);
//    rtmp->Link.timeout = 10; //10s超时
//    rtmp->Link.lFlags |= RTMP_LF_LIVE;

    int ret = RTMP_SetupURL(rtmp, url);
    if (!ret) {
        LOGE("Couldn't set the specified url (%s)", url);
        RTMP_Free(rtmp);
        return RTMP_ERROR;
    }

    RTMP_EnableWrite(rtmp);


    ret = RTMP_Connect(rtmp, NULL);
    if (!ret) {
        LOGE("Failed to connect server!");
        RTMP_Free(rtmp);
        return RTMP_ERROR;
    }
    ret = RTMP_ConnectStream(rtmp, 0);

    if (!ret) {
        LOGE("Failed to connect stream!");
        RTMP_Free(rtmp);
        return RTMP_ERROR;
    }

    video_config_ok = false;
    audio_config_ok = false;

    LOGI("Succeed to alloc rmtp!");

    if (RTMP_IsConnected(rtmp)) {

        uint32_t offset = 0;
        char buffer[512];
        char *output = buffer;
        char *outend = buffer + sizeof(buffer);
        char send_buffer[512];

        output = AMF_EncodeString(output, outend, &av_onMetaData);
        *output++ = AMF_ECMA_ARRAY;

        output = AMF_EncodeInt32(output, outend, 5);
        output = AMF_EncodeNamedNumber(output, outend, &av_width, video_width);
        output = AMF_EncodeNamedNumber(output, outend, &av_height, video_height);
        output = AMF_EncodeNamedNumber(output, outend, &av_duration, 0.0);
        output = AMF_EncodeNamedNumber(output, outend, &av_videocodecid, 7);
        output = AMF_EncodeNamedNumber(output, outend, &av_audiocodecid, 10);
        output = AMF_EncodeInt24(output, outend, AMF_OBJECT_END);

        int body_len = output - buffer;
        int output_len = body_len + FLV_TAG_HEAD_LEN + FLV_PRE_TAG_LEN;

        send_buffer[offset++] = 0x12; //tagtype scripte
        send_buffer[offset++] = (uint8_t) (body_len >> 16); //data len
        send_buffer[offset++] = (uint8_t) (body_len >> 8); //data len
        send_buffer[offset++] = (uint8_t) (body_len); //data len
        send_buffer[offset++] = 0; //time stamp
        send_buffer[offset++] = 0; //time stamp
        send_buffer[offset++] = 0; //time stamp
        send_buffer[offset++] = 0; //time stamp
        send_buffer[offset++] = 0x00; //stream id 0
        send_buffer[offset++] = 0x00; //stream id 0
        send_buffer[offset++] = 0x00; //stream id 0

        memcpy(send_buffer + offset, buffer, body_len);

        LOGI("Starting push ...");
        return RTMP_Write(rtmp, send_buffer, output_len);
    }
    return RTMP_ERROR;
}

int rtmp_sender_alloc(char *url) {
    int ret = 0;
    // 分配空间
    rtmp = RTMP_Alloc();
    if (rtmp == NULL) {
        LOGE("Couldn't alloc for rmtp!");
        return RTMP_ERROR;
    }
    // 初始化
    RTMP_Init(rtmp);
    // 设置推流地址
    ret = RTMP_SetupURL(rtmp, url);
    if (!ret) {
        LOGE("Couldn't set the specified url (%s)!", url);
        RTMP_Free(rtmp);
        return RTMP_ERROR;
    }
    // 设置可写入并建立连接
    RTMP_EnableWrite(rtmp);
    LOGI("Succeed to alloc rmtp!");
    video_config_ok = false;
    audio_config_ok = false;

    return RTMP_OK;
}

int rtmp_sender_close() {
    if (rtmp) {
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
        rtmp = NULL;
    }
    LOGI("Succeed to close rtmp!");
    return 0;
}

int rtmp_is_connected() {
    if (rtmp) {
        if (RTMP_IsConnected(rtmp)) {
            return RTMP_CONNECTED_OK;
        }
    }
    return RTMP_CONNECTED_FAILURE;
}

int rtmp_sender_start_publish(uint32_t video_width, uint32_t video_height) {

    if (!RTMP_Connect(rtmp, NULL) || !RTMP_ConnectStream(rtmp, 0)) {
        LOGE("Couldn't connect the rtmp server!");
        return RTMP_ERROR;
    }

    int val = 0;
    uint32_t offset = 0;
    char buffer[512];
    char *output = buffer;
    char *outend = buffer + sizeof(buffer);
    char send_buffer[512];

    output = AMF_EncodeString(output, outend, &av_onMetaData);
    *output++ = AMF_ECMA_ARRAY;

    output = AMF_EncodeInt32(output, outend, 5);
    output = AMF_EncodeNamedNumber(output, outend, &av_width, video_width);
    output = AMF_EncodeNamedNumber(output, outend, &av_height, video_height);
    output = AMF_EncodeNamedNumber(output, outend, &av_duration, 0.0);
    output = AMF_EncodeNamedNumber(output, outend, &av_videocodecid, 7);
    output = AMF_EncodeNamedNumber(output, outend, &av_audiocodecid, 10);
    output = AMF_EncodeInt24(output, outend, AMF_OBJECT_END);

    int body_len = output - buffer;
    int output_len = body_len + FLV_TAG_HEAD_LEN + FLV_PRE_TAG_LEN;

    send_buffer[offset++] = 0x12; //tagtype scripte
    send_buffer[offset++] = (uint8_t) (body_len >> 16); //data len
    send_buffer[offset++] = (uint8_t) (body_len >> 8); //data len
    send_buffer[offset++] = (uint8_t) (body_len); //data len
    send_buffer[offset++] = 0; //time stamp
    send_buffer[offset++] = 0; //time stamp
    send_buffer[offset++] = 0; //time stamp
    send_buffer[offset++] = 0; //time stamp
    send_buffer[offset++] = 0x00; //stream id 0
    send_buffer[offset++] = 0x00; //stream id 0
    send_buffer[offset++] = 0x00; //stream id 0

    memcpy(send_buffer + offset, buffer, body_len);//H264 sequence parameter set
    //no need to set pre_tag_size
    /*
       offset += body_len;
       uint32_t fff = body_len + FLV_TAG_HEAD_LEN;
       output[offset++] = (uint8_t)(fff >> 24); //data len
       output[offset++] = (uint8_t)(fff >> 16); //data len
       output[offset++] = (uint8_t)(fff >> 8); //data len
       output[offset++] = (uint8_t)(fff); //data len
     */
    val = RTMP_Write(rtmp, send_buffer, output_len);

    return val;
}

static uint8_t *get_adts(uint32_t *len, uint8_t **offset, uint8_t *start, uint32_t total)
{
    uint8_t *p  =  *offset;
    uint32_t frame_len_1;
    uint32_t frame_len_2;
    uint32_t frame_len_3;
    uint32_t frame_length;

    if (total < AAC_ADTS_HEADER_SIZE) {
        return NULL;
    }
    if ((p - start) >= total) {
        return NULL;
    }

    if (p[0] != 0xff) {
        return NULL;
    }
    if ((p[1] & 0xf0) != 0xf0) {
        return NULL;
    }
    frame_len_1 = p[3] & 0x03;
    frame_len_2 = p[4];
    frame_len_3 = (p[5] & 0xe0) >> 5;
    frame_length = (frame_len_1 << 11) | (frame_len_2 << 3) | frame_len_3;
    *offset = p + frame_length;
    *len = frame_length;
    return p;
}


// @brief send audio frame
// @param [in] data       : AACAUDIODATA
// @param [in] size       : AACAUDIODATA size
// @param [in] dts_us     : decode timestamp of frame
// @param [in] abs_ts     : indicate whether you'd like to use absolute time stamp
int rtmp_sender_write_audio_frame(unsigned char *data,
                                  int size,
                                  unsigned long dts_us,
                                  unsigned int abs_ts) {

    if (!RTMP_IsConnected(rtmp)){
        return RTMP_ERROR;
    }
    int val = 0;
    uint32_t audio_ts = (uint32_t)dts_us;
    uint8_t * audio_buf = data;
    uint32_t audio_total = size;
    uint8_t *audio_buf_offset = audio_buf;
    uint8_t *audio_frame;
    uint32_t adts_len;
    uint32_t offset;
    uint32_t body_len;
    uint32_t output_len;
    char *output ;

//    if (data == NULL) {
//        return RTMP_ERROR;
//    }
//    while (1){
//        offset = 0;
//        audio_frame = get_adts(&adts_len, &audio_buf_offset, audio_buf, audio_total);
//    }

    //Audio OUTPUT
    offset = 0;

    if (audio_config_ok == false) {
        // first packet is two bytes AudioSpecificConfig

        body_len = 2 + 2; //AudioTagHeader + AudioSpecificConfig
        output_len = body_len + FLV_TAG_HEAD_LEN + FLV_PRE_TAG_LEN;
        output = malloc(output_len);
        // flv tag header
        output[offset++] = 0x08; //tagtype audio
        output[offset++] = (uint8_t) (body_len >> 16); //data len
        output[offset++] = (uint8_t) (body_len >> 8); //data len
        output[offset++] = (uint8_t) (body_len); //data len
        output[offset++] = (uint8_t) (audio_ts >> 16); //time stamp
        output[offset++] = (uint8_t) (audio_ts >> 8); //time stamp
        output[offset++] = (uint8_t) (audio_ts); //time stamp
        output[offset++] = (uint8_t) (audio_ts >> 24); //time stamp
        output[offset++] = abs_ts; //stream id 0
        output[offset++] = 0x00; //stream id 0
        output[offset++] = 0x00; //stream id 0

        //flv AudioTagHeader
        output[offset++] = gen_audio_tag_header(); // sound format aac
        output[offset++] = 0x00; //aac sequence header

        //flv VideoTagBody --AudioSpecificConfig
        //    uint8_t audio_object_type = rtmp_xiecc->config.audio_object_type;
        output[offset++] = data[0]; //(audio_object_type << 3)|(rtmp_xiecc->config.sample_frequency_index >> 1);
        output[offset++] = data[1]; //((rtmp_xiecc->config.sample_frequency_index & 0x01) << 7) \
                           //| (rtmp_xiecc->config.channel_configuration << 3) ;
        //no need to set pre_tag_size

        uint32_t fff = body_len + FLV_TAG_HEAD_LEN;
        output[offset++] = (uint8_t) (fff >> 24); //data len
        output[offset++] = (uint8_t) (fff >> 16); //data len
        output[offset++] = (uint8_t) (fff >> 8); //data len
        output[offset++] = (uint8_t) (fff); //data len

//        if (g_file_handle) {
//            fwrite(output, output_len, 1, g_file_handle);
//        }
        val = RTMP_Write(rtmp, output, output_len);
        free(output);
        //rtmp_xiecc->audio_config_ok = 1;
        audio_config_ok = true;
    } else {

        body_len = 2 +
                   size; //aac header + raw data size // adts_len - AAC_ADTS_HEADER_SIZE; // audito tag header + adts_len - remove adts header + AudioTagHeader
        output_len = body_len + FLV_TAG_HEAD_LEN + FLV_PRE_TAG_LEN;
        output = malloc(output_len);
        // flv tag header
        output[offset++] = 0x08; //tagtype audio
        output[offset++] = (uint8_t) (body_len >> 16); //data len
        output[offset++] = (uint8_t) (body_len >> 8); //data len
        output[offset++] = (uint8_t) (body_len); //data len
        output[offset++] = (uint8_t) (audio_ts >> 16); //time stamp
        output[offset++] = (uint8_t) (audio_ts >> 8); //time stamp
        output[offset++] = (uint8_t) (audio_ts); //time stamp
        output[offset++] = (uint8_t) (audio_ts >> 24); //time stamp
        output[offset++] = abs_ts; //stream id 0
        output[offset++] = 0x00; //stream id 0
        output[offset++] = 0x00; //stream id 0

        //flv AudioTagHeader
        output[offset++] = gen_audio_tag_header(); // sound format aac
        output[offset++] = 0x01; //aac raw data

        //flv VideoTagBody --raw aac data
        memcpy(output + offset, data, size); // data + AAC_ADTS_HEADER_SIZE -> data,
        // (adts_len - AAC_ADTS_HEADER_SIZE) -> size


        //previous tag size
        uint32_t fff = body_len + FLV_TAG_HEAD_LEN;
        offset += size; // (adts_len - AAC_ADTS_HEADER_SIZE);
        output[offset++] = (uint8_t) (fff >> 24); //data len
        output[offset++] = (uint8_t) (fff >> 16); //data len
        output[offset++] = (uint8_t) (fff >> 8); //data len
        output[offset++] = (uint8_t) (fff); //data len

//        if (g_file_handle) {
//            fwrite(output, output_len, 1, g_file_handle);
//        }
        val = RTMP_Write(rtmp, output, output_len);
        free(output);
    }
    return val;
}

static uint32_t find_start_code(uint8_t *buf, uint32_t zeros_in_startcode) {
    uint32_t info;
    uint32_t i;

    info = 1;
    if ((info = (buf[zeros_in_startcode] != 1) ? 0 : 1) == 0)
        return 0;

    for (i = 0; i < zeros_in_startcode; i++)
        if (buf[i] != 0) {
            info = 0;
            break;
        };

    return info;
}

/**
 *
 * len parameter will be filled the length of the nal unit
 *
 * total: total size of the packet
 *
 * return nal unit start byte or NULL if there is no nal unit
 */
static uint8_t *get_nal(uint32_t *len, uint8_t **offset, uint8_t *start, uint32_t total) {
    uint32_t info;
    uint8_t *q;
    uint8_t *p = *offset;
    *len = 0;

    while (1) {
        //p=offset
        // p - start >= total means reach of the end of the packet
        // HINT "-3": Do not access not allowed memory
        if ((p - start) >= total - 3)
            return NULL;

        info = find_start_code(p, 3);
        //if info equals to 1, it means it find the start code
        if (info == 1)
            break;
        p++;
    }
    q = p + 4; // add 4 for first bytes 0 0 0 1
    p = q;
    // find a second start code in the data, there may be second code in data or there may not
    while (1) {
        // HINT "-3": Do not access not allowed memory
        if ((p - start) >= total - 3) {
            p = start + total;
            break;
        }

        info = find_start_code(p, 3);

        if (info == 1)
            break;
        p++;
    }

    // length of the nal unit
    *len = (p - q);
    //offset is the second nal unit start or the end of the data
    *offset = p;
    //return the first nal unit pointer
    return q;
}

int send_key_frame(int nal_len, uint32_t ts, uint32_t abs_ts, uint8_t *nal) {
    int offset = 0;
    int body_len = nal_len + 5 + 4; //flv VideoTagHeader +  NALU length
    int output_len = body_len + FLV_TAG_HEAD_LEN + FLV_PRE_TAG_LEN;
    char *output = malloc(output_len);
    if (!output) {
        LOGE("Memory is not allocated...");
    }
    // flv tag header
    output[offset++] = 0x09; //tagtype video
    output[offset++] = (uint8_t) (body_len >> 16); //data len
    output[offset++] = (uint8_t) (body_len >> 8); //data len
    output[offset++] = (uint8_t) (body_len); //data len
    output[offset++] = (uint8_t) (ts >> 16); //time stamp
    output[offset++] = (uint8_t) (ts >> 8); //time stamp
    output[offset++] = (uint8_t) (ts); //time stamp
    output[offset++] = (uint8_t) (ts >> 24); //time stamp
    output[offset++] = abs_ts; //stream id 0
    output[offset++] = 0x00; //stream id 0
    output[offset++] = 0x00; //stream id 0

    //flv VideoTagHeader
    output[offset++] = 0x17; //key frame, AVC
    output[offset++] = 0x01; //avc NALU unit
    output[offset++] = 0x00; //composit time ??????????
    output[offset++] = 0x00; // composit time
    output[offset++] = 0x00; //composit time

    output[offset++] = (uint8_t) (nal_len >> 24); //nal length
    output[offset++] = (uint8_t) (nal_len >> 16); //nal length
    output[offset++] = (uint8_t) (nal_len >> 8); //nal length
    output[offset++] = (uint8_t) (nal_len); //nal length
    memcpy(output + offset, nal, nal_len);

    //no need set pre_tag_size ,RTMP NO NEED

    offset += nal_len;
    uint32_t fff = body_len + FLV_TAG_HEAD_LEN;
    output[offset++] = (uint8_t) (fff >> 24); //data len
    output[offset++] = (uint8_t) (fff >> 16); //data len
    output[offset++] = (uint8_t) (fff >> 8); //data len
    output[offset++] = (uint8_t) (fff); //data len

//    if (g_file_handle) {
//        fwrite(output, output_len, 1, g_file_handle);
//    }
    int val = RTMP_Write(rtmp, output, output_len);
    //RTMP Send out
    free(output);
    return val;
}

// @brief send video frame, now only H264 supported
// @param [in] rtmp_sender handler
// @param [in] size       : video data size
// @param [in] dts_us     : decode timestamp of frame
// @param [in] abs_ts     : indicate whether you'd like to use absolute time stamp
int rtmp_sender_write_video_frame(unsigned char *data,
                                  int size,
                                  unsigned long dts_us,
                                  unsigned int abs_ts) {
    uint8_t *buf;
    uint8_t *buf_offset;
    int val = 0;
    int total;
    uint32_t ts;
    uint32_t nal_len;
    uint32_t nal_len_n;
    uint8_t *nal;
    uint8_t *nal_n;
    char *output;
    uint32_t offset = 0;
    uint32_t body_len;
    uint32_t output_len;

    buf = data;
    buf_offset = data;
    total = size;
    ts = (uint32_t) dts_us;

    if (data == NULL){
        LOGE("Write video data is NULL!");
        return RTMP_ERROR;
    }

    while (1){
        offset = 0;
        nal = get_nal(&nal_len, &buf_offset, buf, total);

        if (nal == NULL) break;
        if (nal[0] == 0x67) {
            LOGI("Pushing SPS and PPS ...");
            if (video_config_ok == true){
                LOGD("video config is already set");
                //only send video seq set once;
                continue;
            }
            nal_n = get_nal(&nal_len_n, &buf_offset, buf, total); //get pps
            if (nal_n == NULL) {
                LOGE("No Nal after SPS\n");
                return RTMP_ERROR;
            }

            body_len = nal_len + nal_len_n + 16;
            output_len = body_len + FLV_TAG_HEAD_LEN + FLV_PRE_TAG_LEN;
            output = malloc(output_len);
            if (!output) {
                LOGE("Memory is not allocated...");
            }

            // flv tag header
            output[offset++] = 0x09; //tagtype video
            output[offset++] = (uint8_t)(body_len >> 16); //data len
            output[offset++] = (uint8_t)(body_len >> 8); //data len
            output[offset++] = (uint8_t)(body_len); //data len
            output[offset++] = (uint8_t)(ts >> 16); //time stamp
            output[offset++] = (uint8_t)(ts >> 8); //time stamp
            output[offset++] = (uint8_t)(ts); //time stamp
            output[offset++] = (uint8_t)(ts >> 24); //time stamp
            output[offset++] = abs_ts; //stream id 0
            output[offset++] = 0x00; //stream id 0
            output[offset++] = 0x00; //stream id 0

            //flv VideoTagHeader
            output[offset++] = 0x17; //key frame, AVC
            output[offset++] = 0x00; //avc sequence header
            output[offset++] = 0x00; //composit time ??????????
            output[offset++] = 0x00; // composit time
            output[offset++] = 0x00; //composit time

            //flv VideoTagBody --AVCDecoderCOnfigurationRecord
            output[offset++] = 0x01; //configurationversion
            output[offset++] = nal[1]; //avcprofileindication
            output[offset++] = nal[2]; //profilecompatibilty
            output[offset++] = nal[3]; //avclevelindication
            output[offset++] = 0xff; //reserved + lengthsizeminusone
            output[offset++] = 0xe1; //numofsequenceset
            output[offset++] = (uint8_t)(nal_len >> 8); //sequence parameter set length high 8 bits
            output[offset++] = (uint8_t)(nal_len); //sequence parameter set  length low 8 bits
            memcpy(output + offset, nal, nal_len); //H264 sequence parameter set
            offset += nal_len;
            output[offset++] = 0x01; //numofpictureset
            output[offset++] = (uint8_t)(nal_len_n >> 8); //picture parameter set length high 8 bits
            output[offset++] = (uint8_t)(nal_len_n); //picture parameter set length low 8 bits
            memcpy(output + offset, nal_n, nal_len_n); //H264 picture parameter set

            //no need set pre_tag_size ,RTMP NO NEED

            val = RTMP_Write(rtmp, output, output_len);
            //RTMP Send out
            free(output);

            video_config_ok = true;
            continue;
        }

        if ((nal[0] & 0x1f) == 0x05)
        {
            body_len = nal_len + 5 + 4; //flv VideoTagHeader +  NALU length
            output_len = body_len + FLV_TAG_HEAD_LEN + FLV_PRE_TAG_LEN;
            output = malloc(output_len);if (!output) {
                LOGE("Memory is not allocated...");
            }
            // flv tag header
            output[offset++] = 0x09; //tagtype video
            output[offset++] = (uint8_t)(body_len >> 16); //data len
            output[offset++] = (uint8_t)(body_len >> 8); //data len
            output[offset++] = (uint8_t)(body_len); //data len
            output[offset++] = (uint8_t)(ts >> 16); //time stamp
            output[offset++] = (uint8_t)(ts >> 8); //time stamp
            output[offset++] = (uint8_t)(ts); //time stamp
            output[offset++] = (uint8_t)(ts >> 24); //time stamp
            output[offset++] = abs_ts; //stream id 0
            output[offset++] = 0x00; //stream id 0
            output[offset++] = 0x00; //stream id 0

            //flv VideoTagHeader
            output[offset++] = 0x17; //key frame, AVC
            output[offset++] = 0x01; //avc NALU unit
            output[offset++] = 0x00; //composit time ??????????
            output[offset++] = 0x00; // composit time
            output[offset++] = 0x00; //composit time

            output[offset++] = (uint8_t)(nal_len >> 24); //nal length
            output[offset++] = (uint8_t)(nal_len >> 16); //nal length
            output[offset++] = (uint8_t)(nal_len >> 8); //nal length
            output[offset++] = (uint8_t)(nal_len); //nal length
            memcpy(output + offset, nal, nal_len);

            //no need set pre_tag_size ,RTMP NO NEED
            /*
            offset += nal_len;
            uint32_t fff = body_len + FLV_TAG_HEAD_LEN;
            output[offset++] = (uint8_t)(fff >> 24); //data len
            output[offset++] = (uint8_t)(fff >> 16); //data len
            output[offset++] = (uint8_t)(fff >> 8); //data len
            output[offset++] = (uint8_t)(fff); //data len
            */
            val = RTMP_Write(rtmp, output, output_len);
            //RTMP Send out
            free(output);
            continue;
        }

//        if ((nal[0] & 0x1f) == 0x05) // it can be 25,45,65
//        {
//            int result = send_key_frame(nal_len, ts, abs_ts, nal);
//            if (result < 0) {
//                return result;
//            } else {
//                val += result;
//            }
//            continue;
//        }

        if ((nal[0] & 0x1f) == 0x01)
        {
            body_len = nal_len + 5 + 4; //flv VideoTagHeader +  NALU length
            output_len = body_len + FLV_TAG_HEAD_LEN + FLV_PRE_TAG_LEN;
            output = malloc(output_len);if (!output) {
                LOGE("Memory is not allocated...");
            }
            // flv tag header
            output[offset++] = 0x09; //tagtype video
            output[offset++] = (uint8_t)(body_len >> 16); //data len
            output[offset++] = (uint8_t)(body_len >> 8); //data len
            output[offset++] = (uint8_t)(body_len); //data len
            output[offset++] = (uint8_t)(ts >> 16); //time stamp
            output[offset++] = (uint8_t)(ts >> 8); //time stamp
            output[offset++] = (uint8_t)(ts); //time stamp
            output[offset++] = (uint8_t)(ts >> 24); //time stamp
            output[offset++] = abs_ts; //stream id 0
            output[offset++] = 0x00; //stream id 0
            output[offset++] = 0x00; //stream id 0

            //flv VideoTagHeader
            output[offset++] = 0x27; //not key frame, AVC
            output[offset++] = 0x01; //avc NALU unit
            output[offset++] = 0x00; //composit time ??????????
            output[offset++] = 0x00; // composit time
            output[offset++] = 0x00; //composit time

            output[offset++] = (uint8_t)(nal_len >> 24); //nal length
            output[offset++] = (uint8_t)(nal_len >> 16); //nal length
            output[offset++] = (uint8_t)(nal_len >> 8); //nal length
            output[offset++] = (uint8_t)(nal_len); //nal length
            memcpy(output + offset, nal, nal_len);

            //no need set pre_tag_size ,RTMP NO NEED
            /*
            offset += nal_len;
            uint32_t fff = body_len + FLV_TAG_HEAD_LEN;
            output[offset++] = (uint8_t)(fff >> 24); //data len
            output[offset++] = (uint8_t)(fff >> 16); //data len
            output[offset++] = (uint8_t)(fff >> 8); //data len
            output[offset++] = (uint8_t)(fff); //data len
            */
            val = RTMP_Write(rtmp, output, output_len);

            //RTMP Send out
            free(output);
            continue;
        }
    }

    return (val > 0) ? RTMP_OK: RTMP_ERROR;
}
