//
// Created by c2yu on 2021/5/13.
//

#include "libyuv.h"
#include "yuv_convert.h"

void nv21_to_i420(char *src,  char *dst, int width, int height) {
    int src_y_size = width * height;
     char *src_y = src;
     char *src_vu = src + src_y_size;

    int dst_y_size = width * height;
    int dst_u_size = dst_y_size >> 2;
     char *dst_y = dst;
     char *dst_u = dst + dst_y_size;
     char *dst_v = dst + dst_y_size + dst_u_size;


    NV21ToI420((unsigned char *)src_y, width,
               (unsigned char *)src_vu, width,
               (unsigned char *)dst_y, width,
               (unsigned char *)dst_u, width >> 1,
               (unsigned char *)dst_v, width >> 1,
               width, height);
}

void i420_to_nv21( char *src,  char *dst, int width, int height) {
    /**
     * const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_y,
               int dst_stride_y,
               uint8_t* dst_vu,
               int dst_stride_vu,
               int width,
               int height
     */
    int src_y_size = width * height;
    int src_u_size = src_y_size >> 2;
     char *src_y = src;
     char *src_u = src + src_y_size;
     char *src_v = src + src_y_size + src_u_size;

    int dst_y_size = width * height;
     char *dst_y = dst;
     char *dst_vu = dst + dst_y_size;

    I420ToNV21((unsigned char *)src_y, width,
               (unsigned char *)src_u, width >> 1,
               (unsigned char *)src_v, width >> 1,
               (unsigned char *)dst_y, width,
               (unsigned char *)dst_vu, width,
               width,height);
}