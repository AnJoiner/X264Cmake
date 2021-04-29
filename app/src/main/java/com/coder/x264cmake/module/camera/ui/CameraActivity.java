package com.coder.x264cmake.module.camera.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.coder.x264cmake.R;
import com.coder.x264cmake.databinding.ActivityCameraBinding;
import com.coder.x264cmake.module.camera.loader.CameraLoader;
import com.coder.x264cmake.utils.LogUtils;
import com.coder.x264cmake.widgets.CameraPreview;

import java.io.IOException;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener{

    private ActivityCameraBinding mViewBinding;
    // 相机
    private CameraLoader mCameraLoader;
    // 相机预览
    private CameraPreview mPreview;
    public static void start(Context context) {
        Intent starter = new Intent(context, CameraActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = ActivityCameraBinding.inflate(LayoutInflater.from(this));
        setContentView(mViewBinding.getRoot());

        init();
    }

    private void init(){
        initCamera();
        initListener();
    }

    private void initCamera() {
        mCameraLoader = new CameraLoader();
        mCameraLoader.setOnCameraPreCallback(new CameraLoader.OnCameraPreCallback() {
            @Override
            public void onCameraPreFrame(byte[] data, int width, int height) {
                // data nv21
                LogUtils.i("CameraPreFrame ===>>> data size:"+ data.length +", width x height:"+width*height);
            }
        });
        mPreview = new CameraPreview(this, mCameraLoader);
        mViewBinding.cameraPreview.addView(mPreview);
    }

    private void initListener() {
        mViewBinding.cameraBtn.setOnClickListener(this);
        mViewBinding.backBtn.setOnClickListener(this);
        mViewBinding.switchBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.camera_btn){

        }else if (v.getId() == R.id.back_btn){
            finish();
        }else if (v.getId() == R.id.switch_btn){
            // 切换摄像头
            onSwitchCamera();
        }
    }


    /**
     * 切换摄像头
     */
    private void onSwitchCamera(){
        if (mCameraLoader!=null){
            mCameraLoader.switchCamera();
            SurfaceHolder holder = mPreview.getHolder();
            if (holder!=null){
                try {
                    mCameraLoader.cameraInstance.setPreviewDisplay(holder);
                    mCameraLoader.cameraInstance.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
