package com.coder.x264cmake;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.coder.x264cmake.module.GLRenderer;

import static android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY;

public class GLCameraActivity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(new GLRenderer());
        mGLSurfaceView.setRenderMode(RENDERMODE_CONTINUOUSLY);

        setContentView(mGLSurfaceView);
    }
}
