package com.coder.x264cmake.module.camera;

public abstract class ICameraLoader {

    protected OnCameraPreCallback mOnCameraPreCallback;

    abstract void onResume(int width, int height);

    abstract void onPause();

    abstract void switchCamera();

    abstract int getCameraOrientation();

    abstract boolean hasMultipleCamera();


    public interface OnCameraPreCallback {
        void onCameraPreFrame(byte[] data, int width, int height);
    }

    public void setCameraPreCallback(OnCameraPreCallback onCameraPreCallback) {
        this.mOnCameraPreCallback = onCameraPreCallback;
    }
}
