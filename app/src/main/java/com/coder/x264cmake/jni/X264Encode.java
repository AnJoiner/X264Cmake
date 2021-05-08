package com.coder.x264cmake.jni;

import com.coder.x264cmake.annotation.YUVFormat;

/**
 * @author: AnJoiner
 * @datetime: 21-4-10
 */
public class X264Encode {
    static {
        System.loadLibrary("h264-encode");
        System.loadLibrary("rtmp");
    }
    public native int init_x264(int width,int height, String h264Path, @YUVFormat int format);
    public native int encode_x264_data(byte[] data);
    public native void release_x264();
    public native int encode_x264(int width,int height,String yuvPath,String h264Path,@YUVFormat int format);


    public native int init_aac(int sample_rate, int channel, int bitrate, String aac_path);
    public native int encode_aac_data(byte[] data);
    public native void release_aac();
}
