package com.coder.x264cmake.jni;

import com.coder.x264cmake.annotation.YUVFormat;

/**
 * @author: AnJoiner
 * @datetime: 21-4-10
 */
public class X264Encode {
    static {
        System.loadLibrary("h264-encode");
    }
    public native int initQueue(byte[] bytes);
    public native int init(int width,int height, String h264Path, @YUVFormat int format);
    public native int encodeData(byte[] data);
    public native void release();
    public native int encode(int width,int height,String yuvPath,String h264Path,@YUVFormat int format);
}
