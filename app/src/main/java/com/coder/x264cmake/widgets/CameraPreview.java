package com.coder.x264cmake.widgets;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.coder.x264cmake.module.camera.loader.CameraLoader;
import com.coder.x264cmake.utils.LogUtils;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private CameraLoader mCameraLoader;


    public CameraPreview(Context context, CameraLoader cameraLoader) {
        super(context);
        mCameraLoader = cameraLoader;

        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        LogUtils.d("CameraPreview ===>>> surfaceCreated");
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            if (mCameraLoader != null) {
                mCameraLoader.setUpCamera();
                if (mCameraLoader.cameraInstance != null) {
                    mCameraLoader.cameraInstance.setPreviewDisplay(holder);
                    mCameraLoader.cameraInstance.startPreview();
                }
            }
        } catch (IOException e) {
            LogUtils.e("Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        LogUtils.d("CameraPreview ===>>> surfaceChanged");
        if (mHolder.getSurface() == null) {
            return;
        }
        try {
            if (mCameraLoader != null) {
                mCameraLoader.releaseCamera();
                mCameraLoader.setUpCamera();

                if (mCameraLoader.cameraInstance != null) {
                    mCameraLoader.cameraInstance.setPreviewDisplay(mHolder);
                    mCameraLoader.cameraInstance.startPreview();
                }
            }
        } catch (Exception e) {
            LogUtils.e("Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        LogUtils.d("CameraPreview ===>>> surfaceDestroyed");
        if (mCameraLoader != null) {
            mCameraLoader.releaseCamera();
        }
    }
}