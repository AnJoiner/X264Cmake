package com.coder.x264cmake;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.coder.x264cmake.module.GLRenderer;
import com.coder.x264cmake.module.camera.Camera1Loader;
import com.coder.x264cmake.module.filter.GLImageFilter;

import java.io.IOException;

import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

public class GLCameraActivity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;
    private GLImageFilter mGLImageFilter;
    private GLRenderer glRenderer;
    private Camera1Loader mCamera1Loader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCamera1Loader = new Camera1Loader(this);

        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLImageFilter = new GLImageFilter();

        glRenderer = new GLRenderer(mGLSurfaceView);
        glRenderer.setImageFilter(mGLImageFilter);
        glRenderer.setOnSurfaceListener(new GLRenderer.OnSurfaceListener() {
            @Override
            public void onSurfaceCreated(SurfaceTexture surfaceTexture) {
                try {
                    mCamera1Loader.cameraInstance.stopPreview();
                    mCamera1Loader.cameraInstance.setPreviewTexture(surfaceTexture);
                    mCamera1Loader.cameraInstance.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
//        glRenderer.setCameraLoader(mCamera1Loader);

        mGLSurfaceView.setRenderer(glRenderer);
        mGLSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);

        setContentView(mGLSurfaceView);

    }


    @Override
    protected void onResume() {
        super.onResume();
        mCamera1Loader.onResume(mGLSurfaceView.getWidth(),mGLSurfaceView.getHeight());
    }

    @Override
    protected void onPause() {
        mCamera1Loader.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        glRenderer.onDestroy();
    }
}
