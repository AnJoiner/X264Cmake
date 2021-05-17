//
// Created by c2yu on 2021/5/10.
//

#ifndef X264CMAKE_RTMP_CORE_H
#define X264CMAKE_RTMP_CORE_H

#endif //X264CMAKE_RTMP_CORE_H

enum {
    RTMP_ERROR = -1,
    RTMP_OK = 0
};

enum {
    RTMP_CONNECTED_FAILURE = 0,
    RTMP_CONNECTED_OK = 1
};


enum {
    RTMP_PUSHING,
    RTMP_STOPPED
};

int rtmp_pusher_open(char* url,unsigned int video_width, unsigned int video_height);

int rtmp_sender_alloc(char *url);

int rtmp_sender_close();

int rtmp_is_connected();

int rtmp_sender_start_publish(unsigned int video_width, unsigned int video_height);

int rtmp_sender_write_audio_frame(unsigned char *data,
                                  int size,
                                  unsigned long dts_us,
                                  unsigned int abs_ts);


int rtmp_sender_write_video_frame(unsigned char *data,
                                  int size,
                                  unsigned long dts_us,
                                  unsigned int abs_ts);

