package com.coder.x264cmake.module.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.coder.x264cmake.jni.YuvCore;
import com.coder.x264cmake.utils.LogUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2Loader extends ICameraLoader {

    private static final int PREVIEW_WIDTH = 480;
    private static final int PREVIEW_HEIGHT = 640;

    private Activity mActivity;
    // 默认后置摄像头
    public int cameraFacing = CameraCharacteristics.LENS_FACING_BACK;
    // 相机实例
    private CameraDevice cameraInstance = null;
    // 图像读取器
    private ImageReader imageReader = null;
    // 视图宽高
    private int viewWidth = 0;
    private int viewHeight = 0;
    // 相机管理
    private CameraManager cameraManager;

    private CameraCaptureSession captureSession;

    private byte[] mImageData;

    public Camera2Loader(Activity activity) {
        mActivity = activity;
        cameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
    }

    @Override
    void onResume(int width, int height) {
        viewWidth = width;
        viewHeight = height;
        setUpCamera();
    }

    @Override
    void onPause() {
        releaseCamera();
    }

    @Override
    void switchCamera() {
        if (cameraFacing == CameraCharacteristics.LENS_FACING_BACK) {
            cameraFacing = CameraCharacteristics.LENS_FACING_FRONT;
        } else if (cameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
            cameraFacing = CameraCharacteristics.LENS_FACING_BACK;
        }
        releaseCamera();
        setUpCamera();
    }

    @Override
    int getCameraOrientation() {
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

        String cameraId = getCameraId(cameraFacing);
        if (cameraId == null) {
            return 0;
        }
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            int orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (cameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                return (orientation + degrees) % 360;
            } else {
                return (orientation - degrees) % 360;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    boolean hasMultipleCamera() {
        try {
            return cameraManager.getCameraIdList().length > 1;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    private void setUpCamera() {
        String cameraId = getCameraId(cameraFacing);
        try {
            cameraManager.openCamera(cameraId, new CameraDeviceCallback(), null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        if (cameraInstance != null) {
            cameraInstance.close();
            cameraInstance = null;
        }
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
    }

    /**
     * 获取相机id
     *
     * @param facing 前置或者后置
     * @return cameraID
     */
    private String getCameraId(int facing) {
        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            for (String cameraId : cameraIds) {
                if (cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.LENS_FACING)
                        == facing) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void startCaptureSession() {
        Size size = chooseOptimalSize();
        imageReader = ImageReader.newInstance(size.getWidth(), size.getHeight(), ImageFormat.YUV_420_888, 2);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireNextImage();
                if (image == null) return;
                int ySize = image.getWidth() * image.getHeight();
                int uSize = ySize / 4;
                if (mOnCameraPreCallback != null) {
                    if (mImageData == null) {
                        mImageData = new byte[ySize * 3 / 2];
                    }

                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer yBuffer = planes[0].getBuffer();
                    ByteBuffer uBuffer = planes[1].getBuffer();
                    ByteBuffer vBuffer = planes[2].getBuffer();


                    yBuffer.get(mImageData, 0, ySize);
                    uBuffer.get(mImageData, ySize, ySize / 4);
                    vBuffer.get(mImageData, ySize + uSize, ySize / 4);

                    mOnCameraPreCallback.onCameraPreFrame(mImageData, image.getWidth(), image.getHeight());
                }
                image.close();
            }
        }, null);


        try {
            cameraInstance.createCaptureSession(Arrays.asList(imageReader.getSurface()), new CaptureStateCallback(), null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private Size chooseOptimalSize() {
        if (viewWidth == 0 || viewHeight == 0) {
            return new Size(0, 0);
        }
        String cameraId = getCameraId(cameraFacing);
        if (cameraId == null) {
            return new Size(0, 0);
        }
        Size size = new Size(PREVIEW_WIDTH, PREVIEW_HEIGHT);
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            Size[] outputSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(ImageFormat.YUV_420_888);
            int orientation = getCameraOrientation();
            int maxPreviewWidth = (orientation == 90 || orientation == 270) ? viewHeight : viewWidth;
            int maxPreviewHeight = (orientation == 90 || orientation == 270) ? viewWidth : viewHeight;

            int maxValue = 0;

            for (Size outputSize : outputSizes) {
                if (outputSize.getWidth() < maxPreviewWidth / 2 && outputSize.getHeight() < maxPreviewHeight / 2) {
                    if (outputSize.getWidth() * outputSize.getHeight() > maxValue) {
                        maxValue = outputSize.getWidth() * outputSize.getHeight();
                        size = outputSize;
                    }
                }
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return size;
    }

    private class CameraDeviceCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraInstance = camera;
            startCaptureSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraInstance = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraInstance = null;
        }
    }

    private class CaptureStateCallback extends CameraCaptureSession.StateCallback {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            if (cameraInstance == null) return;
            captureSession = session;
            try {
                CaptureRequest.Builder builder = cameraInstance.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                builder.addTarget(imageReader.getSurface());
                session.setRepeatingRequest(builder.build(), null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            LogUtils.e("Failed to configure capture session.");
        }
    }
}
