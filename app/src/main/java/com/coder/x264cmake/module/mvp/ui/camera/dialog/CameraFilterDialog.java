package com.coder.x264cmake.module.mvp.ui.camera.dialog;

import android.app.Dialog;
import android.view.Gravity;
import android.widget.SeekBar;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.coder.x264cmake.R;
import com.coder.x264cmake.databinding.DialogCameraFilterBinding;
import com.coder.x264cmake.module.mvp.model.TableEntity;
import com.coder.x264cmake.module.mvp.ui.camera.adapter.CameraFilterAdapter;
import com.coder.x264cmake.module.mvp.ui.base.BaseDialog;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;

import java.util.ArrayList;

public class CameraFilterDialog extends BaseDialog<DialogCameraFilterBinding> {

    private CameraFilterAdapter mCameraFilterAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_camera_filter;
    }


    @Override
    protected void initParams(Dialog dialog) {
        super.initParams(dialog);
        dialog.getWindow().setDimAmount(0);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().setWindowAnimations(R.style.dialog_slide_anim);
    }

    @Override
    protected void init() {
        initTable();
        initRecycler();
        initListener();
    }

    private void initRecycler() {
        mCameraFilterAdapter = new CameraFilterAdapter();
        mViewBinding.filterRecycler.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mViewBinding.filterRecycler.setAdapter(mCameraFilterAdapter);
    }

    private void initTable() {
        ArrayList<CustomTabEntity> tableEntities = new ArrayList<>();
        String[] titles = getResources().getStringArray(R.array.filter_titles);
        for (int i = 0; i < titles.length; i++) {
            TableEntity entity = new TableEntity(titles[i], 0, 0);
            tableEntities.add(entity);
        }
        mViewBinding.tableLayout.setTabData(tableEntities);
        mViewBinding.tableLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {

            }

            @Override
            public void onTabReselect(int position) {

            }
        });
    }

    private void initListener() {
        mViewBinding.filterSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

}
