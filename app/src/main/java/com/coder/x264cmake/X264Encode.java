package com.coder.x264cmake;

/**
 * @author: AnJoiner
 * @datetime: 21-4-10
 */
public class X264Encode {
    static {
//        System.loadLibrary("h264");
        System.loadLibrary("h264-encode");
    }

    public native int encode(int width,int height,String yuvPath,String h264Path,@YUVFormat int format);
}
