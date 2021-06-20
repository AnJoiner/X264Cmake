package com.coder.x264cmake;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.coder.x264cmake.module.camera.render.GLImageRenderer;

import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

public class GLCameraActivity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;
    // 渲染render
    private GLImageRenderer mGLImageRenderer;

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
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLImageRenderer = new GLImageRenderer(mGLSurfaceView);

        mGLSurfaceView.setRenderer(mGLImageRenderer);
        mGLSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
        mGLImageRenderer.resumeCamera();
//        mCamera1Loader.onResume(mGLSurfaceView.getWidth(),mGLSurfaceView.getHeight());
    }

    @Override
    protected void onPause() {
//        mCameraLoader.releaseCamera();
        mGLImageRenderer.releaseCamera();
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGLImageRenderer.release();
    }
}
