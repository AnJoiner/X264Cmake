package com.coder.x264cmake.module.mvp.ui.camera.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coder.x264cmake.R;
import com.coder.x264cmake.utils.DensityUtils;
import com.coder.x264cmake.utils.GlideUtils;

import org.jetbrains.annotations.NotNull;

public class CameraFilterAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public CameraFilterAdapter() {
        super(R.layout.adapter_camera_filter);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, String s) {
        GlideUtils.show(getContext(), s, holder.getView(R.id.filter_image), DensityUtils.dp2px(5));
    }
}
