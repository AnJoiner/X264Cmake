package com.coder.x264cmake.module.filter.color;

import android.content.Context;
import android.opengl.GLES20;

import com.coder.x264cmake.module.filter.GLImageBaseFilter;
import com.coder.x264cmake.utils.FileUtils;
import com.coder.x264cmake.utils.OpenGlUtils;

public class GLImageFairyTaleFilter extends GLImageBaseFilter {

    private static final String FRAG_LOOKUP = "lookupTexture";

    private int mLookupUniformLoc;
    private int mLookupTexture;

    public GLImageFairyTaleFilter(Context context) {
        this(context, VERTEX_SHADER, FileUtils.getShaderFromAssets(context, "shader/color/fragment_fairytale.glsl"));
    }

    public GLImageFairyTaleFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    protected void onInitGLProgram(boolean isValidate) {
        super.onInitGLProgram(isValidate);
        if (isValidate) {
            mLookupUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_LOOKUP);
            loadTextures();
        } else {
            mLookupUniformLoc = OpenGlUtils.NO_TEXTURE;
        }
    }

    private void loadTextures() {
        mLookupTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/fairytale/fairy_tale.png"));
    }

    @Override
    protected void onPreDraw() {
        super.onPreDraw();
        OpenGlUtils.bindTexture(mLookupUniformLoc, getTextureType(), mLookupTexture, 1);
    }

    @Override
    public void release() {
        super.release();
        GLES20.glDeleteTextures(1, new int[]{mLookupTexture}, 0);
    }
}
