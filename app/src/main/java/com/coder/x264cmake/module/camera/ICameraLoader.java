package com.coder.x264cmake.module.camera;

import android.graphics.SurfaceTexture;

public abstract class ICameraLoader {

    public static final int CAMERA_PREVIEW_16To9 = 0x01;
    public static final int CAMERA_PREVIEW_4To3 = 0x02;
    public static final int CAMERA_PREVIEW_1To1 = 0x03;
    // 预览格式，默认16:9
    public int mPreviewType = CAMERA_PREVIEW_16To9;
    // 宽高比
    public float mWidthRatio = 16;
    public float mHeightRatio = 9;

    public int mImageWidth;
    public int mImageHeight;

    // 纹理
    public SurfaceTexture mSurfaceTexture;

    protected OnCameraPreCallback mOnCameraPreCallback;

    public abstract void resume(int width, int height);

    public abstract void pause();

    public abstract void switchCamera();

    public abstract void release();

    public abstract int getOrientation();

    public abstract boolean hasMultipleCamera();

    public void setPreviewType(int previewType) {
        this.mPreviewType = previewType;
        switch (mPreviewType) {
            case CAMERA_PREVIEW_16To9:
                mWidthRatio = 16;
                mHeightRatio = 9;
                break;
            case CAMERA_PREVIEW_4To3:
                mWidthRatio = 4;
                mHeightRatio = 3;
                break;
            case CAMERA_PREVIEW_1To1:
                mWidthRatio = 1;
                mHeightRatio = 1;
                break;
            default:
                return;
        }
    }

    public void setCameraPreCallback(OnCameraPreCallback onCameraPreCallback) {
        this.mOnCameraPreCallback = onCameraPreCallback;
    }


    public interface OnCameraPreCallback {
        void onCameraPreSize(int width, int height);
        void onCameraPreFrame(byte[] data, int width, int height);
    }

}
