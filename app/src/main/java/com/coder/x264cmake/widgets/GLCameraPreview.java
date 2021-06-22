package com.coder.x264cmake.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @auther: AnJoiner
 * @datetime: 2021/6/19
 */
public class GLCameraPreview extends FrameLayout {

    private View mSurfaceView;

    public GLCameraPreview(Context context) {
        this(context,null);
    }

    public GLCameraPreview(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public GLCameraPreview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 需根据用户选择设置视图大小
    }
}
