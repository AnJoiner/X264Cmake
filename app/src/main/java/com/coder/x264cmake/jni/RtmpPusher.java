package com.coder.x264cmake.jni;

public class RtmpPusher {
    static {
        System.loadLibrary("h264-encode");
        System.loadLibrary("rtmp");
        System.loadLibrary("faac");
    }


    public native int rtmp_pusher_open(String url, int width, int height);

    public native int rtmp_pusher_close();

    public native int rtmp_pusher_is_connected();

    public native int rtmp_pusher_push_video(byte[] bytes, int size, long timestamp);

    public native int rtmp_pusher_push_audio(byte[] bytes, int size, long timestamp);
}
