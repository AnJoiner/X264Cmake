package com.coder.x264cmake.module.camera.loader;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.Surface;
import android.view.WindowManager;
import android.view.WindowMetrics;

import static android.hardware.Camera.Parameters.FOCUS_MODE_AUTO;

import com.coder.x264cmake.BaseApplication;
import com.coder.x264cmake.utils.DensityUtils;
import com.coder.x264cmake.utils.LogUtils;

import java.util.List;

public class CameraLoader {
    // 默认后置摄像头
    public int cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
    public Camera cameraInstance = null;
    private int cameraId = 0;

    private int videoWidth;
    private int videoHeight;

    private OnCameraPreCallback onCameraPreCallback;

    public void setOnCameraPreCallback(OnCameraPreCallback onCameraPreCallback) {
        this.onCameraPreCallback = onCameraPreCallback;
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

    public void setUpCamera() {
        cameraId = getCurrentCameraId();
        try {
            cameraInstance = getCameraInstance(cameraId);
        } catch (IllegalAccessError e) {
            LogUtils.e("Camera not found");
        }
        Camera.Parameters parameters = cameraInstance.getParameters();
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFlashMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        // 设置预览格式为nv21
        parameters.setPreviewFormat(ImageFormat.NV21);
        setFlash(parameters);
        // 自动对焦
        setFocus(parameters);
        setPreviewSize(parameters);
        setPictureSize(parameters);
        // 将预览数据进行回调
        if (onCameraPreCallback != null) {
            cameraInstance.setPreviewCallback((data, camera) -> onCameraPreCallback.onCameraPreFrame(data, videoWidth, videoHeight));
        }
        cameraInstance.setParameters(parameters);
        cameraInstance.setDisplayOrientation(getRotation());
    }

    public void releaseCamera() {
        if (cameraInstance != null) {
            cameraInstance.setPreviewCallback(null);
            cameraInstance.stopPreview();
            cameraInstance.release();
            cameraInstance = null;
        }
    }

    /**
     * Check if this device has a camera
     */
    public boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    public void switchCamera() {
        if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        releaseCamera();
        setUpCamera();
    }


    public int getRotation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        final int rotation = ((WindowManager) BaseApplication.getInstance().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
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
//            return (90 + degrees) % 360;
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else { // back-facing
//            return (90 - degrees) % 360;
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }

        return result;
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


    private void setFlash(Camera.Parameters parameters) {
        List<String> flashModes = parameters.getSupportedFlashModes();
        //  && cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK
        if (flashModes != null && flashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        }
    }

    private void setFocus(Camera.Parameters parameters) {
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (focusModes.contains(FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(FOCUS_MODE_AUTO);
        }
    }

    public void setPreviewSize(Camera.Parameters parameters) {
        List<Camera.Size> supportSizes = parameters.getSupportedPreviewSizes();
        int surfaceWidth = DensityUtils.width();
        int surfaceHeight = DensityUtils.height();

        Camera.Size size = getCloselyPreSize(surfaceWidth, surfaceHeight, supportSizes);
        videoWidth = size.width;
        videoHeight = size.height;
        LogUtils.d("CameraLoader===>>> width:"+videoWidth+", height:"+videoHeight);
        parameters.setPreviewSize(size.width, size.height);
    }


    protected Camera.Size getCloselyPreSize(int surfaceWidth, int surfaceHeight,
                                            List<Camera.Size> preSizeList) {
        int ReqTmpWidth;
        int ReqTmpHeight;
        // 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
        int rotation = getRotation();
        if (rotation == 90 || rotation == 270) {
            ReqTmpWidth = surfaceHeight;
            ReqTmpHeight = surfaceWidth;
        } else {
            ReqTmpWidth = surfaceWidth;
            ReqTmpHeight = surfaceHeight;
        }
        //先查找preview中是否存在与surfaceview相同宽高的尺寸
        for (Camera.Size size : preSizeList) {
            if ((size.width == ReqTmpWidth) && (size.height == ReqTmpHeight)) {
                return size;
            }
        }
        // 得到与传入的宽高比最接近的size
        float reqRatio = ((float) ReqTmpWidth) / ReqTmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : preSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }
        return retSize;
    }

    /**
     * save picture size
     */
    private void setPictureSize(Camera.Parameters parameters) {
        List<Camera.Size> mPictureSizes = parameters.getSupportedPictureSizes();
        Camera.Size previewSize = parameters.getPreviewSize();
        Camera.Size biggestSize = null;
        Camera.Size fitSize = null;// 优先选预览界面的尺寸
        LogUtils.d("##### preview size: " + previewSize.width + ", height:" + previewSize.height);

        float scaleSize = 0;
        if (null != previewSize) {
            scaleSize = previewSize.width / (float) previewSize.height;
        }

        for (int i = 0; i < mPictureSizes.size(); i++) {
            Camera.Size picture = mPictureSizes.get(i);
            LogUtils.d("###### SupportedPictureSizes: width=" + picture.width + ", height="
                    + picture.height);
            if (null == biggestSize) {
                biggestSize = picture;
            } else if (picture.width > biggestSize.width && picture.height > biggestSize.height) {
                biggestSize = picture;
            }

            if (scaleSize > 0 && picture.width > previewSize.width && picture.height > previewSize.height) {
                float currentScale = picture.width / (float) picture.height;
                if (scaleSize == currentScale) {
                    if (null == fitSize) {
                        fitSize = picture;
                    } else if (picture.width > fitSize.width && picture.height > fitSize.height) {
                        fitSize = picture;
                    }
                }
            }
        }

        if (null == fitSize) {
            fitSize = biggestSize;
        }

        LogUtils.d("##### fit size: " + fitSize.width + ", height:" + fitSize.height);
        parameters.setPictureSize(fitSize.width, fitSize.height);
    }

    private static int[] getCloselyPreviewFps(int expectedFps, List<int[]> fpsRanges) {
        expectedFps *= 1000;
        int[] closestRange = fpsRanges.get(0);
        int measure = Math.abs(closestRange[0] - expectedFps) + Math.abs(closestRange[1] - expectedFps);
        for (int[] range : fpsRanges) {
            if (range[0] <= expectedFps && range[1] >= expectedFps) {
                int curMeasure = Math.abs(range[0] - expectedFps) + Math.abs(range[1] - expectedFps);
                if (curMeasure < measure) {
                    closestRange = range;
                    measure = curMeasure;
                }
            }
        }
        return closestRange;
    }


    public interface OnCameraPreCallback {
        void onCameraPreFrame(byte[] data, int width, int height);
    }


}