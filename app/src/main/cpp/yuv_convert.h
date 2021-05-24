//
// Created by c2yu on 2021/5/13.
//

#ifndef X264CMAKE_YUV_CONVERT_H
#define X264CMAKE_YUV_CONVERT_H

#endif //X264CMAKE_YUV_CONVERT_H


void i420_to_rgba(char *src, char *dst, int width, int height);
/**
 * i420 convert to nv21
 */
void i420_to_nv21( char *src,  char *dst, int width, int height);


void nv21_to_abgr(char *src, char *dst, int width, int height);

void nv21_to_rgb24(char *src, char *dst, int width, int height);

/**
 * nv21 convert to i420
 */
void nv21_to_i420( char *src,  char *dst, int width, int height);


void rgb24_to_i420(char *src, char *dst, int width, int height);