package com.coder.x264cmake;

import android.view.View;

import com.coder.x264cmake.databinding.ActivityGlCameraBinding;
import com.coder.x264cmake.module.mvp.ui.base.BaseActivity;
import com.coder.x264cmake.module.mvp.ui.camera.CameraFilterDialog;


public class GLCameraActivity extends BaseActivity<ActivityGlCameraBinding> implements View.OnClickListener {

    private CameraFilterDialog mCameraFilterDialog;

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
        mViewBinding.cameraSwitchBtn.setOnClickListener(this);
        mViewBinding.cameraFilterBtn.setOnClickListener(this);
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
