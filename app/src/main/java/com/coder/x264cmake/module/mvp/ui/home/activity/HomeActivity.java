package com.coder.x264cmake.module.mvp.ui.home.activity;

import android.Manifest;
import android.view.View;

import androidx.annotation.NonNull;

import com.coder.x264cmake.H264Activity;
import com.coder.x264cmake.R;
import com.coder.x264cmake.databinding.ActivityHomeBinding;
import com.coder.x264cmake.module.mvp.ui.base.BaseActivity;
import com.coder.x264cmake.module.mvp.ui.camera.activity.GLCameraActivity;
import com.coder.x264cmake.module.mvp.ui.common.dialog.CommonMessageDialog;
import com.coder.x264cmake.utils.ToastUtils;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

/**
 * @auther: AnJoiner
 * @datetime: 2021/6/24
 */
@RuntimePermissions
public class HomeActivity extends BaseActivity<ActivityHomeBinding> implements View.OnClickListener {

    private CommonMessageDialog mCommonMessageDialog;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    protected void init() {
        initListener();
    }

    private void initListener(){
        mViewBinding.h264Btn.setOnClickListener(this);
        mViewBinding.beautyFilterBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.h264_btn){
            H264Activity.start(this);
        }else if (v.getId() == R.id.beauty_filter_btn){
            HomeActivityPermissionsDispatcher.showBeautyWithPermissionCheck(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        HomeActivityPermissionsDispatcher.onRequestPermissionsResult(this,requestCode,grantResults);
    }

    @NeedsPermission({Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showBeauty(){
        GLCameraActivity.start(this);
    }

    @OnPermissionDenied({Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onBeautyDenied(){
        popupCommonMessageDialog();
    }

    @OnNeverAskAgain({Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onNeverAskAgain(){
        ToastUtils.show("??????????????????????????????");
    }

    private void popupCommonMessageDialog(){
        if (mCommonMessageDialog == null){
            mCommonMessageDialog = new CommonMessageDialog();
            mCommonMessageDialog.setOnCommonMessageClickListener(isPositive -> {
                if (isPositive){
                        HomeActivityPermissionsDispatcher.showBeautyWithPermissionCheck(this);
                }
            });
        }
        if (mCommonMessageDialog.isVisible()){
            mCommonMessageDialog.dismiss();
        }else {
            mCommonMessageDialog.setTitle("??????");
            mCommonMessageDialog.setContent("???????????????????????????????????????????????????????????????????????????????????????????????????");
            mCommonMessageDialog.show(getSupportFragmentManager(),"CommonMessageDialog");
        }
    }
}
