package com.coder.x264cmake.module.filter.color;

import android.content.Context;
import android.opengl.GLES20;

import com.coder.x264cmake.module.filter.GLImageBaseFilter;
import com.coder.x264cmake.utils.FileUtils;
import com.coder.x264cmake.utils.OpenGlUtils;

public class GLImageEmeraldFilter extends GLImageBaseFilter {

    private static final String FRAG_CURVE = "curveTexture";

    private int mCurveUniformLoc;

    private int mCurveTexture;

    public GLImageEmeraldFilter(Context context) {
        this(context, VERTEX_SHADER, FileUtils.getShaderFromAssets(context, "shader/color/fragment_emerald.glsl"));
    }

    public GLImageEmeraldFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    protected void onInitGLProgram(boolean isValidate) {
        super.onInitGLProgram(isValidate);
        if (isValidate) {
            mCurveUniformLoc = GLES20.glGetUniformLocation(mGLProgramId, FRAG_CURVE);
            loadTextures();
        } else {
            mCurveUniformLoc = OpenGlUtils.NO_TEXTURE;
        }
    }

    private void loadTextures() {
        mCurveTexture = OpenGlUtils.loadTexture(FileUtils.getImageFromAssetsFile(mContext,
                "filter/color/emerald/emerald_curve.png"));
    }

    @Override
    protected void onPreDraw() {
        super.onPreDraw();
        OpenGlUtils.bindTexture(mCurveUniformLoc, getTextureType(), mCurveTexture, 1);
    }

    @Override
    public void release() {
        super.release();
        GLES20.glDeleteTextures(1, new int[]{mCurveTexture}, 0);
    }
}
