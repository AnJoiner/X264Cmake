package com.coder.x264cmake.module.camera;

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
    // 上下文
    private final Context mContext;
    // 默认后置摄像头
    public int cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
    // 相机实例
    public Camera cameraInstance = null;
    // 相机id
    private int cameraId = 0;
    // 是否正在预览
    public boolean isPreviewing = false;

    public Camera1Loader(Context context) {
        mContext = context;
    }

    @Override
    public void pause() {
        release();
    }

    @Override
    public void resume(int width, int height) {
        setUpCamera();
    }

    @Override
    public void switchCamera() {
        if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        release();
        setUpCamera();
    }

    @Override
    public int getOrientation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        final int rotation = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
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


    public void setUpCamera() {
        cameraId = getCurrentCameraId();
        try {
            cameraInstance = getCameraInstance(cameraId);
        } catch (IllegalAccessError e) {
            LogUtils.e("Camera not found");
        }
        // 获取相机参数
        Camera.Parameters parameters = cameraInstance.getParameters();
        // 设置闪光
        setFlash(parameters);
        // 设置对焦
        setFocus(parameters);
        // 设置预览大小
        setPreviewSize(parameters);
        // 设置预览格式为nv21
        parameters.setPreviewFormat(ImageFormat.NV21);
        // 设置图片大小
        setPictureSize(parameters);
        // 设置配置参数
        cameraInstance.setParameters(parameters);
        // 设置数据返回
        cameraInstance.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (mOnCameraPreCallback != null && isPreviewing) {
                    mOnCameraPreCallback.onCameraPreFrame(data, mImageWidth, mImageHeight);
                }
            }
        });
        // 设置预览方向
        cameraInstance.setDisplayOrientation(getOrientation());
        try {
            if (mSurfaceTexture!=null)
            cameraInstance.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void setFlash(Camera.Parameters parameters) {
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        }
    }

    /**
     * 设置对焦
     *
     * @param parameters 相机参数
     */
//    private void setFocus(Camera.Parameters parameters) {
//        List<String> focusModes = parameters.getSupportedFocusModes();
//        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//        }
//    }
    private void setFocus(Camera.Parameters parameters) {
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        } else {
            parameters.setFocusMode(focusModes.get(0));
        }
    }

//    @Override
//    public void setPreviewType(int previewType) {
//        super.setPreviewType(previewType);
//        release();
//        setUpCamera();
//    }

    private void setPreviewSize(Camera.Parameters parameters) {
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> sizes = new ArrayList<>();

        for (Camera.Size previewSize : previewSizes) {
            // 满足设置比例
            if (previewSize.width * mHeightRatio == previewSize.height * mWidthRatio) {
                sizes.add(previewSize);
            }
        }

        if (sizes.size() == 0) return;
        // 设置预览大小
        Camera.Size  size = sizes.get(0);
//        if (sizes.size() < 3){
//            size = sizes.get(0);
//        }else {
//            // 多个满足时取中间值
//            int index = (sizes.size() - 1)/2;
//            size = sizes.get(index);
//        }

        if (mOnCameraPreCallback != null) {
            mImageWidth = size.width;
            mImageHeight = size.height;
            mOnCameraPreCallback.onCameraPreSize(size.width,size.height);
        }
        parameters.setPreviewSize(size.width, size.height);
    }

    /**
     * 设置拍照大小
     */
    private void setPictureSize(Camera.Parameters parameters) {
        List<Camera.Size> mPictureSizes = parameters.getSupportedPictureSizes();
        Camera.Size previewSize = parameters.getPreviewSize();
        Camera.Size biggestSize = null;
        Camera.Size fitSize = null;// 优先选预览界面的尺寸

        float scaleSize = 0;
        if (null != previewSize) {
            scaleSize = previewSize.width / (float) previewSize.height;
        }

        for (int i = 0; i < mPictureSizes.size(); i++) {
            Camera.Size picture = mPictureSizes.get(i);
//            LogUtils.d("###### SupportedPictureSizes: width=" + picture.width + ", height="
//                    + picture.height);
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

        parameters.setPictureSize(fitSize.width, fitSize.height);
    }

    @Override
    public void release() {
        if (cameraInstance != null) {
            cameraInstance.setPreviewCallback(null);
            cameraInstance.stopPreview();
            cameraInstance.release();
            cameraInstance = null;

            isPreviewing = false;
        }
    }
}
