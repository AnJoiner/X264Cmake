package com.coder.x264cmake.module.filter.color;

import android.content.Context;
import android.opengl.GLES20;

import com.coder.x264cmake.module.filter.GLImageBaseFilter;
import com.coder.x264cmake.utils.FileUtils;
import com.coder.x264cmake.utils.OpenGlUtils;

public class GLImageEarlyBirdFilter extends GLImageBaseFilter {

    private static final String FRAG_CURVE = "curveTexture";
    private static final String FRAG_OVERLAY = "overlayTexture";
    private static final String FRAG_VIGNETTE = "vignetteTexture";
    private static final String FRAG_BLOWOUT = "blowoutTexture";
    private static final String FRAG_MAP = "mapTexture";

    private int mCurveUniformLoc;
    private int mOverlayUniformLoc;
    private int mVignetteUniformLoc;
    private int mBlowoutUniformLoc;
    private int mMapUniformLoc;

    private int mCurveTexture;
    private int mOverlayTexture;
    private int mVignetteTexture;
    private int mBlowoutTexture;
    private int mMapTexture;

    public GLImageEarlyBirdFilter(Context context) {
        this(context,VERTEX_SHADER, FileUtils.getShaderFromAssets(context,"shader/color/fragment_earlybird.glsl"));
    }

    public GLImageEarlyBirdFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    protected void onInitGLProgram(boolean isValidate) {
        super.onInitGLProgram(isValidate);
        if (isValidate){
            mCurveUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_CURVE);
            mOverlayUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_OVERLAY);
            mVignetteUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_VIGNETTE);
            mBlowoutUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_BLOWOUT);
            mMapUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_MAP);

            loadTextures();
        }else {
            mCurveUniformLoc = OpenGlUtils.NO_TEXTURE;
            mOverlayUniformLoc = OpenGlUtils.NO_TEXTURE;
            mVignetteUniformLoc = OpenGlUtils.NO_TEXTURE;
            mBlowoutUniformLoc = OpenGlUtils.NO_TEXTURE;
            mMapUniformLoc = OpenGlUtils.NO_TEXTURE;
        }
    }

    private void loadTextures(){
        mCurveTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/earlybird/earlybird_curve.png"));
        mOverlayTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/earlybird/earlybird_overlay.png"));
        mVignetteTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/earlybird/earlybird_vignette.png"));
        mBlowoutTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/earlybird/earlybird_blowout.png"));
        mMapTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/earlybird/earlybird_map.png"));
    }


    @Override
    protected void onPreDraw() {
        super.onPreDraw();
        OpenGlUtils.bindTexture(mCurveUniformLoc, getTextureType(), mCurveTexture, 1);
        OpenGlUtils.bindTexture(mOverlayUniformLoc, getTextureType(), mOverlayTexture, 2);
        OpenGlUtils.bindTexture(mVignetteUniformLoc, getTextureType(), mVignetteTexture, 3);
        OpenGlUtils.bindTexture(mBlowoutUniformLoc, getTextureType(), mBlowoutTexture, 4);
        OpenGlUtils.bindTexture(mMapUniformLoc, getTextureType(), mMapTexture, 5);
    }

    @Override
    public void release() {
        super.release();
        GLES20.glDeleteTextures(5, new int[]{mCurveTexture, mOverlayTexture, mVignetteTexture, mBlowoutTexture, mMapTexture}, 0);
    }
}
