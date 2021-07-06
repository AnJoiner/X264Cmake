package com.coder.x264cmake.widgets;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.coder.x264cmake.module.camera.render.GLCameraHandler;
import com.coder.x264cmake.module.camera.render.GLCameraRenderer;
import com.coder.x264cmake.module.camera.render.GLThread;

/**
 * @auther: AnJoiner
 * @datetime: 2021/7/4
 */
public class GLCameraView extends SurfaceView implements SurfaceHolder.Callback {
    // 渲染线程
    private GLThread mGLThread;
    // 相机渲染事件处理
    private GLCameraHandler mCameraHandler;
    // 相机渲染
    private GLCameraRenderer mGLCameraRenderer;

    public GLCameraView(Context context) {
        this(context,null);
    }

    public GLCameraView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public GLCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGLCameraRenderer =  new GLCameraRenderer();
        mGLThread = new GLThread();
        mGLThread.start();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Handler handler = getCameraHandler();
        handler.sendMessage(handler.obtainMessage(GLCameraHandler.MSG_CREATED,holder.getSurface()));
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Handler handler = getCameraHandler();
        handler.sendMessage(handler.obtainMessage(GLCameraHandler.MSG_CHANGED, width, height));
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Handler handler = getCameraHandler();
        handler.sendMessage(handler.obtainMessage(GLCameraHandler.MSG_DESTROYED ,holder.getSurface()));
    }


    public void onDestroy(){
        mGLThread.quit();
    }

    public Handler getCameraHandler() {
        if (mCameraHandler == null) {
            mCameraHandler = new GLCameraHandler(mGLThread.getLooper(), getContext() ,mGLCameraRenderer);
        }
        return mCameraHandler;
    }
}
