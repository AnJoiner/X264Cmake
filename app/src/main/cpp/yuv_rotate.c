//
// Created by c2yu on 2021/5/13.
//

#include "libyuv.h"
#include "yuv_rotate.h"

void rotate_i420(char *src, char *dst, int width, int height, int degree) {
    int src_y_size = width * height;
    int src_u_size = src_y_size >> 2;
    char *src_y = src;
    char *src_u = src + src_y_size;
    char *src_v = src + src_y_size + src_u_size;

    int dst_y_size = width * height;
    int dst_u_size = dst_y_size >> 2;
    char *dst_y = dst;
    char *dst_u = dst + dst_y_size;
    char *dst_v = dst + dst_y_size + dst_u_size;

    int mode;
    switch (degree) {
        case 0 : mode = kRotate0; break;
        case 90: mode = kRotate90; break;
        case 180: mode = kRotate180; break;
        case 270: mode = kRotate270; break;
        default: mode = kRotate0;
    }


    I420Rotate((unsigned char *)src_y, width,
               (unsigned char *)src_u, width >> 1,
               (unsigned char *)src_v, width >> 1,
               (unsigned char *)dst_y,height,
               (unsigned char *)dst_u,height>>1,
               (unsigned char *)dst_v,height >> 1,
               width,height,
               mode
    );
}