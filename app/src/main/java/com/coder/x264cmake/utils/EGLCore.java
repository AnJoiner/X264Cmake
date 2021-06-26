package com.coder.x264cmake.utils;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.SurfaceHolder;


/**
 * @auther: AnJoiner
 * @datetime: 2021/6/26
 */
public class EGLCore {
    // egl context
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    // egl display
    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    // egl config
    private EGLConfig mEGLConfig = null;

    private int mGLVersion = -1;

    public EGLCore() {

    }

    public void initEgl(SurfaceHolder surfaceHolder){
        // 获取egl display
        EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (display == EGL14.EGL_NO_DISPLAY){
            LogUtils.e("Failed to get egl display!");
            return;
        }
        // 初始化 egl display
        int []version = new int[2];
        boolean isInitialized = EGL14.eglInitialize(display,version,0,version,1);
        if (!isInitialized){
            LogUtils.e("Failed to initialize egl display!");
            return;
        }
        // 选择egl配置
        int[] attrs = new int[]  {
                EGL14.EGL_RED_SIZE,8,
                EGL14.EGL_GREEN_SIZE,8,
                EGL14.EGL_BLUE_SIZE,8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 16,
                EGL14.EGL_RENDERABLE_TYPE, mGLVersion == 3 ? EGLExt.EGL_OPENGL_ES3_BIT_KHR : EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT | EGL14.EGL_WINDOW_BIT,
                EGL14.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        boolean isChooseConfig = EGL14.eglChooseConfig(display,attrs,0,configs,
                0,configs.length,numConfigs,0);
        if (!isChooseConfig){
            LogUtils.e("Failed to choose config!");
            return;
        }
        // 创建surface
        EGLSurface window = EGL14.eglCreateWindowSurface(display,configs[0],surfaceHolder,null,0);

        if (window == EGL14.EGL_NO_SURFACE){
            LogUtils.e("Failed to create egl window surface!");
            return;
        }
        int[] ctxAttr = new int[]{ EGL14.EGL_CONTEXT_CLIENT_VERSION,2, EGL14.EGL_NONE  };
        // 创建关联的上下文
        EGLContext eglContext = EGL14.eglCreateContext(display,configs[0],EGL14.EGL_NO_CONTEXT,ctxAttr,0);
        if (eglContext == EGL14.EGL_NO_CONTEXT){
            LogUtils.e("Failed to create egl context!");
            return;
        }

        boolean isMakeCurrent = EGL14.eglMakeCurrent(display,window,window,eglContext);
        if (!isMakeCurrent){
            LogUtils.e("Failed to make current!");
            return;
        }
        LogUtils.i("Succeed to initialize window!");
    }

    public void getError(){
        int errorCode = EGL14.eglGetError();
        switch (errorCode){
            case EGL14.EGL_SUCCESS:
                LogUtils.d("Succeed to ");
                break;
        }
    }


}
