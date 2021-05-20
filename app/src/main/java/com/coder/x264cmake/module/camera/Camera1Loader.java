package com.coder.x264cmake.module.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.Surface;
import android.view.WindowManager;

import com.coder.x264cmake.utils.LogUtils;

import java.util.List;


public class Camera1Loader extends ICameraLoader {

    private Activity mActivity;
    // 默认后置摄像头
    public int cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
    // 相机实例
    public Camera cameraInstance = null;
    // 相机id
    private int cameraId = 0;

    public Camera1Loader(Activity activity) {
        mActivity = activity;
    }

    @Override
    void onPause() {
        releaseCamera();
    }

    @Override
    void onResume(int width, int height) {
        setUpCamera();
    }

    @Override
    void switchCamera() {
        if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        releaseCamera();
        setUpCamera();
    }

    @Override
    int getCameraOrientation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        final int rotation = ((WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
        }
        int result;
        if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else { // back-facing
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }

        return result;
    }

    @Override
    boolean hasMultipleCamera() {
        return Camera.getNumberOfCameras() > 1;
    }



    private void setUpCamera() {
        cameraId = getCurrentCameraId();
        try {
            cameraInstance = getCameraInstance(cameraId);
        } catch (IllegalAccessError e) {
            LogUtils.e("Camera not found");
        }
        // 获取相机参数
        Camera.Parameters parameters = cameraInstance.getParameters();
        // 设置对焦
        setFocus(parameters);
        // 设置预览格式为nv21
        parameters.setPreviewFormat(ImageFormat.NV21);

        cameraInstance.setParameters(parameters);

        cameraInstance.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                Camera.Size size = camera.getParameters().getPreviewSize();
                if (mOnCameraPreCallback != null) {
                    mOnCameraPreCallback.onCameraPreFrame(data, size.width, size.height);
                }
            }
        });

        cameraInstance.startPreview();
    }


    /**
     * 获取当前相机Id
     *
     * @return CameraId
     */
    private int getCurrentCameraId() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == cameraFacing) {
                return i;
            }
        }
        return 0;
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public Camera getCameraInstance(int cameraId) {
        Camera c = null;
        try {
            c = Camera.open(cameraId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    /**
     * 设置对焦
     *
     * @param parameters 相机参数
     */
    private void setFocus(Camera.Parameters parameters) {
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
    }


    public void releaseCamera() {
        if (cameraInstance != null) {
            cameraInstance.setPreviewCallback(null);
            cameraInstance.stopPreview();
            cameraInstance.release();
            cameraInstance = null;
        }
    }
}
