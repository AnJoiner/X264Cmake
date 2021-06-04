package com.coder.x264cmake.module.filter;


import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.coder.x264cmake.utils.LogUtils;
import com.coder.x264cmake.utils.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class GLImageFilter {

    // 定点着色器
    public static final String VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec2 inputTextureCoordinate;\n" +
            "varying vec2 textureCoordinate;\n" +
            "void main()\n" +
            "{\n" +
            "    textureCoordinate = inputTextureCoordinate;\n" +
            "    gl_Position = position;\n" +
            "}";

    // oes片元着色器
    public static final String TexOES_FRAGMENT_SHADER = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            "void main()\n" +
            "{\n" +
            "    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    // 2d片元着色器
    public static final String Tex2D_FRAGMENT_SHADER = ""
            + "precision mediump float;\n"
            + "uniform sampler2D inputImageTexture;\n"
            + "varying vec2 textureCoordinate;\n"
            + "void main() {\n"
            + "  gl_FragColor = texture2D( inputImageTexture, textureCoordinate );\n" + "}";

    public static final String VERTEX_POSITION = "position";
    public static final String VERTEX_TEXCOORD = "inputTextureCoordinate";
    public static final String FRAG_UNIFORM_TEX = "inputImageTexture";

    // 材质顶点坐标
    private final float vertices[] = {
            -1, -1,
            -1, 1,
            1, -1,
            1, 1};
    // oes纹理坐标
    private final float texCoordOES[] = {
            0, 1,
            0, 0,
            1, 1,
            1, 0};
    // tex 2d纹理坐标
    private final float texCoord2D[] = {
            0, 0,
            0, 1,
            1, 0,
            1, 1};
    // gl程序id
    protected int glProgramId;
    // 顶点着色器位置
    protected int glAttribPosition;
    // 顶点着色器传入材质坐标
    protected int glAttribTexcoord;
    // 片元着色器纹理
    protected int glUniformTexture;
    private SurfaceTexture surfaceTexture;
    // 分配缓存空间
    private FloatBuffer vert, texOES, tex2D;
    // 纹理id
    private int[] textures;

    public GLImageFilter() {
        int bytes = vertices.length * Float.SIZE / Byte.SIZE;
        vert = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder()).asFloatBuffer();
        texOES = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder()).asFloatBuffer();
//        tex2D = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vert.put(vertices).position(0);
        texOES.put(texCoordOES).position(0);
//        tex2D.put(texCoord2D).position(0);
    }

    public SurfaceTexture onCreate() {
        glProgramId = OpenGlUtils.loadProgram(VERTEX_SHADER, TexOES_FRAGMENT_SHADER);
        boolean isValidate = OpenGlUtils.validateProgram(glProgramId);
        if (isValidate) {
            glAttribPosition = GLES20.glGetAttribLocation(glProgramId, VERTEX_POSITION);
            glAttribTexcoord = GLES20.glGetAttribLocation(glProgramId, VERTEX_TEXCOORD);
            glUniformTexture = GLES20.glGetUniformLocation(glProgramId, FRAG_UNIFORM_TEX);


            createTextureOesID();
            surfaceTexture = new SurfaceTexture(textures[0]);
        }
        return surfaceTexture;
    }


    public void onDestroy() {
        deleteSurfaceTexture();
        GLES20.glDeleteProgram(glProgramId);
    }

    private void deleteSurfaceTexture() {
        if (surfaceTexture != null) {
            surfaceTexture.release();
            surfaceTexture = null;
            // 删除纹理
            GLES20.glDeleteTextures(1, textures, 0);
        }
    }

    /**
     * 创建oes纹理id
     *
     * @return 纹理id
     */
    private void createTextureOesID() {
        textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
    }

    /**
     * 绘制纹理
     */
    public void drawTexture() {
        GLES20.glUseProgram(glProgramId);

        GLES20.glVertexAttribPointer(glAttribPosition, 2, GLES20.GL_FLOAT, false, 4 * 2, vert);
        GLES20.glVertexAttribPointer(glAttribTexcoord, 2, GLES20.GL_FLOAT, false, 4 * 2, texOES);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, glUniformTexture);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(glProgramId, FRAG_UNIFORM_TEX), 0);

//        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 2, GLES20.GL_UNSIGNED_SHORT, 4 * 2);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glFlush();
    }

}
