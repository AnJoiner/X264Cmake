package com.coder.x264cmake.module.mvp.ui.common.dialog;

import android.text.TextUtils;

import com.coder.x264cmake.R;
import com.coder.x264cmake.databinding.DialogCommonMessageBinding;
import com.coder.x264cmake.module.mvp.ui.base.BaseDialog;

/**
 * @auther: AnJoiner
 * @datetime: 2021/6/24
 */
public class CommonMessageDialog extends BaseDialog<DialogCommonMessageBinding> {

    private String mTitle;
    private String mContent;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_common_message;
    }

    @Override
    protected void init() {
        mViewBinding.titleText.setText(TextUtils.isEmpty(mTitle)?"":mTitle);
        mViewBinding.contentText.setText(TextUtils.isEmpty(mContent)?"":mContent);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setContent(String content) {
        mContent = content;
    }
}
