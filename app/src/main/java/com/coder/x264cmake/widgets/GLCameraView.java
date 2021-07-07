package com.coder.x264cmake.widgets;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.coder.x264cmake.module.camera.Camera1Loader;
import com.coder.x264cmake.module.camera.Camera2Loader;
import com.coder.x264cmake.module.camera.ICameraLoader;
import com.coder.x264cmake.module.camera.render.GLCameraHandler;
import com.coder.x264cmake.module.camera.render.GLCameraRenderer;
import com.coder.x264cmake.module.camera.render.GLThread;
import com.coder.x264cmake.utils.LogUtils;

import static com.coder.x264cmake.module.camera.ICameraLoader.CAMERA_1;
import static com.coder.x264cmake.module.camera.ICameraLoader.CAMERA_2;

/**
 * @auther: AnJoiner
 * @datetime: 2021/7/4
 */
public class GLCameraView extends SurfaceView implements SurfaceHolder.Callback, GLCameraRenderer.RenderCallback {
    // 渲染线程
    private GLThread mGLThread;
    // 相机渲染事件处理
    private GLCameraHandler mCameraHandler;
    // 相机渲染
    private GLCameraRenderer mGLCameraRenderer;
    // 相机加载器
    private ICameraLoader mCameraLoader;
    // 相机类型
    private int mCameraType = ICameraLoader.CAMERA_1;
    // 预览格式
    private int mPreviewType;
    // 预览宽高
    private int mDisplayWidth;
    private int mDisplayHeight;

    private float mWidthRatio;
    private float mHeightRatio;

    public GLCameraView(Context context) {
        this(context,null);
    }

    public GLCameraView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public GLCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initGLThread();
        initCamera();
        initCameraRender();
    }

    private void initCameraRender(){
        mGLCameraRenderer =  new GLCameraRenderer();
        mGLCameraRenderer.setRenderCallback(this);
    }

    private void initGLThread(){
        mGLThread = new GLThread();
        mGLThread.start();
    }

    /**
     * initialized camera
     */
    private void initCamera() {
        if (mCameraType == CAMERA_1) {
            mCameraLoader = new Camera1Loader(getContext());
        } else if (mCameraType == CAMERA_2) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCameraLoader = new Camera2Loader(getContext());
            } else {
                mCameraLoader = new Camera1Loader(getContext());
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

                    Handler handler = getCameraHandler();
                    handler.sendMessage(handler.obtainMessage(GLCameraHandler.MSG_IMAGE_CHANGED, imageWidth, imageHeight));
//                    if (mGLCameraRenderer != null)
//                        mGLCameraRenderer.setImageChangeSize(imageWidth, imageHeight);
                }

                @Override
                public void onCameraPreFrame(byte[] data, int width, int height) {

                }
            });
        }
        generateRatio();
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

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Handler handler = getCameraHandler();
        handler.sendMessage(handler.obtainMessage(GLCameraHandler.MSG_CREATED,holder.getSurface()));
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Handler handler = getCameraHandler();
        handler.sendMessage(handler.obtainMessage(GLCameraHandler.MSG_DISPLAY_CHANGED, width, height));
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Handler handler = getCameraHandler();
        handler.sendMessage(handler.obtainMessage(GLCameraHandler.MSG_DESTROYED ,holder.getSurface()));
    }

    @Override
    public void surfaceTextureCreated(SurfaceTexture surfaceTexture) {
        // surfaceTexture has been created. You can use camera to preview.

    }

    public void onDestroy(){
        mGLThread.quit();
    }

    public Handler getCameraHandler() {
        if (mCameraHandler == null) {
            mCameraHandler = new GLCameraHandler(mGLThread.getLooper(), getContext() ,mGLCameraRenderer);
        }
        return mCameraHandler;
    }


}
