package com.coder.x264cmake.module.filter;


import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import javax.microedition.khronos.opengles.GL10;

public class GLImageFilter {

    // 定点着色器
    public static final String VERTEX_SHADER = "" +
            "attribute vec2 position;\n" +
            "attribute vec2 inputTextureCoordinate;\n" +
            "varying vec2 textureCoordinate;\n" +
            "void main()\n" +
            "{\n" +
            "    textureCoordinate = inputTextureCoordinate;\n" +
            "    gl_Position = position;\n" +
            "}";

    // oes片元着色器
    public static final String TexOES_FRAGMENT_SHADER = ""+
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform samplerExternalOES vTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D( vTexture, textureCoordinate );\n" +
            "}\n";

    // 2d片元着色器
    public static final String Tex2D_FRAGMENT_SHADER = ""
            + "precision mediump float;\n"
            + "uniform sampler2D vTexture;\n"
            + "varying vec2 textureCoordinate;\n"
            + "void main() {\n"
            + "  gl_FragColor = texture2D( vTexture, textureCoordinate );\n" + "}";


    // 材质顶点坐标
    private final float vertices[] = {
            -1, -1,
            -1,  1,
            1, -1,
            1,  1 };

    // oes纹理坐标
    private final float texCoordOES[] = {
            0,  1,
            0,  0,
            1,  1,
            1,  0 };

    // tex 2d纹理坐标
    private final float texCoord2D[] = {
            0,  0,
            0,  1,
            1,  0,
            1,  1 };

    /**
     * 创建oes纹理id
     *
     * @return 纹理id
     */
    private int createTextureOesID() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }



}
