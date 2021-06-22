package com.coder.x264cmake.module.filter.effect;

import android.content.Context;
import android.opengl.GLES20;

import com.coder.x264cmake.utils.FileUtils;
import com.coder.x264cmake.utils.OpenGlUtils;

public class GLImageEffectSoulStuffFilter extends GLImageEffectFilter {

    private static final String FRAG_SHAKE_SCALE = "scale";

    private int mScaleLoc;

    private float mScale = 1.0f;
    private float mOffset = 0.0f;

    public GLImageEffectSoulStuffFilter(Context context) {
        this(context,VERTEX_SHADER, FileUtils.getShaderFromAssets(context, "shader/effect/fragment_effect_soul_stuff.glsl"));
    }

    public GLImageEffectSoulStuffFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    protected void onInitGLProgram(boolean isValidate) {
        super.onInitGLProgram(isValidate);
        if (isValidate){
            mScaleLoc = GLES20.glGetUniformLocation(mGLProgramId,FRAG_SHAKE_SCALE);
        }else {
            mScaleLoc = OpenGlUtils.NO_TEXTURE;
        }
    }

    @Override
    protected void onPreDraw() {
        super.onPreDraw();
        GLES20.glUniform1f(mScaleLoc, mScale);
    }

    @Override
    protected void calculateInterval() {
        // 步进，40ms算一次步进
        float interval = mCurrentPosition % 40.0f;
        mOffset += interval * 0.0025f;
        if (mOffset > 1.0f) {
            mOffset = 0.0f;
        }
        mScale = 1.0f + 0.3f * getInterpolation(mOffset);
    }

    private float getInterpolation(float input) {
        return (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
    }
}
