package com.coder.x264cmake.module.filter.color;

import android.content.Context;
import android.opengl.GLES20;

import com.coder.x264cmake.module.filter.GLImageBaseFilter;
import com.coder.x264cmake.utils.FileUtils;
import com.coder.x264cmake.utils.OpenGlUtils;

public class GLImageFreudFilter extends GLImageBaseFilter {

    private static final String FRAG_RAND = "randTexture";
    private static final String FRAG_WIDTH = "texelWidthOffset";
    private static final String FRAG_HEIGHT = "texelHeightOffset";
    private static final String FRAG_STRENGTH = "strength";

    private int mRandUniformLoc;
    private int mWidthUniformLoc;
    private int mHeightUniformLoc;
    private int mStrengthUniformLoc;

    private int mRandTexture;

    private float mStrength = 1.0f;

    public GLImageFreudFilter(Context context) {
        this(context,VERTEX_SHADER, "shader/color/fragment_freud.glsl");
    }

    public GLImageFreudFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    protected void onInitGLProgram(boolean isValidate) {
        super.onInitGLProgram(isValidate);
        if (isValidate){
            mRandUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_RAND);
            mWidthUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_WIDTH);
            mHeightUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_HEIGHT);
            mStrengthUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_STRENGTH);

            loadTextures();
        }else {
            mRandUniformLoc = OpenGlUtils.NO_TEXTURE;
            mWidthUniformLoc = OpenGlUtils.NO_TEXTURE;
            mHeightUniformLoc = OpenGlUtils.NO_TEXTURE;
            mStrengthUniformLoc = OpenGlUtils.NO_TEXTURE;
        }
    }

    private void loadTextures(){
        mRandTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/freud/freud_rand.png"));
    }

    @Override
    protected void onPreDraw() {
        super.onPreDraw();
        OpenGlUtils.bindTexture(mRandUniformLoc, getTextureType(), mRandTexture, 1);
        GLES20.glUniform1f(mStrengthUniformLoc, mStrength);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged(width, height);
        GLES20.glUniform1f(mWidthUniformLoc, 1.0f/(float) width);
        GLES20.glUniform1f(mHeightUniformLoc, 1.0f/(float) height);
    }

    @Override
    public void release() {
        super.release();
        GLES20.glDeleteTextures(1, new int[]{mRandTexture},0);
    }
}
