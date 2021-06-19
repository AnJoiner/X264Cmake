package com.coder.x264cmake.module.camera;

public abstract class ICameraLoader {

    public static final int CAMERA_PREVIEW_16To9 = 0x01;
    public static final int CAMERA_PREVIEW_4To3 = 0x02;
    public static final int CAMERA_PREVIEW_1To1 = 0x03;
    // 预览格式，默认16:9
    protected int previewType = CAMERA_PREVIEW_16To9;

    protected int[] widthRange = {720, 1280};
    protected int[] heightRange = {540, 720};

    protected OnCameraPreCallback mOnCameraPreCallback;

    public abstract void onResume(int width, int height);

    public abstract void onPause();

    public abstract void switchCamera();

    public abstract int getCameraOrientation();

    public abstract boolean hasMultipleCamera();


    public void setPreviewType(int previewType) {
        this.previewType = previewType;
    }

    public void setCameraPreCallback(OnCameraPreCallback onCameraPreCallback) {
        this.mOnCameraPreCallback = onCameraPreCallback;
    }


    public interface OnCameraPreCallback {
        void onCameraPreSize(int width, int height);
        void onCameraPreFrame(byte[] data, int width, int height);
    }

}
