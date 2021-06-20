package com.coder.x264cmake.module.filter.beauty;

import android.content.Context;
import android.opengl.GLES20;

import com.coder.x264cmake.module.filter.GLImageBaseFilter;
import com.coder.x264cmake.utils.FileUtils;

/**
 * @auther: AnJoiner
 * @datetime: 2021/6/20
 */
public class GLImageBeautyFilter extends GLImageBaseFilter {

    private static final String FRAG_BEAUTY_WIDTH = "width";
    private static final String FRAG_BEAUTY_HEIGHT = "height";
    private static final String FRAG_BEAUTY_OPACITY = "opacity";

    // 美颜宽度、高度
    private int mWidthUniformLoc;
    private int mHeightUniformLoc;
    // 磨皮程度
    private int mOpacityUniformLoc;

    // 高斯模糊处理的图像缩放倍数
    private float mBlurScale = 1.0f;

    public GLImageBeautyFilter(Context context) {
        this(context, VERTEX_SHADER, FileUtils.getShaderFromAssets(context,
                "shader/beauty/fragment_beauty.glsl"));
    }

    public GLImageBeautyFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void onCreateFrameBuffer(int width, int height) {
        super.onCreateFrameBuffer((int) (width * mBlurScale), (int)(height *mBlurScale));
    }

    @Override
    protected void onInitGLProgram(boolean isValidate) {
        super.onInitGLProgram(isValidate);
        if (isValidate){
            mWidthUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_BEAUTY_WIDTH);
            mHeightUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_BEAUTY_HEIGHT);
            mOpacityUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_BEAUTY_OPACITY);

            setSkinBeautyLevel(1.0f);
        }
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged((int) (width * mBlurScale), (int)(height * mBlurScale));
        // 宽高变更时需要重新设置宽高值
        setInteger(mWidthUniformLoc, (int) (width * mBlurScale));
        setInteger(mHeightUniformLoc, (int)(height * mBlurScale));
    }

    @Override
    public void onDisplaySizeChanged(int width, int height) {
        super.onDisplaySizeChanged(width, height);
    }

    /**
     * 设置磨皮程度
     * @param percent 0.0 ~ 1.0
     */
    public void setSkinBeautyLevel(float percent) {
        float opacity;
        if (percent <= 0) {
            opacity = 0.0f;
        } else {
            opacity = calculateOpacity(percent);
        }
        setFloat(mOpacityUniformLoc, opacity);
    }

    /**
     * 根据百分比计算出实际的磨皮程度
     * @param percent 0% ~ 100%
     * @return
     */
    private float calculateOpacity(float percent) {
        if (percent > 1.0f) {
            percent = 1.0f;
        }
        float result = (float) (1.0f - (1.0f - percent + 0.02) / 2.0f);

        return result;
    }
}
