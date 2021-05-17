//
// Created by c2yu on 2021/5/8.
//

#ifndef X264CMAKE_X264_ENCODE_H
#define X264CMAKE_X264_ENCODE_H

#endif //X264CMAKE_X264_ENCODE_H

enum {
    X264_ENC_OK = 0,
    X264_ENC_FAIL = -1
};

enum {
    X264_ENC_UNINITIALIZED,
    X264_ENC_INITIALIZED
};

enum {
    X264_ENC_ENCODING,
    X264_ENC_STOPPED
};


int x264_enc_init(int width, int height, const char *x264_file_path, int yuv_format);

int x264_enc_data(char *buffer, int size);

void x264_enc_release();
