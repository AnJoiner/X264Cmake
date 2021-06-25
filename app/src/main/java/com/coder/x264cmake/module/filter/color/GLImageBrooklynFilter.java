package com.coder.x264cmake.module.filter.color;

import android.content.Context;
import android.opengl.GLES20;

import com.coder.x264cmake.module.filter.GLImageBaseFilter;
import com.coder.x264cmake.utils.FileUtils;
import com.coder.x264cmake.utils.OpenGlUtils;

public class GLImageBrooklynFilter extends GLImageBaseFilter {

    private static final String FRAG_CURVE1 = "curve1Texture";
    private static final String FRAG_MAP = "mapTexture";
    private static final String FRAG_CURVE2 = "curve2Texture";

    private static final String FRAG_STRENGTH = "strength";

    private int mCurve1UniformLoc;
    private int mMapUniformLoc;
    private int mCurve2UniformLoc;

    private int mStrengthUniformLoc;

    private int mCurve1Texture;
    private int mMapTexture;
    private int mCurve2Texture;

    private float mStrength = 1.0f;

    public GLImageBrooklynFilter(Context context) {
        this(context, VERTEX_SHADER, FileUtils.getShaderFromAssets(context, "shader/color/fragment_brooklyn.glsl"));
    }

    public GLImageBrooklynFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    protected void onInitGLProgram(boolean isValidate) {
        super.onInitGLProgram(isValidate);
        if (isValidate) {
            mCurve1UniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_CURVE1);
            mMapUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_MAP);
            mCurve2UniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_CURVE2);

            mStrengthUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_STRENGTH);

            loadTextures();
        } else {
            mCurve1UniformLoc = OpenGlUtils.NO_TEXTURE;
            mMapUniformLoc = OpenGlUtils.NO_TEXTURE;
            mCurve2UniformLoc = OpenGlUtils.NO_TEXTURE;

            mStrengthUniformLoc = OpenGlUtils.NO_TEXTURE;
        }
    }


    private void loadTextures() {
        mCurve1Texture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/brooklyn/brooklyn_curve1.png"));
        mMapTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/brooklyn/brooklyn_map.png"));
        mCurve2Texture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/brooklyn/brooklyn_curve2.png"));
    }

    @Override
    protected void onPreDraw() {
        super.onPreDraw();
        OpenGlUtils.bindTexture(mCurve1UniformLoc, getTextureType(), mCurve1Texture, 1);
        OpenGlUtils.bindTexture(mMapUniformLoc, getTextureType(), mMapTexture, 2);
        OpenGlUtils.bindTexture(mCurve2UniformLoc, getTextureType(), mCurve2Texture, 3);

        GLES20.glUniform1f(mStrengthUniformLoc, mStrength);
    }

    @Override
    public void release() {
        super.release();
        GLES20.glDeleteTextures(3, new int[]{mCurve1Texture, mMapTexture, mCurve2Texture}, 0);
    }
}
