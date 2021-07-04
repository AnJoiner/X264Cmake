package com.coder.x264cmake.module.camera.egl;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class GLInputSurface implements InputSurfaceInterface {
    // egl 管理工具
    private EGLManager mEGLManager;
    private Object mSurface;
    // 是否释放Surface
    private boolean isReleaseSurface;
    // egl surface
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
    // egl 宽高
    private int mWidth, mHeight;

    public GLInputSurface(EGLManager eglManager, Object surface) {
        this(eglManager,surface,false);
    }

    public GLInputSurface(EGLManager eglManager, Object surface, boolean isReleaseSurface) {

        if (surface == null) {
            throw new NullPointerException();
        }
        if (surface instanceof Surface){
            mSurface = surface;
        }else if (surface instanceof SurfaceHolder){
            mSurface = ((SurfaceHolder) surface).getSurface();
        }else if (surface instanceof SurfaceView){
            mSurface = ((SurfaceView) surface).getHolder().getSurface();
        }else if (surface instanceof SurfaceTexture){
            mSurface = surface;
        }else {
            throw new RuntimeException("invalid surface: " + surface);
        }

        this.mEGLManager = eglManager;
        this.isReleaseSurface = isReleaseSurface;

        if (mEGLManager == null){
            mEGLManager = new EGLManager();
        }
        if (mEGLSurface!= EGL14.EGL_NO_SURFACE){
            throw new IllegalStateException("surface already created");
        }
        mEGLSurface = mEGLManager.createWindowSurface(mSurface);

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
            mEGLManager.releaseEGLSurface(mEGLSurface);
            mEGLSurface = mEGLManager.createWindowSurface(mSurface);
            mWidth = getWidth();
            mHeight = getHeight();
        }
    }

    @Override
    public void release() {
        mEGLManager.releaseEGLSurface(mEGLSurface);
        releaseSurface();
        mEGLSurface = EGL14.EGL_NO_SURFACE;
        mSurface = null;
    }


    private void releaseSurface(){
        // 释放surface
        if (isReleaseSurface && mSurface!=null){
            if (mSurface instanceof Surface){
                ((Surface) mSurface).release();
            }else if (mSurface instanceof SurfaceHolder){
                ((SurfaceHolder) mSurface).getSurface().release();
            }else if (mSurface instanceof SurfaceView){
                ((SurfaceView) mSurface).getHolder().getSurface().release();
            }else if (mSurface instanceof SurfaceTexture){
                ((SurfaceTexture) mSurface).release();
            }
        }
    }
}
