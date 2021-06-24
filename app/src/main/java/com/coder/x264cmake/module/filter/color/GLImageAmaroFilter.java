package com.coder.x264cmake.module.filter.color;

import android.content.Context;
import android.opengl.GLES20;

import com.coder.x264cmake.module.filter.GLImageBaseFilter;
import com.coder.x264cmake.utils.FileUtils;
import com.coder.x264cmake.utils.OpenGlUtils;

public class GLImageAmaroFilter extends GLImageBaseFilter {

    private static final String FRAG_AMARO_BLOWOUT = "blowoutTexture";
    private static final String FRAG_AMARO_OVERLAY = "overlayTexture";
    private static final String FRAG_AMARO_MAP = "mapTexture";

    private static final String FRAG_AMARO_STRENGTH = "strength";

    private int mStrengthUniformLoc;

    private float mStrength = 1.0f;

    private int[] mTextures;
    private int[] mUniformLocs;

    public GLImageAmaroFilter(Context context) {
        this(context, VERTEX_SHADER, FileUtils.getShaderFromAssets(context, "shader/color/fragment_amaro.glsl"));
    }

    public GLImageAmaroFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    protected void onInitGLProgram(boolean isValidate) {
        super.onInitGLProgram(isValidate);

        if (mUniformLocs == null) mUniformLocs = new int[3];
        if (isValidate) {
            mUniformLocs[0]  = GLES20.glGetUniformLocation(mGLProgramId, FRAG_AMARO_BLOWOUT);
            mUniformLocs[1]  = GLES20.glGetUniformLocation(mGLProgramId, FRAG_AMARO_OVERLAY);
            mUniformLocs[2]  = GLES20.glGetUniformLocation(mGLProgramId, FRAG_AMARO_MAP);

            mStrengthUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_AMARO_STRENGTH);

//            setStrength();
            loadTextures();
        }else {
            mUniformLocs[0] = OpenGlUtils.NO_TEXTURE;
            mUniformLocs[1] = OpenGlUtils.NO_TEXTURE;
            mUniformLocs[2] = OpenGlUtils.NO_TEXTURE;
        }
    }

    private void setStrength(){
//        setFloat(mStrengthUniformLoc, mStrength);
    }

    /**
     * 加载纹理
     */
    private void loadTextures() {
        if (mTextures == null) {
            mTextures = new int[3];
        }
        mTextures[0] = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/amaro/blowout.png"));
        mTextures[1] = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/amaro/overlay.png"));
        mTextures[2] = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/amaro/map.png"));
    }


    @Override
    protected void onPreExtra() {
        super.onPreExtra();
    }

    @Override
    protected void onPreDraw() {
        super.onPreDraw();

        for (int i = 0; i < mTextures.length ; i++) {
            OpenGlUtils.bindTexture(mUniformLocs[i],getTextureType(), mTextures[i], i+1);
        }
        GLES20.glUniform1f(mStrengthUniformLoc, mStrength);
    }


    @Override
    public void release() {
        super.release();
        GLES20.glDeleteTextures(mTextures.length,mTextures,0);
        mTextures = null;
        mUniformLocs = null;
    }
}
