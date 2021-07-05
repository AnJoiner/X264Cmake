package com.coder.x264cmake.module.camera.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.coder.x264cmake.module.camera.egl.EGLManager;
import com.coder.x264cmake.module.camera.egl.GLInputSurface;
import com.coder.x264cmake.utils.OpenGlUtils;

/**
 * @auther: AnJoiner
 * @datetime: 2021/7/4
 */
public class GLCameraRenderer {
    // egl 渲染管理
    private EGLManager mEGLManager;
    // 渲染环境
    private GLInputSurface mGLInputSurface;
    // surface只有两种 Surface、SurfaceTexture
    private Object mSurface;
    // 渲染管理器
    private RendererManager mRendererManager;

    // 纹理对象
//    private int[] textures;
    private int mOESTexture = OpenGlUtils.NO_TEXTURE;
    // image stream to openGL es texture.
    private SurfaceTexture mSurfaceTexture;

    private void parseSurface(Object surface) {
        if (surface == null) {
            throw new NullPointerException();
        }
        if (surface instanceof Surface) {
            mSurface = surface;
        } else if (surface instanceof SurfaceHolder) {
            mSurface = ((SurfaceHolder) surface).getSurface();
        } else if (surface instanceof SurfaceView) {
            mSurface = ((SurfaceView) surface).getHolder().getSurface();
        } else if (surface instanceof SurfaceTexture) {
            mSurface = surface;
        } else {
            throw new RuntimeException("invalid surface: " + surface);
        }
    }

    public void init(Context context, Object surface) {
        parseSurface(surface);

        mEGLManager = new EGLManager();
        mGLInputSurface = new GLInputSurface(mEGLManager, surface);
        // Make egl context to current surface
        mGLInputSurface.makeCurrent();

        // EGLSurface has been created

        // 1. Create renderer manager
        mRendererManager = new RendererManager(context);
        // 2. Create oes texture
        createTexture();

        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                // render video frame from camera preview.
                requestRenderer();
            }
        });
        // 3. Open Camera and preview

        // 4. Set image's width and height

//        if (mRendererManager != null) {
//            mRendererManager.setImageSize(mImageWidth, mImageHeight);
//        }
    }


    /**
     * Request render video frame.
     */
    public void requestRenderer() {

    }

    /**
     * Display area change
     *
     * @param width  宽度
     * @param height 高度
     */
    public void displayChanged(int width, int height) {
        if (mRendererManager != null) {
            mRendererManager.setDisplaySize(width, height);
        }
    }


    /**
     * Release EGLSurface and EGL
     */
    public void release() {
        if (mGLInputSurface != null) {
            mGLInputSurface.release();
        }
        if (mEGLManager != null) {
            mEGLManager.release();
        }
    }


    /**
     * Create oes texture
     */
    private void createTexture() {
        if (mOESTexture != OpenGlUtils.NO_TEXTURE && mSurfaceTexture != null) {
            releaseTexture();
        }
        int[] textures = new int[1];
        OpenGlUtils.generateTextureOES(textures);
        mOESTexture = textures[0];
        mSurfaceTexture = new SurfaceTexture(mOESTexture);
    }


    /**
     * Release oes texture
     */
    private void releaseTexture() {
        if (mOESTexture != OpenGlUtils.NO_TEXTURE) {
            OpenGlUtils.deleteTexture(new int[]{mOESTexture});
            mOESTexture = OpenGlUtils.NO_TEXTURE;
        }
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
    }

}
