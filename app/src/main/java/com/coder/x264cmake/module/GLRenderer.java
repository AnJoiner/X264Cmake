package com.coder.x264cmake.module;


import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.coder.x264cmake.module.filter.GLImageFilter;
import com.coder.x264cmake.utils.LogUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class GLRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private GLSurfaceView gLSurfaceView;
    // 滤镜效果
    private GLImageFilter gLImageFilter;

    private int width = 720;
    private int height = 1280;

    public SurfaceTexture surfaceTexture;
    private OnSurfaceListener mOnSurfaceListener;


    public GLRenderer(GLSurfaceView gLSurfaceView) {
        this.gLSurfaceView = gLSurfaceView;
    }

    public void setImageFilter(GLImageFilter glImageFilter) {
        this.gLImageFilter = glImageFilter;
    }

    public void setOnSurfaceListener(OnSurfaceListener onSurfaceListener) {
        mOnSurfaceListener = onSurfaceListener;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        LogUtils.e("onSurfaceCreated === >>>>" + gLSurfaceView.getWidth() + "x" + gLSurfaceView.getHeight());
        // 建立纹理
        surfaceTexture = gLImageFilter.createSurfaceTexture();
        // 设置纹理监听
        surfaceTexture.setOnFrameAvailableListener(this);
        // 回调创建的纹理与camera关联
        if (mOnSurfaceListener != null) {
            mOnSurfaceListener.onSurfaceCreated(surfaceTexture);
        }
        // 建立滤镜数据
        gLImageFilter.setUp();
        gLImageFilter.createFrameBuffer(gLSurfaceView.getWidth(), gLSurfaceView.getHeight());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (surfaceTexture != null) {
            surfaceTexture.updateTexImage();
            gLImageFilter.drawTexture();
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        gLSurfaceView.requestRender();
    }

    public void onDestroy() {
        gLImageFilter.onDestroy();
    }

    public interface OnSurfaceListener {
        void onSurfaceCreated(SurfaceTexture surfaceTexture);
    }
}
