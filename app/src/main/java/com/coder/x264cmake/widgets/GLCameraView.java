package com.coder.x264cmake.widgets;

import android.content.Context;
import android.opengl.EGLContext;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.coder.x264cmake.module.camera.egl.EGLManager;
import com.coder.x264cmake.module.camera.egl.GLInputSurface;
import com.coder.x264cmake.module.camera.render.GLThread;

/**
 * @auther: AnJoiner
 * @datetime: 2021/7/4
 */
public class GLCameraView extends SurfaceView implements SurfaceHolder.Callback {

    // egl 渲染管理
    private EGLManager mEGLManager;
    // 渲染环境
    private GLInputSurface mGLInputSurface;
    // 渲染线程
    private GLThread mGLThread;
    // 渲染上下文
    private EGLContext mEGLContext;

    public GLCameraView(Context context) {
        super(context);
    }

    public GLCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GLCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        mEGLManager = new EGLManager();
        mGLInputSurface = new GLInputSurface(mEGLManager,holder.getSurface());
        mEGLContext = mEGLManager.getEGLContext();

        mGLThread = new GLThread(mGLInputSurface);
        mGLThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (mGLInputSurface!=null){
            mGLInputSurface.updateSize(width, height);
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        mGLThread.quitSafely();
        release();
    }

    private void release(){
        if (mGLInputSurface!=null){
            mGLInputSurface.release();
        }
        if (mEGLManager!=null){
            mEGLManager.release();
        }
    }

}
