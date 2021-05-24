package com.coder.x264cmake.jni;

public class YuvCore {
    static {
        System.loadLibrary("h264-encode");
        System.loadLibrary("yuv");
    }

    public native static void i420ToRGBA(byte[] src, byte[] dst, int width, int height);

    public native static void i420ToNv21(byte[] src, byte[] dst, int width, int height);

    public native static void nv21ToI420(byte[] src, byte[] dst, int width, int height);

    public native static void nv21ToABGR(byte[] src, byte[] dst, int width, int height);

    public native static void nv21ToRGB24(byte[] src, byte[] dst, int width, int height);

    public native static void rgb24ToI420(byte[] src, byte[] dst, int width, int height);

    public native static void rotateI420(byte[] src, byte[] dst, int width, int height, int degree);
}
