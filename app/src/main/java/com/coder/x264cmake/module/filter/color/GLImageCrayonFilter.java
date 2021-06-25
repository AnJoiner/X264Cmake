package com.coder.x264cmake.module.filter.color;

import android.content.Context;
import android.opengl.GLES20;

import com.coder.x264cmake.module.filter.GLImageBaseFilter;
import com.coder.x264cmake.utils.FileUtils;
import com.coder.x264cmake.utils.OpenGlUtils;

import java.nio.FloatBuffer;

public class GLImageCrayonFilter extends GLImageBaseFilter {

    private static final String FRAG_STEP_OFFSET = "singleStepOffset";
    private static final String FRAG_STRENGTH = "strength";

    private int mStepOffsetUniformLoc;
    private int mStrengthUniformLoc;

    private float mStrength = 0.5f;

    public GLImageCrayonFilter(Context context) {
        this(context, VERTEX_SHADER, FileUtils.getShaderFromAssets(context, "shader/color/fragment_crayon.glsl"));
    }

    public GLImageCrayonFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    protected void onInitGLProgram(boolean isValidate) {
        super.onInitGLProgram(isValidate);
        if (isValidate) {
            mStepOffsetUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_STEP_OFFSET);
            mStrengthUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_STRENGTH);
        } else {
            mStepOffsetUniformLoc = OpenGlUtils.NO_TEXTURE;
            mStrengthUniformLoc = OpenGlUtils.NO_TEXTURE;
        }
    }

    @Override
    protected void onPreExtra() {
        super.onPreExtra();
        GLES20.glUniform1f(mStrengthUniformLoc, mStrength);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged(width, height);
        float[] array = new float[]{1.0f / width, 1.0f / height};
        GLES20.glUniform2fv(mStepOffsetUniformLoc, array.length, FloatBuffer.wrap(array));
    }
}
