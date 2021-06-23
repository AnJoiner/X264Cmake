package com.coder.x264cmake.module.filter.beauty;

import android.content.Context;
import android.opengl.GLES20;

import com.coder.x264cmake.module.filter.GLImageBaseFilter;
import com.coder.x264cmake.utils.FileUtils;

import java.nio.FloatBuffer;

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
    // 美肤滤镜
    private GLImageBeautySkinFilter mGLImageBeautySkinFilter;

    public GLImageBeautyFilter(Context context) {
        this(context, VERTEX_SHADER, FileUtils.getShaderFromAssets(context,
                "shader/beauty/fragment_beauty.glsl"));
    }

    public GLImageBeautyFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
        mGLImageBeautySkinFilter = new GLImageBeautySkinFilter(context);
    }

    @Override
    public void onCreateFrameBuffer(int width, int height) {
        super.onCreateFrameBuffer((int) (width * mBlurScale), (int)(height *mBlurScale));
        if (mGLImageBeautySkinFilter != null) {
            mGLImageBeautySkinFilter.onCreateFrameBuffer(width, height);
        }
    }

    @Override
    public void onDestroyFrameBuffer() {
        super.onDestroyFrameBuffer();
        if (mGLImageBeautySkinFilter != null) {
            mGLImageBeautySkinFilter.onDestroyFrameBuffer();
        }
    }

    @Override
    protected void onInitGLProgram(boolean isValidate) {
        super.onInitGLProgram(isValidate);
        if (isValidate){
            mWidthUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_BEAUTY_WIDTH);
            mHeightUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_BEAUTY_HEIGHT);
            mOpacityUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_BEAUTY_OPACITY);

            setMillingSkinLevel(1.0f);
        }
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged((int) (width * mBlurScale), (int)(height * mBlurScale));
        // 宽高变更时需要重新设置宽高值
        setInteger(mWidthUniformLoc, (int) (width * mBlurScale));
        setInteger(mHeightUniformLoc, (int)(height * mBlurScale));

        if (mGLImageBeautySkinFilter != null) {
            mGLImageBeautySkinFilter.onSurfaceChanged(width, height);
        }
    }

    @Override
    public void onDisplaySizeChanged(int width, int height) {
        super.onDisplaySizeChanged(width, height);
        if (mGLImageBeautySkinFilter != null) {
            mGLImageBeautySkinFilter.onDisplaySizeChanged(width, height);
        }
    }

    @Override
    public int onDrawFrame(int textureId, FloatBuffer vertexFloatBuffer, FloatBuffer textureFloatBuffer, boolean isFrameBuffer) {
        int currentTexture = textureId;
        if (mGLImageBeautySkinFilter != null) {
            currentTexture = mGLImageBeautySkinFilter.onDrawFrame(currentTexture, vertexFloatBuffer, textureFloatBuffer,isFrameBuffer);
        }
        return super.onDrawFrame(currentTexture, vertexFloatBuffer, textureFloatBuffer, isFrameBuffer);
    }

    @Override
    public void release() {
        super.release();
        if (mGLImageBeautySkinFilter != null) {
            mGLImageBeautySkinFilter.release();
            mGLImageBeautySkinFilter = null;
        }
    }

    /**
     * 设置磨皮程度
     * @param percent 0.0 ~ 1.0
     */
    public void setMillingSkinLevel(float percent) {
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
     */
    private float calculateOpacity(float percent) {
        if (percent > 1.0f) {
            percent = 1.0f;
        }
        return (float) (1.0f - (1.0f - percent + 0.02) / 2.0f);
    }
}
