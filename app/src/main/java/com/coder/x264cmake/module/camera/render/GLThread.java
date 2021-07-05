package com.coder.x264cmake.module.camera.render;

import android.os.Looper;
import android.util.Log;

/**
 * @auther: AnJoiner
 * @datetime: 2021/7/4
 */
public class GLThread extends Thread {
    private static final String TAG = "GLThread";
    private static final String THREAD_NAME = "camera.egl.GLThread";
    protected Looper mLooper;

    public GLThread() {
        setName(THREAD_NAME);
    }

    @Override
    public void run() {
        Looper.prepare();
        Log.d(TAG, String.format("Starting GL thread %s", getName()));
        synchronized (this) {
            mLooper = Looper.myLooper();
            notifyAll();
        }
        Looper.loop();
        Log.d(TAG, String.format("Stopping GL thread %s", getName()));
    }

    public Looper getLooper() {
        if (!isAlive()) {
            return null;
        }

        // If the thread has been started, wait until the looper has been created.
        synchronized (this) {
            while (isAlive() && mLooper == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return mLooper;
    }


    public boolean quit() {
        Looper looper = getLooper();
        if (looper != null) {
            looper.quit();
            return true;
        }
        return false;
    }

    /** Terminates the thread, after processing all pending messages. */
    public boolean quitSafely() {
        Looper looper = getLooper();
        if (looper != null) {
            looper.quitSafely();
            return true;
        }
        return false;
    }

}
