package com.coder.x264cmake.module.filter.color;

import android.content.Context;
import android.opengl.GLES20;

import com.coder.x264cmake.module.filter.GLImageBaseFilter;
import com.coder.x264cmake.utils.FileUtils;
import com.coder.x264cmake.utils.OpenGlUtils;

public class GLImageBlackCatFilter extends GLImageBaseFilter {

    private static final String FRAG_ANTIQUE_CURVE = "curveTexture";
    private static final String FRAG_ANTIQUE_STRENGTH = "strength";

    private int mCurveUniformLoc;
    private int mStrengthUniformLoc;

    private int mCurveTexture;

    private float mStrength =1.0f;

    public GLImageBlackCatFilter(Context context) {
        this(context, VERTEX_SHADER, FileUtils.getShaderFromAssets(context, "shader/color/fragment_black_cat.glsl"));
    }

    public GLImageBlackCatFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    protected void onInitGLProgram(boolean isValidate) {
        super.onInitGLProgram(isValidate);
        if (isValidate) {
            mCurveUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_ANTIQUE_CURVE);
            mStrengthUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_ANTIQUE_STRENGTH);

            loadTextures();
        } else {
            mCurveUniformLoc = OpenGlUtils.NO_TEXTURE;
            mStrengthUniformLoc = OpenGlUtils.NO_TEXTURE;
        }
    }


    private void setStrength() {
//        setFloat(mStrengthUniformLoc, mStrength);
    }

    /**
     * 加载纹理
     */
    private void loadTextures() {
        mCurveTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/blackcat/curve.png"));
    }

    @Override
    protected void onPreDraw() {
        super.onPreDraw();
        OpenGlUtils.bindTexture(mCurveUniformLoc, getTextureType(), mCurveTexture, 1);

        GLES20.glUniform1f(mStrengthUniformLoc, mStrength);
    }

    @Override
    public void release() {
        super.release();
        GLES20.glDeleteTextures(1, new int[]{mCurveTexture}, 0);
    }
}
