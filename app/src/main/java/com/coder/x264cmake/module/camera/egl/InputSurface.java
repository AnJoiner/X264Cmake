package com.coder.x264cmake.module.camera.egl;

import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.view.Surface;

public class InputSurface implements InputSurfaceInterface {
    // egl 管理工具
    private EGLManager mEGLManager;
    private Surface mSurface;
    // 是否释放Surface
    private boolean isReleaseSurface;
    // egl surface
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;

    private int mWidth = -1, mHeight = -1;

    public InputSurface(EGLManager eglManager, Surface surface) {
        this(eglManager,surface,true);
    }

    public InputSurface(EGLManager eglManager, Surface surface, boolean isReleaseSurface) {
        if (surface == null) {
            throw new NullPointerException();
        }

        mEGLManager = eglManager;
        mSurface = surface;
        this.isReleaseSurface = isReleaseSurface;

        if (mEGLManager == null){
            mEGLManager = new EGLManager();
        }
        if (mEGLSurface!= EGL14.EGL_NO_SURFACE){
            throw new IllegalStateException("surface already created");
        }
        mEGLSurface = mEGLManager.createWindowSurface(surface);

        mWidth = getWidth();
        mHeight = getHeight();
    }


    /**
     * Makes our EGL context and surface current.
     */
    @Override
    public void makeCurrent() {
        mEGLManager.makeCurrent(mEGLSurface);
    }

    @Override
    public void makeUnCurrent() {
        mEGLManager.makeCurrent(mEGLSurface);
    }

    /**
     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
     */
    @Override
    public boolean swapBuffers() {
        return mEGLManager.swapBuffers(mEGLSurface);
    }

    /**
     * Queries the surface's width.
     */
    @Override
    public int getWidth() {
        return mEGLManager.getWidth(mEGLSurface);
    }

    /**
     * Queries the surface's height.
     */
    @Override
    public int getHeight() {
        return mEGLManager.getHeight(mEGLSurface);
    }

    /**
     * Returns the Surface that the MediaCodec receives buffers from.
     */
    @Override
    public void setPresentationTime(long nsecs) {
        mEGLManager.setPresentationTime(nsecs,mEGLSurface);
    }


    @Override
    public void updateSize(int width, int height) {
        if (mWidth!= width || mHeight != height){
            mEGLManager.releaseSurface(mEGLSurface);
            mEGLSurface = mEGLManager.createWindowSurface(mSurface);
            mWidth = getWidth();
            mHeight = getHeight();
        }
    }

    @Override
    public void release() {
        mEGLManager.releaseSurface(mEGLSurface);
        mEGLManager.release();
        // 释放surface
        if (isReleaseSurface && mSurface!=null){
            mSurface.release();
        }
        mEGLSurface = EGL14.EGL_NO_SURFACE;
        mSurface = null;
    }
}
