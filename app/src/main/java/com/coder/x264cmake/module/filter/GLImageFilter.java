package com.coder.x264cmake.module.filter;


import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
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
            "uniform mat4 u_matrix;\n" +
            "void main()\n" +
            "{\n" +
            "    textureCoordinate = inputTextureCoordinate;\n" +
            "    gl_Position = u_matrix * position;\n" +
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

    public static final String VERTEX_UNIFORM_MAT = "u_matrix";
    public static final String VERTEX_POSITION = "position";
    public static final String VERTEX_TEXCOORD = "inputTextureCoordinate";
    public static final String FRAG_UNIFORM_TEX = "inputImageTexture";

    // 材质顶点坐标
    private final float vertices[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f};
    // oes纹理坐标
    private final float texCoordOES[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f};

    // gl程序id
    protected int glProgramId;
    // 顶点着色器位置
    protected int glAttribPosition;
    // 顶点着色器传入材质坐标
    protected int glAttribTexcoord;
    // 矩阵变换
    protected int glUniformMatrix;
    // 片元着色器纹理
    protected int glUniformTexture;
    private SurfaceTexture surfaceTexture;
    // 分配缓存空间
    private FloatBuffer vert, texOES;
    // 纹理id
    private int[] textures;
    // 变换操作
    private float[] matrix = new float[16];
    // 离屏渲染
    private int[] texFBO = {0}, texDraw = {0};
    private int[] FBO = {0};
    protected int mFBOWidth = -1, mFBOHeight = -1;

    public GLImageFilter() {
        vert = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vert.put(vertices);
        vert.position(0);

        texOES = ByteBuffer.allocateDirect(texCoordOES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        texOES.put(texCoordOES);
        texOES.position(0);
    }

    public SurfaceTexture createSurfaceTexture() {
        createTextureOES();
//        createTexture2D();
        surfaceTexture = new SurfaceTexture(textures[0]);
        return surfaceTexture;
    }


    public void setUp(){
        glProgramId = OpenGlUtils.loadProgram(VERTEX_SHADER, TexOES_FRAGMENT_SHADER);
        boolean isValidate = OpenGlUtils.validateProgram(glProgramId);
        if (isValidate) {
            glAttribPosition = GLES20.glGetAttribLocation(glProgramId, VERTEX_POSITION);
            glAttribTexcoord = GLES20.glGetAttribLocation(glProgramId, VERTEX_TEXCOORD);
            glUniformTexture = GLES20.glGetUniformLocation(glProgramId, FRAG_UNIFORM_TEX);
            glUniformMatrix = GLES20.glGetUniformLocation(glProgramId, VERTEX_UNIFORM_MAT);
        }
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

    public void rotate(){
        Matrix.setIdentityM(matrix, 0);
        // 对称左右翻转
        Matrix.rotateM(matrix,0,180F,0F,1F,0F);
        // 旋转270
        Matrix.rotateM(matrix,0,270,0F,0F,1F);
    }
    /**
     * 创建oes纹理id
     *
     * @return 纹理id
     */
    private void createTextureOES() {
        textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
    }

    private void createTexture2D(){
        textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    private void deleteFBO(){
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);
        GLES20.glDeleteFramebuffers(1, FBO, 0);
    }
    /**
     * 创建离屏渲染 fbo
     */
    private void createFBO(int width,int height){
        deleteFBO();
//        GLES20.glGenTextures(1, texDraw, 0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texDraw[0]);
//        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // 创建2d纹理用于FBO附着
        GLES20.glGenTextures(1, texFBO, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texFBO[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // 创建FBO
        GLES20.glGenFramebuffers(1, FBO, 0);
        // 绑定FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, FBO[0]);
        // 将纹理连接到FBO附着
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texFBO[0], 0);

        int fboStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (fboStatus != GLES20.GL_FRAMEBUFFER_COMPLETE){
            LogUtils.e("initFBO failed, status: " + fboStatus);
        }
        // 解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE);
        // 解绑附着
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);

        mFBOWidth  = width;
        mFBOHeight = height;
    }

    /**
     * 绘制纹理
     */
    public void drawTexture() {
        GLES20.glUseProgram(glProgramId);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, glUniformTexture);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glUniformTexture);
        GLES20.glUniform1i(glUniformTexture, 0);
        // 矩阵变换
        GLES20.glUniformMatrix4fv(glUniformMatrix, 1, false, matrix, 0);

        vert.clear();
        vert.put(vertices).position(0);
        GLES20.glVertexAttribPointer(glAttribPosition, 2, GLES20.GL_FLOAT, false, 4*2, vert);
        GLES20.glEnableVertexAttribArray(glAttribPosition);

        texOES.clear();
        texOES.put(texCoordOES).position(0);
        GLES20.glVertexAttribPointer(glAttribTexcoord, 2, GLES20.GL_FLOAT, false, 4*2, texOES);
        GLES20.glEnableVertexAttribArray(glAttribTexcoord);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(glAttribPosition);
        GLES20.glDisableVertexAttribArray(glAttribTexcoord);

//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NONE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE);
    }

}
