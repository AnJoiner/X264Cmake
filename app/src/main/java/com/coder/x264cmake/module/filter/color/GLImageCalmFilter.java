package com.coder.x264cmake.module.filter.color;

import android.content.Context;
import android.opengl.GLES20;

import com.coder.x264cmake.module.filter.GLImageBaseFilter;
import com.coder.x264cmake.utils.FileUtils;
import com.coder.x264cmake.utils.OpenGlUtils;

public class GLImageCalmFilter extends GLImageBaseFilter {

    private static final String FRAG_MASK1 = "mask1Texture";
    private static final String FRAG_MASK2 = "mask2Texture";
    private static final String FRAG_CURVE = "curveTexture";

    private int mMask1UniformLoc;
    private int mMask2UniformLoc;
    private int mCurveUniformLoc;

    private int mMask1Texture;
    private int mMask2Texture;
    private int mCurveTexture;


    public GLImageCalmFilter(Context context) {
        this(context, VERTEX_SHADER, FileUtils.getShaderFromAssets(context, "shader/color/fragment_calm.glsl"));
    }

    public GLImageCalmFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    protected void onInitGLProgram(boolean isValidate) {
        super.onInitGLProgram(isValidate);
        if (isValidate) {
            mMask1UniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_MASK1);
            mMask2UniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_MASK2);
            mCurveUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_CURVE);

            loadTextures();
        } else {
            mMask1UniformLoc = OpenGlUtils.NO_TEXTURE;
            mMask2UniformLoc = OpenGlUtils.NO_TEXTURE;
            mCurveUniformLoc = OpenGlUtils.NO_TEXTURE;
        }
    }

    private void loadTextures() {
        mMask1Texture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/calm/calm_mask1.png"));
        mMask2Texture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/calm/calm_mask2.png"));
        mCurveTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/calm/calm_curve.png"));
    }

    @Override
    protected void onPreDraw() {
        super.onPreDraw();
        OpenGlUtils.bindTexture(mMask1UniformLoc, getTextureType(), mMask1Texture, 1);
        OpenGlUtils.bindTexture(mMask2UniformLoc, getTextureType(), mMask2Texture, 2);
        OpenGlUtils.bindTexture(mCurveUniformLoc, getTextureType(), mCurveTexture, 3);
    }

    @Override
    public void release() {
        super.release();
        GLES20.glDeleteTextures(3, new int[]{mMask1Texture, mMask2Texture, mCurveTexture}, 0);
    }
}
