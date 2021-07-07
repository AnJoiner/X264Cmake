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
    // egl manager
    private EGLManager mEGLManager;
    // egl environment
    private GLInputSurface mGLInputSurface;
    // the object is one of Surface or SurfaceTexture
    private Object mSurface;
    // renderer manager
    private RendererManager mRendererManager;
    // oes texture
    private int mOESTexture = OpenGlUtils.NO_TEXTURE;
    // image stream to openGL es texture.
    private SurfaceTexture mSurfaceTexture;
    // transformed matrix
    private final float[] mMatrix = new float[16];
    // callback data
    private RenderCallback mRenderCallback;

    public void setRenderCallback(RenderCallback renderCallback) {
        mRenderCallback = renderCallback;
    }

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
        mSurfaceTexture.setOnFrameAvailableListener(surfaceTexture -> {
            // render video frame from camera preview.
            requestRenderer();
        });
        // 3. Open Camera and preview, we should attach the SurfaceTexture to camera.
        if (mRenderCallback!=null){
            mRenderCallback.surfaceTextureCreated(mSurfaceTexture);
        }
        // 4. Set image's width and height that from camera preview size.

//        if (mRendererManager != null) {
//            mRendererManager.setImageSize(mImageWidth, mImageHeight);
//        }
    }


    /**
     * Request render video frame.
     */
    public void requestRenderer() {
        if (mSurfaceTexture != null) {
            // 1. update image
            mSurfaceTexture.updateTexImage();
            // 2. get texture coordinate transform matrix
            mSurfaceTexture.getTransformMatrix(mMatrix);

            // 3. draw the frame
            if (mRendererManager != null) {
                mRendererManager.drawFrame(mOESTexture, mMatrix);
            }
            // 4. commit and display
            mGLInputSurface.swapBuffers();
        }
    }

    /**
     * Display area change
     *
     * @param width  宽度
     * @param height 高度
     */
    public void setDisplayChangeSize(int width, int height) {
        if (mRendererManager != null) {
            mRendererManager.setDisplaySize(width, height);
        }
    }


    /**
     * Release EGLSurface 、 EGL 、OES Texture、 RendererManager
     */
    public void release() {
        // Release oes texture
        releaseTexture();
        // Release renderer manager
        releaseRenderer();
        // Release EGL
        releaseEGL();
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

    /**
     * Release Renderer Manager
     */
    private void releaseRenderer() {
        if (mRendererManager != null) {
            mRendererManager.release();
        }
    }

    /**
     * Release egl
     */
    private void releaseEGL() {
        if (mGLInputSurface != null) {
            mGLInputSurface.release();
        }
        if (mEGLManager != null) {
            mEGLManager.release();
        }

        if (mSurface != null) {
            if (mSurface instanceof Surface) {
                ((Surface) mSurface).release();
            } else if (mSurface instanceof SurfaceTexture) {
                ((SurfaceTexture) mSurface).release();
            }
        }
    }

    /**
     * set the image size by user selected.
     */
    public void setImageChangeSize(int width , int height){
        if (mRendererManager!=null){
            mRendererManager.setImageSize(width, height);
        }
    }

    public interface RenderCallback {
        void surfaceTextureCreated(SurfaceTexture surfaceTexture);
    }

}
