//
// Created by c2yu on 2021/5/13.
//

#include "libyuv.h"
#include "yuv_convert.h"

void i420_to_rgba(char *src, char *dst, int width, int height) {
    int src_y_size = width * height;
    int src_u_size = src_y_size >> 2;
    char *src_y = src;
    char *src_u = src + src_y_size;
    char *src_v = src + src_y_size + src_u_size;

    char *dst_rgba = dst;

    I420ToRGBA((unsigned char *) src_y, width,
               (unsigned char *) src_u, width >> 1,
               (unsigned char *) src_v, width >> 1,
               (unsigned char *) dst_rgba, width * 4,
               width, height);

}


void i420_to_nv21(char *src, char *dst, int width, int height) {
    int src_y_size = width * height;
    int src_u_size = src_y_size >> 2;
    char *src_y = src;
    char *src_u = src + src_y_size;
    char *src_v = src + src_y_size + src_u_size;

    int dst_y_size = width * height;
    char *dst_y = dst;
    char *dst_vu = dst + dst_y_size;

    I420ToNV21((unsigned char *) src_y, width,
               (unsigned char *) src_u, width >> 1,
               (unsigned char *) src_v, width >> 1,
               (unsigned char *) dst_y, width,
               (unsigned char *) dst_vu, width,
               width, height);
}


void nv21_to_abgr(char *src, char *dst, int width, int height) {
    int src_y_size = width * height;
    char *src_y = src;
    char *src_vu = src + src_y_size;

    char *dst_abgr = dst;

    NV21ToABGR((unsigned char *) src_y, width,
               (unsigned char *) src_vu, width,
               (unsigned char *) dst_abgr, width * 4,
               width, height
    );
}

void nv21_to_rgb24(char *src, char *dst, int width, int height) {

    int src_y_size = width * height;
    char *src_y = src;
    char *src_vu = src + src_y_size;

    char *dst_rgb24 = dst;

    NV21ToRGB24((unsigned char *) src_y, width,
                (unsigned char *) src_vu, width,
                (unsigned char *) dst_rgb24, width * 3,
                width,height);
}

void nv21_to_i420(char *src, char *dst, int width, int height) {
    int src_y_size = width * height;
    char *src_y = src;
    char *src_vu = src + src_y_size;

    int dst_y_size = width * height;
    int dst_u_size = dst_y_size >> 2;
    char *dst_y = dst;
    char *dst_u = dst + dst_y_size;
    char *dst_v = dst + dst_y_size + dst_u_size;


    NV21ToI420((unsigned char *) src_y, width,
               (unsigned char *) src_vu, width,
               (unsigned char *) dst_y, width,
               (unsigned char *) dst_u, width >> 1,
               (unsigned char *) dst_v, width >> 1,
               width, height);
}


void rgb24_to_i420(char *src, char *dst, int width, int height){
    char * src_rgb24 = src;

    int dst_y_size = width * height;
    int dst_u_size = dst_y_size >> 2;
    char *dst_y = dst;
    char *dst_u = dst + dst_y_size;
    char *dst_v = dst + dst_y_size + dst_u_size;


    RGB24ToI420((unsigned char *)src_rgb24, width *3,
                (unsigned char *) dst_y, width,
                (unsigned char *) dst_u, width >> 1,
                (unsigned char *) dst_v, width >> 1,
                width, height);
}