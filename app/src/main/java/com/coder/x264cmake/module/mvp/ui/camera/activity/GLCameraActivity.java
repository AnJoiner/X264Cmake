package com.coder.x264cmake.module.mvp.ui.camera.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.coder.x264cmake.R;
import com.coder.x264cmake.databinding.ActivityGlCameraBinding;
import com.coder.x264cmake.module.mvp.ui.base.BaseActivity;
import com.coder.x264cmake.module.mvp.ui.camera.dialog.CameraFilterDialog;


public class GLCameraActivity extends BaseActivity<ActivityGlCameraBinding> implements View.OnClickListener {

    private CameraFilterDialog mCameraFilterDialog;

    public static void start(Context context) {
        Intent starter = new Intent(context, GLCameraActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_gl_camera;
    }

    @Override
    protected void init() {
        initListener();
        initData();
    }

    private void initListener() {
        mViewBinding.cameraCloseBtn.setOnClickListener(this);
        mViewBinding.cameraSwitchBtn.setOnClickListener(this);
        mViewBinding.cameraFilterBtn.setOnClickListener(this);
        mViewBinding.cameraBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.camera_close_btn) {
            finish();
        } else if (v.getId() == R.id.camera_switch_btn) {
            // 切换相机镜头
            mViewBinding.cameraPreview.switchCamera();
        } else if (v.getId() == R.id.camera_filter_btn) {
            // 滤镜弹窗
            popupCameraFilterDialog();
        }else if (v.getId() == R.id.camera_btn){
            mViewBinding.cameraPreview.takePicture();
        }
    }

    private void initData() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewBinding.cameraPreview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mViewBinding.cameraPreview.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewBinding.cameraPreview.onRelease();
    }

    private void popupCameraFilterDialog(){
        if (mCameraFilterDialog == null){
            mCameraFilterDialog = new CameraFilterDialog();
        }
        if (mCameraFilterDialog.isVisible()){
            mCameraFilterDialog.dismiss();
        }else {
            mCameraFilterDialog.show(getSupportFragmentManager(),"CameraFilterDialog");
        }
    }

}
