package com.coder.x264cmake;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.coder.x264cmake.module.GLRenderer;
import com.coder.x264cmake.module.camera.CameraLoader;
import com.coder.x264cmake.module.filter.GLImageFilter;
import com.coder.x264cmake.utils.LogUtils;

import java.io.IOException;

import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

public class GLCameraActivity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;
    private GLImageFilter mGLImageFilter;
    private GLRenderer glRenderer;
    //    private Camera1Loader mCamera1Loader;
    private CameraLoader mCameraLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_gl_camera);

        init();
    }

    private void init() {
        initView();
        initData();
    }

    private void initView() {
        mGLSurfaceView = findViewById(R.id.camera_surface);
    }

    private void initData() {
        mCameraLoader = new CameraLoader();

        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLImageFilter = new GLImageFilter();

        glRenderer = new GLRenderer(mGLSurfaceView);
        glRenderer.setImageFilter(mGLImageFilter);
        glRenderer.setOnSurfaceListener(new GLRenderer.OnSurfaceListener() {
            @Override
            public void onSurfaceCreated(SurfaceTexture surfaceTexture) {
                try {
                    mCameraLoader.setUpCamera();
                    mCameraLoader.cameraInstance.setPreviewTexture(surfaceTexture);
                    mCameraLoader.cameraInstance.startPreview();
//                    mCamera1Loader.cameraInstance.stopPreview();
//                    mCamera1Loader.cameraInstance.setPreviewTexture(surfaceTexture);
//                    mCamera1Loader.cameraInstance.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mGLSurfaceView.setRenderer(glRenderer);
        mGLSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
//        mCamera1Loader.onResume(mGLSurfaceView.getWidth(),mGLSurfaceView.getHeight());
    }

    @Override
    protected void onPause() {
        mCameraLoader.releaseCamera();
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        glRenderer.onDestroy();
    }
}
