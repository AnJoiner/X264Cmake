package com.coder.x264cmake.module.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.coder.x264cmake.utils.FileUtils;
import com.coder.x264cmake.utils.OpenGlUtils;

public class GLImageOESFilter extends GLImageBaseFilter{
    // 变换矩阵
    private float[] matrix;

    public GLImageOESFilter(Context context) {
        this(context,FileUtils.getShaderFromAssets(context, "shader/base/vertex_oes.glsl"),
                FileUtils.getShaderFromAssets(context, "shader/base/fragment_oes.glsl"));
    }

    public GLImageOESFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    protected void onInitGLProgram(boolean isValidate) {
        super.onInitGLProgram(isValidate);
        if (isValidate){
            mGLUniformMatrix = GLES20.glGetUniformLocation(mGLProgramId, VERTEX_UNIFORM_MAT);
        }else {
            mGLUniformMatrix = OpenGlUtils.NO_GL;
        }
    }

    /**
     * 重写纹理类型，使用OES扩展类型
     */
    @Override
    protected int getTextureType() {
        return GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
    }

    @Override
    protected void onPreExtra() {
        super.onPreExtra();

    }

    @Override
    protected void onPreDraw() {
        super.onPreDraw();
        GLES20.glUniformMatrix4fv(mGLUniformMatrix, 1, false, matrix, 0);
    }

    /**
     * 设置变换矩阵
     * @param matrix 矩阵
     */
    public void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }
}
