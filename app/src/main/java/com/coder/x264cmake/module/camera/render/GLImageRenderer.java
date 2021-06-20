package com.coder.x264cmake.module.camera.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;

import com.coder.x264cmake.module.camera.Camera1Loader;
import com.coder.x264cmake.module.camera.ICameraLoader;
import com.coder.x264cmake.utils.LogUtils;
import com.coder.x264cmake.utils.OpenGlUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLImageRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    // 显示区域
    private GLSurfaceView mGLSurfaceView;
    // 上下文
    private Context mContext;
    // 渲染管理器
    private RendererManager mRendererManager;
    // 相机加载器
    private Camera1Loader mCameraLoader;
    // 纹理对象
    private int[] textures;

    private SurfaceTexture mSurfaceTexture;
    // 矩阵
    private final float[] mMatrix = new float[16];

    public GLImageRenderer(GLSurfaceView GLSurfaceView) {
        mGLSurfaceView = GLSurfaceView;
        if (mGLSurfaceView != null) {
            mContext = mGLSurfaceView.getContext();
        }
        // 创建相机
        resumeCamera();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (mGLSurfaceView != null) mGLSurfaceView.requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 创建渲染管理
        mRendererManager = new RendererManager(mContext);
        // 创建OES纹理
        createTexture();
        // 设置监听
        mSurfaceTexture.setOnFrameAvailableListener(this);
        // 开启预览
        if (mCameraLoader != null) {
            mCameraLoader.mSurfaceTexture = mSurfaceTexture;
            mCameraLoader.setUpCamera();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // surface变更
        if (mRendererManager != null) {
            mRendererManager.setDisplaySize(width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        drawFrame();
    }

    /**
     * 创建纹理
     */
    public void createTexture() {
        if (textures != null && mSurfaceTexture != null) {
            releaseTexture();
        }
        textures = new int[1];
        OpenGlUtils.generateTextureOES(textures);
        mSurfaceTexture = new SurfaceTexture(textures[0]);
    }

    /**
     * 渲染
     */
    public void drawFrame() {
        // 更新视图和转换矩阵
        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mMatrix);
        }
        // 绘制
        if (mRendererManager != null) {
            mRendererManager.drawFrame(textures[0], mMatrix);
        }
    }

    /**
     * 销毁纹理
     */
    public void release() {
        // 释放纹理
        releaseTexture();
        // 释放相机
        releaseCamera();
        // 释放renderer
        releaseRenderer();
    }

    /**
     * 释放纹理
     */
    public void releaseTexture() {
        if (textures != null) {
            OpenGlUtils.deleteTexture(textures);
            textures = null;
        }
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
    }

    /**
     * 释放相机
     */
    public void releaseCamera(){
        if (mCameraLoader != null) {
            mCameraLoader.release();
        }
    }

    public void resumeCamera(){
        mCameraLoader = new Camera1Loader(mContext);
        mCameraLoader.setCameraPreCallback(new ICameraLoader.OnCameraPreCallback() {
            @Override
            public void onCameraPreSize(int width, int height) {
                // 预览大小
                mRendererManager.setImageSize(height, width);
                LogUtils.e("onCameraPreSize==>>> width:" + width + ", height:" + height);
            }

            @Override
            public void onCameraPreFrame(byte[] data, int width, int height) {
                // 帧数据返回
            }
        });
    }

    /**
     * 释放相机renderer
     */
    public void releaseRenderer() {

        if (mRendererManager != null) {
            mRendererManager.release();
        }
    }
}
