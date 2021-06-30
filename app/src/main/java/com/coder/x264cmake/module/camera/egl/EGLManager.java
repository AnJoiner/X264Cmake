package com.coder.x264cmake.module.camera.egl;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.coder.x264cmake.module.camera.anotations.EGLFlag;



/**
 * @auther: AnJoiner
 * @datetime: 2021/6/26
 */
public class EGLManager {
    private static final String TAG = "EGLManager";

    public static final int FLAG_RECORDABLE = 0x01;

    // egl context
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    // egl display
    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    // egl config
    private EGLConfig mEGLConfig = null;
    // openGL es版本，默认2.0
    private int mGLVersion = 2;

    // EGL针对Android使用的扩展配置
    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    public EGLManager(@EGLFlag int flag) {
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("EGL display has been created!");
        }
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("Failed to get EGL display!");
        }
        int[] version = new int[2];
        boolean isInitialized = EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1);
        if (!isInitialized) {
            throw new RuntimeException("Failed to initialize EGL display!");
        }

        mEGLConfig = newConfig(mGLVersion, flag);
        // Configure context for OpenGL ES 2.0 / 3.0 .
        if (mEGLConfig != null) {
            int[] attrib_list = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, mGLVersion,
                    EGL14.EGL_NONE
            };
            mEGLContext = EGL14.eglCreateContext(mEGLDisplay, mEGLConfig, EGL14.EGL_NO_CONTEXT, attrib_list, 0);
            checkEglError("eglCreateContext");
        }
        if (mEGLContext == null) {
            throw new RuntimeException("null context");
        }
        Log.i(TAG, "Succeed to create egl context!");
    }


    /**
     * 创建一个window surface 并与接收到的surface关联
     */
    public EGLSurface createWindowSurface(Object surface) {
        if (!(surface instanceof Surface
                || surface instanceof SurfaceTexture
                || surface instanceof SurfaceHolder
                || surface instanceof SurfaceView)) {
            throw new RuntimeException("invalid surface: " + surface);
        }
        // Create a window surface, and attach it to the Surface we received.
        int[] attrib_list = {EGL14.EGL_NONE};
        EGLSurface eglSurface =
                EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface, attrib_list, 0);
        checkEglError("eglCreateWindowSurface");

        if (eglSurface == null) {
            throw new RuntimeException("surface was null");
        }
        return eglSurface;
    }


    public EGLConfig newConfig(int glVersion, int flag) {
        boolean isRecordable = flag == FLAG_RECORDABLE;
        // 选择egl配置
        int[] attrs = new int[]{
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, glVersion == 3 ? EGLExt.EGL_OPENGL_ES3_BIT_KHR : EGL14.EGL_OPENGL_ES2_BIT,
                isRecordable ? EGL_RECORDABLE_ANDROID : EGL14.EGL_SURFACE_TYPE, isRecordable ? 1 : (EGL14.EGL_PBUFFER_BIT | EGL14.EGL_WINDOW_BIT),
                EGL14.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];

        boolean isChooseConfig = EGL14.eglChooseConfig(mEGLDisplay, attrs, 0, configs,
                0, configs.length, numConfigs, 0);

        if (!isChooseConfig) {
            throw new RuntimeException("unable to find RGB888+recordable ES" + glVersion + " EGL config");
        }

        return configs[0];
    }

    /**
     * Discard all resources held by this class, notably the EGL context.  Also releases the
     * Surface that was passed to our constructor.
     */
    public void release() {
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(mEGLDisplay);
        }
        mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        mEGLContext = EGL14.EGL_NO_CONTEXT;
        mEGLConfig = null;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                // We're limited here -- finalizers don't run on the thread that holds
                // the EGL state, so if a surface or context is still current on another
                // thread we can't fully release it here.  Exceptions thrown from here
                // are quietly discarded.  Complain in the log file.
                Log.w(TAG, "WARNING: EglCore was not explicitly released -- state may be leaked");
                release();
            }
        } finally {
            super.finalize();
        }
    }

    /**
     * Destroys the specified surface.  Note the EGLSurface won't actually be destroyed if it's
     * still current in a context.
     */
    public void releaseSurface(EGLSurface eglSurface) {
        EGL14.eglDestroySurface(mEGLDisplay, eglSurface);
    }


    /**
     * Makes our EGL context and surface current.
     */
    public void makeCurrent(EGLSurface eglSurface) {
        if (!EGL14.eglMakeCurrent(mEGLDisplay, eglSurface, eglSurface, mEGLContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    public void makeUnCurrent() {
        if (!EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    /**
     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
     */
    public boolean swapBuffers(EGLSurface eglSurface) {
        return EGL14.eglSwapBuffers(mEGLDisplay, eglSurface);
    }

    /**
     * Queries the surface's width.
     */
    public int getWidth(EGLSurface eglSurface) {
        int[] value = new int[1];
        EGL14.eglQuerySurface(mEGLDisplay, eglSurface, EGL14.EGL_WIDTH, value, 0);
        return value[0];
    }

    /**
     * Queries the surface's height.
     */
    public int getHeight(EGLSurface eglSurface) {
        int[] value = new int[1];
        EGL14.eglQuerySurface(mEGLDisplay, eglSurface, EGL14.EGL_HEIGHT, value, 0);
        return value[0];
    }

    /**
     * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
     */
    public void setPresentationTime(long nsecs, EGLSurface eglSurface) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, eglSurface, nsecs);
    }


    /**
     * Checks for EGL errors.
     */
    private void checkEglError(String msg) {
        int error;
        if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }

}
