package com.coder.x264cmake.module.camera.render;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

public class GLCameraHandler extends Handler {

    public static final int MSG_CREATED = 0x01;     // 创建
    public static final int MSG_CHANGED = 0x02;     // 显示变化
    public static final int MSG_DESTROYED = 0x03;   // 销毁
    // context的弱引用
    private WeakReference<Context> mContextWeakReference;
    // renderer的弱引用
    private WeakReference<GLCameraRenderer> mRendererWeakReference;

    public GLCameraHandler(@NonNull Looper looper,Context context, GLCameraRenderer glCameraRenderer) {
        super(looper);
        mContextWeakReference = new WeakReference<>(context);
        mRendererWeakReference = new WeakReference<>(glCameraRenderer);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        switch (msg.what){
            case MSG_CREATED:
                Object surface = msg.obj;
                mRendererWeakReference.get().init(mContextWeakReference.get(), surface);
                break;
            case MSG_CHANGED:
                mRendererWeakReference.get().displayChanged(msg.arg1,msg.arg2);
                break;
            case MSG_DESTROYED:
                mRendererWeakReference.get().release();
                break;
        }
    }
}
