package com.coder.x264cmake.module.filter.color;

import android.content.Context;
import android.opengl.GLES20;

import com.coder.x264cmake.module.filter.GLImageBaseFilter;
import com.coder.x264cmake.utils.FileUtils;
import com.coder.x264cmake.utils.OpenGlUtils;

public class GLImageBrannanFilter extends GLImageBaseFilter {

    private static final String FRAG_PROCESS = "processTexture";
    private static final String FRAG_BLOWOUT = "blowoutTexture";
    private static final String FRAG_CONTRAST = "contrastTexture";
    private static final String FRAG_LUMA = "lumaTexture";
    private static final String FRAG_SCREEN = "screenTexture";

    private static final String FRAG_STRENGTH = "strength";

    private int mProcessUniformLoc;
    private int mBlowoutUniformLoc;
    private int mContrastUniformLoc;
    private int mLumaUniformLoc;
    private int mScreenUniformLoc;

    private int mProcessTexture;
    private int mBlowoutTexture;
    private int mContrastTexture;
    private int mLumaTexture;
    private int mScreenTexture;

    private int mStrengthUniformLoc;

    private float mStrength = 1.0f;

    public GLImageBrannanFilter(Context context) {
        this(context, VERTEX_SHADER, FileUtils.getShaderFromAssets(context, "shader/color/fragment_brannan.glsl"));
    }

    public GLImageBrannanFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    protected void onInitGLProgram(boolean isValidate) {
        super.onInitGLProgram(isValidate);
        if (isValidate) {
            mProcessUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_PROCESS);
            mBlowoutUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_BLOWOUT);
            mContrastUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_CONTRAST);
            mLumaUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_LUMA);
            mScreenUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_SCREEN);

            mStrengthUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_STRENGTH);

            loadTextures();
        } else {
            mProcessUniformLoc = OpenGlUtils.NO_TEXTURE;
            mBlowoutUniformLoc = OpenGlUtils.NO_TEXTURE;
            mContrastUniformLoc = OpenGlUtils.NO_TEXTURE;
            mLumaUniformLoc = OpenGlUtils.NO_TEXTURE;
            mScreenUniformLoc = OpenGlUtils.NO_TEXTURE;

            mStrengthUniformLoc = OpenGlUtils.NO_TEXTURE;
        }
    }


    private void loadTextures() {
        mProcessTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/brannan/brannan_process.png"));
        mBlowoutTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/brannan/brannan_blowout.png"));
        mContrastTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/brannan/brannan_contrast.png"));
        mLumaTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/brannan/brannan_luma.png"));
        mScreenTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/brannan/brannan_screen.png"));
    }

    @Override
    protected void onPreDraw() {
        super.onPreDraw();
        OpenGlUtils.bindTexture(mProcessUniformLoc, getTextureType(), mProcessTexture, 1);
        OpenGlUtils.bindTexture(mBlowoutUniformLoc, getTextureType(), mBlowoutTexture, 2);
        OpenGlUtils.bindTexture(mContrastUniformLoc, getTextureType(), mContrastTexture, 3);
        OpenGlUtils.bindTexture(mLumaUniformLoc, getTextureType(), mLumaTexture, 4);
        OpenGlUtils.bindTexture(mScreenUniformLoc, getTextureType(), mScreenTexture, 5);

        GLES20.glUniform1f(mStrengthUniformLoc, mStrength);
    }

    @Override
    public void release() {
        super.release();
        GLES20.glDeleteTextures(5, new int[]{mProcessTexture, mBlowoutTexture, mContrastTexture, mLumaTexture, mScreenTexture}, 0);
    }

}
