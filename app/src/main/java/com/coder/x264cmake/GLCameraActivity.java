package com.coder.x264cmake;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.coder.x264cmake.module.camera.render.GLImageRenderer;
import com.coder.x264cmake.widgets.GLCameraPreview;

import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

public class GLCameraActivity extends AppCompatActivity {

    private GLCameraPreview mGLCameraPreview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_gl_camera);

        init();
    }

    private void init() {
        initView();
        initListener();
        initData();
    }

    private void initView() {
        mGLCameraPreview = findViewById(R.id.camera_preview);
    }

    private void initListener(){

    }

    private void initData() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGLCameraPreview!=null) mGLCameraPreview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGLCameraPreview!=null) mGLCameraPreview.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGLCameraPreview!=null) mGLCameraPreview.onRelease();
    }
}
