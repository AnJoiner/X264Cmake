package com.coder.x264cmake.module.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.Surface;
import android.view.WindowManager;

import com.coder.x264cmake.utils.LogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Camera1Loader extends ICameraLoader {

    private Activity mActivity;
    // 默认后置摄像头
    public int cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
    // 相机实例
    public Camera cameraInstance = null;
    // 相机id
    private int cameraId = 0;
    // 是否正在预览
    public boolean isPreviewing = false;

    public Camera1Loader(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onPause() {
        releaseCamera();
    }

    @Override
    public void onResume(int width, int height) {
        setUpCamera();
    }

    @Override
    public void switchCamera() {
        if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        releaseCamera();
        setUpCamera();
    }

    @Override
    public int getCameraOrientation() {
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
    public boolean hasMultipleCamera() {
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
        setPreviewSize(parameters);
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

//        cameraInstance.setDisplayOrientation(getCameraOrientation());
        cameraInstance.startPreview();

        isPreviewing = true;
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

    private void setPreviewSize(Camera.Parameters parameters) {
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> sizes = new ArrayList<>();
        int widthRatio;
        int heightRatio;
        switch (previewType) {
            case CAMERA_PREVIEW_16To9:
                widthRatio = 16;
                heightRatio = 9;
                break;
            case CAMERA_PREVIEW_4To3:
                widthRatio = 4;
                heightRatio = 3;
                break;
            case CAMERA_PREVIEW_1To1:
                widthRatio = 1;
                heightRatio = 1;
                break;
            default:
                return;
        }

        for (Camera.Size previewSize : previewSizes) {
            // 满足设置比例
            if (previewSize.width * heightRatio == previewSize.height * widthRatio) {
                if ((previewSize.width >= widthRange[0] && previewSize.width <= widthRange[1]) &&
                        (previewSize.height >= heightRange[0] && previewSize.height <= heightRange[1])) {
                    sizes.add(previewSize);
                }
            }
        }

        if (sizes.size() == 0) return;
        // 设置预览大小
        parameters.setPreviewSize(sizes.get(0).width, sizes.get(0).height);

    }

    public void releaseCamera() {
        if (cameraInstance != null) {
            cameraInstance.setPreviewCallback(null);
            cameraInstance.stopPreview();
            cameraInstance.release();
            cameraInstance = null;

            isPreviewing = false;
        }
    }
}
