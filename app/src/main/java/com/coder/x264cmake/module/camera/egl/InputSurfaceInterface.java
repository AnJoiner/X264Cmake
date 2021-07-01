package com.coder.x264cmake.module.camera.egl;

import android.media.MediaCodec;

public interface InputSurfaceInterface {
    void makeCurrent();
    void makeUnCurrent();
    boolean swapBuffers();
    int getWidth();
    int getHeight();
    void setPresentationTime(long nsecs);
    void updateSize(int width, int height);
    void release();
}
