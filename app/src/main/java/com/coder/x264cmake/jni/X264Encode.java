package com.coder.x264cmake.jni;

import com.coder.x264cmake.annotation.YUVFormat;

/**
 * @author: AnJoiner
 * @datetime: 21-4-10
 */
public class X264Encode {
    private OnEncodeListener mOnEncodeListener;

    public void setOnEncodeListener(OnEncodeListener onEncodeListener) {
        mOnEncodeListener = onEncodeListener;
    }

    static {
        System.loadLibrary("h264-encode");
        System.loadLibrary("rtmp");
        System.loadLibrary("faac");
    }
    public native int init_x264(int width,int height, String h264Path, @YUVFormat int format);
    public native int encode_x264_data(byte[] data);
    public native void release_x264();
    public native int encode_x264(int width,int height,String yuvPath,String h264Path,@YUVFormat int format);


    public native int init_aac(int sample_rate, int channel, int bitrate, String aac_path);
    public native int encode_aac_data(byte[] data);
    public native void release_aac();

    public void onEncodeH264(byte[] bytes, int size){
        if (mOnEncodeListener!=null){
            mOnEncodeListener.onEncodeH264(bytes, size);
        }
    }

    public void onEncodeAAC(byte[] bytes, int size){
        if (mOnEncodeListener!=null){
            mOnEncodeListener.onEncodeAAC(bytes, size);
        }
    }

    public interface OnEncodeListener{
        void onEncodeH264(byte[] bytes, int size);
        void onEncodeAAC(byte[] bytes, int size);
    }
}
