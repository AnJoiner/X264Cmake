package com.coder.x264cmake.module.camera.render;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.coder.x264cmake.module.camera.egl.GLInputSurface;

/**
 * @auther: AnJoiner
 * @datetime: 2021/7/4
 */
public class GLThread extends Thread {
    private static final String TAG = "GLThread";
    private static final String THREAD_NAME = "camera.egl.GLThread";

    private boolean doneStarting;
    private boolean startedSuccessfully;
    // 同步锁
    private final Object startLock = new Object();

    // EGL Surface
    protected GLInputSurface mGLInputSurface;
    // 消息处理
    protected Handler mHandler;
    protected Looper mLooper;

    public GLThread(@NonNull GLInputSurface GLInputSurface) {
        setName(THREAD_NAME);
        mGLInputSurface = GLInputSurface;
    }

    @Override
    public void run() {
        try {
            Looper.prepare();
            mLooper = Looper.myLooper();
            mHandler = new Handler(mLooper);

            Log.d(TAG, String.format("Starting GL thread %s", getName()));
            mGLInputSurface.makeCurrent();
            startedSuccessfully = true;
        } finally {
            // Always stop waitUntilReady here, even if we got an exception.
            // Otherwise the main thread may be stuck waiting.
            synchronized (startLock) {
                doneStarting = true;
                startLock.notify(); // signal waitUntilReady()
            }
        }

        try {
            Looper.loop();
        } finally {
            mLooper = null;
            mHandler.removeCallbacksAndMessages(null);
            Log.d(TAG, String.format("Stopping GL thread %s", getName()));
        }
    }

    /** Terminates the thread, after processing all pending messages. */
    public boolean quitSafely() {
        if (mLooper == null) {
            return false;
        }
        mLooper.quitSafely();
        return true;
    }


    public boolean waitUntilReady() throws InterruptedException {
        synchronized (startLock) {
            while (!doneStarting) {
                startLock.wait();
            }
        }
        return startedSuccessfully;
    }
}
