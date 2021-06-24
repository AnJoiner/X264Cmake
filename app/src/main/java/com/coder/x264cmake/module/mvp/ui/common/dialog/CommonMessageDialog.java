package com.coder.x264cmake.module.mvp.ui.common.dialog;

import android.app.Dialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.coder.x264cmake.R;
import com.coder.x264cmake.databinding.DialogCommonMessageBinding;
import com.coder.x264cmake.module.mvp.ui.base.BaseDialog;
import com.coder.x264cmake.utils.DensityUtils;

/**
 * @auther: AnJoiner
 * @datetime: 2021/6/24
 */
public class CommonMessageDialog extends BaseDialog<DialogCommonMessageBinding> implements View.OnClickListener {

    private String mTitle;
    private String mContent;

    private OnCommonMessageClickListener mOnCommonMessageClickListener;

    public void setOnCommonMessageClickListener(OnCommonMessageClickListener onCommonMessageClickListener) {
        mOnCommonMessageClickListener = onCommonMessageClickListener;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_common_message;
    }

    @Override
    protected void initParams(Dialog dialog) {
        super.initParams(dialog);
        dialog.getWindow().setLayout(DensityUtils.dp2px(300), ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    @Override
    protected void init() {
        initData();
        initListener();
    }

    private void initData(){
        mViewBinding.titleText.setText(TextUtils.isEmpty(mTitle)?"":mTitle);
        mViewBinding.contentText.setText(TextUtils.isEmpty(mContent)?"":mContent);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.negative_btn){
            if (mOnCommonMessageClickListener!=null){
                mOnCommonMessageClickListener.onCommonMessageClick(false);
            }
            dismiss();
        }else if (v.getId() == R.id.positive_btn){
            if (mOnCommonMessageClickListener!=null){
                mOnCommonMessageClickListener.onCommonMessageClick(true);
            }
            dismiss();
        }
    }

    private void initListener(){
        mViewBinding.negativeBtn.setOnClickListener(this);
        mViewBinding.positiveBtn.setOnClickListener(this);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public interface OnCommonMessageClickListener{
        void onCommonMessageClick(boolean isPositive);
    }
}
