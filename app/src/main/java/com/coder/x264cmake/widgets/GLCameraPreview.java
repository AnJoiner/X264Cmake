package com.coder.x264cmake.widgets;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.coder.x264cmake.R;
import com.coder.x264cmake.module.camera.Camera1Loader;
import com.coder.x264cmake.module.camera.Camera2Loader;
import com.coder.x264cmake.module.camera.ICameraLoader;
import com.coder.x264cmake.module.camera.anotations.CameraPreviewType;
import com.coder.x264cmake.module.camera.render.GLImageRenderer;
import com.coder.x264cmake.utils.LogUtils;

import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

/**
 * @auther: AnJoiner
 * @datetime: 2021/6/19
 */
public class GLCameraPreview extends FrameLayout {

    public static final int CAMERA_1 = 0x01;
    public static final int CAMERA_2 = 0x02;
    public static final int CAMERA_X = 0x03;

    // 上下文
    private Context mContext;
    // 包含EGL的SurfaceView
    private GLSurfaceView mGLSurfaceView;
    // 预览格式
    private int mPreviewType;
    // 渲染管理
    private GLImageRenderer mGLImageRenderer;
    // 相机加载器
    private ICameraLoader mCameraLoader;
    // 相机类型
    private int mCameraType = CAMERA_1;
    // 预览宽高
    private int mDisplayWidth;
    private int mDisplayHeight;

    private float mWidthRatio;
    private float mHeightRatio;

    public GLCameraPreview(Context context) {
        this(context, null);
    }

    public GLCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mWidthRatio != 0 && mHeightRatio != 0) {
            // 需根据用户选择设置视图大小
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);

            float ratio = mWidthRatio / mHeightRatio;

            if (width / ratio < height) {
                mDisplayWidth = width;
                mDisplayHeight = Math.round(width / ratio);
            } else {
                mDisplayHeight = height;
                mDisplayWidth = Math.round(height * ratio);
            }

            int newWidthSpec = MeasureSpec.makeMeasureSpec(mDisplayWidth, MeasureSpec.EXACTLY);
            int newHeightSpec = MeasureSpec.makeMeasureSpec(mDisplayHeight, MeasureSpec.EXACTLY);

            super.onMeasure(newWidthSpec, newHeightSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void init() {
        initCamera();
        initRenderer();
        initSurfaceView();
    }

    /**
     * Checks if OpenGL ES 2.0 is supported on the current device.
     *
     * @param context the context
     * @return true, if successful
     */
    private boolean supportsOpenGLES2(final Context context) {
        final ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }

    /**
     * 初始化相机
     */
    private void initCamera() {
        if (mCameraType == CAMERA_1) {
            mCameraLoader = new Camera1Loader(mContext);
        } else if (mCameraType == CAMERA_2) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCameraLoader = new Camera2Loader(mContext);
            } else {
                mCameraLoader = new Camera1Loader(mContext);
            }
        } else {
            LogUtils.e("Not support CameraX");
        }
        if (mCameraLoader != null) {
            mCameraLoader.setCameraPreCallback(new ICameraLoader.OnCameraPreCallback() {
                @Override
                public void onCameraPreSize(int width, int height) {
                    int cameraOrientation = mCameraLoader.getOrientation();
                    int imageWidth = (cameraOrientation == 90 || cameraOrientation == 270) ? height : width;
                    int imageHeight = (cameraOrientation == 90 || cameraOrientation == 270) ? width : height;

                    if (mGLImageRenderer!=null) mGLImageRenderer.setImageSize(imageWidth, imageHeight);
                }

                @Override
                public void onCameraPreFrame(byte[] data, int width, int height) {

                }
            });
        }
        generateRatio();
    }

    /**
     * 初始化渲染器
     */
    private void initRenderer() {
        mGLSurfaceView = new GLSurfaceView(mContext);
        mGLImageRenderer = new GLImageRenderer(mGLSurfaceView);
        mGLImageRenderer.setOnImageRendererCallback(new GLImageRenderer.OnImageRendererCallback() {
            @Override
            public void onSurfaceCreated(SurfaceTexture surfaceTexture) {
                if (mCameraLoader != null) {
                    // 开始预览
                    mCameraLoader.mSurfaceTexture = surfaceTexture;
                    mCameraLoader.resume(mDisplayWidth, mDisplayHeight);
                }
            }
        });
    }

    /**
     * 初始化视图
     */
    private void initSurfaceView() {
        if (supportsOpenGLES2(mContext)) {
            mGLSurfaceView.setEGLContextClientVersion(2);
            mGLSurfaceView.setRenderer(mGLImageRenderer);
            mGLSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);
            mGLSurfaceView.setId(R.id.camera_surface);
            addView(mGLSurfaceView);
        } else {
            LogUtils.e("Not support OpenGL ES 2.0");
        }
    }

    /**
     * 生成默认比例
     */
    private void generateRatio() {
        if (mCameraLoader != null) {
            if (mWidthRatio == 0 || mHeightRatio == 0) {
                // 相机预览角度
                int cameraOrientation = mCameraLoader.getOrientation();
                // 获取默认宽高比
                mWidthRatio = (cameraOrientation == 90 || cameraOrientation == 270) ? mCameraLoader.mHeightRatio : mCameraLoader.mWidthRatio;
                mHeightRatio = (cameraOrientation == 90 || cameraOrientation == 270) ? mCameraLoader.mWidthRatio : mCameraLoader.mHeightRatio;
            } else {
                mCameraLoader.setPreviewType(mPreviewType);
                mCameraLoader.mWidthRatio = mWidthRatio;
                mCameraLoader.mHeightRatio = mHeightRatio;
            }
        }
    }

    /**
     * 设置相机的预览格式
     *
     * @param previewType 预览格式
     */
    public void setPreviewType(@CameraPreviewType int previewType) {
        mPreviewType = previewType;

        if (mCameraLoader != null) {
            mCameraLoader.setPreviewType(previewType);
            mCameraLoader.release();
            mCameraLoader.resume(mDisplayWidth, mDisplayHeight);
        }
    }

    public void setCameraType(int cameraType) {
        mCameraType = cameraType;
        if (mCameraLoader != null) {
            mCameraLoader.release();
            mCameraLoader = null;
        }

        initCamera();
    }

    public void onPause() {
        if (mCameraLoader != null) {
            mCameraLoader.pause();
        }
        if (mGLSurfaceView!=null){
            mGLSurfaceView.onPause();
        }
    }

    public void onResume() {
        if (mCameraLoader != null) {
            mCameraLoader.resume(mDisplayWidth, mDisplayHeight);
        }
        if (mGLSurfaceView!=null){
            mGLSurfaceView.onResume();
        }
    }

    public void onRelease() {
        if (mCameraLoader != null) {
            mCameraLoader.release();
            mCameraLoader = null;
        }

        if (mGLImageRenderer != null) {
            mGLImageRenderer.release();
        }
    }

}
