package com.coder.x264cmake.module.filter.beauty;

import android.content.Context;
import android.opengl.GLES20;

import com.coder.x264cmake.module.filter.GLImageBaseFilter;
import com.coder.x264cmake.utils.FileUtils;
import com.coder.x264cmake.utils.OpenGlUtils;

public class GLImageBeautySkinFilter extends GLImageBaseFilter {

    public static final String FRAG_SKIN_GRAY_TEXTURE = "grayTexture";
    public static final String FRAG_SKIN_LOOKUP_TEXTURE = "lookupTexture";

    public static final String FRAG_SKIN_LEVEL_RANGE_INV = "levelRangeInv";
    public static final String FRAG_SKIN_LEVEL_BLACK = "levelBlack";
    public static final String FRAG_SKIN_ALPHA = "alpha";

    private int mGrayTextureLoc; // 灰度纹理
    private int mLookupTextureLoc;  // LUT
    private int mLevelRangeInvLoc;  // 范围
    private int mLevelBlackLoc;  // 灰度level
    private int mAlphaLoc;  // 肤色程度

    private int mGrayTexture;
    private int mLookupTexture;
    private float mLevelRangeInv = 1.040816f;
    private float mLevelBlack = 0.01960784f;
    private float mAlpha = 0.5f;

    public GLImageBeautySkinFilter(Context context) {
        this(context, VERTEX_SHADER, FileUtils.getShaderFromAssets(context, "shader/beauty/fragment_beauty_skin.glsl"));
    }

    public GLImageBeautySkinFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);

    }

    @Override
    protected void onInitGLProgram(boolean isValidate) {
        super.onInitGLProgram(isValidate);
        if (isValidate) {
            mGrayTextureLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_SKIN_GRAY_TEXTURE);
            mLookupTextureLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_SKIN_LOOKUP_TEXTURE);

            mLevelRangeInvLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_SKIN_LEVEL_RANGE_INV);
            mLevelBlackLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_SKIN_LEVEL_BLACK);
            mAlphaLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_SKIN_ALPHA);

            loadTextures();
        } else {
            mGrayTextureLoc = OpenGlUtils.NO_TEXTURE;
            mLookupTextureLoc = OpenGlUtils.NO_TEXTURE;

            mLevelRangeInvLoc = OpenGlUtils.NO_TEXTURE;
            mLevelBlackLoc = OpenGlUtils.NO_TEXTURE;
            mAlphaLoc = OpenGlUtils.NO_TEXTURE;
        }
    }

    /**
     * 加载纹理
     */
    private void loadTextures() {
        mGrayTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/beauty/skin/skin_gray.png"));
        mLookupTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/beauty/skin/skin_lookup.png"));
    }

    @Override
    protected void onPreDraw() {
        super.onPreDraw();
        bindTexture(mGrayTextureLoc, mGrayTexture, 1);
        bindTexture(mLookupTextureLoc, mLookupTexture, 2);

        GLES20.glUniform1f(mLevelRangeInvLoc, mLevelRangeInv);
        GLES20.glUniform1f(mLevelBlackLoc, mLevelBlack);
        GLES20.glUniform1f(mAlphaLoc, mAlpha);
    }

    private void bindTexture(int location, int texture, int index) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + index);
        GLES20.glBindTexture(getTextureType(), texture);
        GLES20.glUniform1i(location, index);
    }

    @Override
    public void release() {
        super.release();
        GLES20.glDeleteTextures(2, new int[]{ mGrayTexture, mLookupTexture }, 0);
    }

    /**
     * 美肤程度
     * @param level 0 ~ 1.0f
     */
    public void setBeautySkinLevel(float level) {
        mAlpha = level;
    }
}
