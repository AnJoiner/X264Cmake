package com.coder.x264cmake.jni;

public class YuvCore {
    static {
        System.loadLibrary("h264-encode");
        System.loadLibrary("yuv");
    }

    public native void nv21ToI420(byte[] src, byte[] dst, int width, int height);

    public native void i420ToNv21(byte[] src, byte[] dst, int width, int height);

    public native void rotateI420(byte[] src, byte[] dst, int width, int height, int degree);
}
