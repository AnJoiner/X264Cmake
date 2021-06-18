package com.coder.x264cmake.module.filter;


import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.coder.x264cmake.utils.MatrixUtils;
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
            "{\n"
            + "    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n"
            +"}";

    // 2d片元着色器
    public static final String Tex2D_FRAGMENT_SHADER = ""
            + "precision mediump float;\n"
            + "uniform sampler2D inputImageTexture;\n"
            + "varying vec2 textureCoordinate;\n"
            + "void main() {\n"
//            + "  vec4 tc = texture2D(inputImageTexture, textureCoordinate);\n"
//            + "  float gray = tc.r * 0.299 + tc.g * 0.587 + tc.b * 0.114;\n"
//            + "  gl_FragColor = vec4(gray, gray, gray, 1.0);\n"
//            + "  gl_FragColor = vec4(1.0-tc.r, 1.0-tc.g, 1.0-tc.b, 1.0);\n"
            + "  gl_FragColor = texture2D( inputImageTexture, textureCoordinate );\n"
            + "}";



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


    private final float texCoord2D[] = {
            1.0f, 0.0f,     // 0 right bottom
            0.0f, 0.0f,     // 1 left  bottom
            1.0f, 1.0f,     // 2 right top
            0.0f, 1.0f      // 3 left  top
    };

    // gl程序id
    protected int glProgramOES;
    protected int glProgram2D;
    // 顶点着色器位置
    protected int glAttribPositionOES;
    protected int glAttribPosition2D;
    // 顶点着色器传入材质坐标
    protected int glAttribTexcoordOES;
    protected int glAttribTexcoord2D;
    // 矩阵变换
    protected int glUniformMatrixOES;
    protected int glUniformMatrix2D;
    // 片元着色器纹理
    protected int glUniformTextureOES;
    protected int glUniformTexture2D;

    private SurfaceTexture surfaceTexture;
    // 分配缓存空间
    private FloatBuffer vert, texOES, tex2D;
    // 纹理id
    private int[] textures;
    // 变换操作
    private float[] matrix = new float[16];
    // 离屏渲染
    private int[] texFBO = {0},texDraw={0};
    private int[] FBO = {0}, RBO = {0};
    protected int mFBOWidth = -1, mFBOHeight = -1;

    public GLImageFilter() {
        vert = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vert.put(vertices);
        vert.position(0);

        texOES = ByteBuffer.allocateDirect(texCoordOES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        texOES.put(texCoordOES);
        texOES.position(0);

        tex2D = ByteBuffer.allocateDirect(texCoord2D.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        tex2D.put(texCoord2D);
        tex2D.position(0);
    }

    public SurfaceTexture createSurfaceTexture() {
        createTextureOES();
        surfaceTexture = new SurfaceTexture(textures[0]);
        return surfaceTexture;
    }


    public void createFrameBuffer(int width, int height) {
        createFBO(width, height);
    }


    public void setUp() {
        glProgramOES = OpenGlUtils.loadProgram(VERTEX_SHADER, TexOES_FRAGMENT_SHADER);
        boolean isValidate = OpenGlUtils.validateProgram(glProgramOES);
        if (isValidate) {
            glAttribPositionOES = GLES20.glGetAttribLocation(glProgramOES, VERTEX_POSITION);
            glAttribTexcoordOES = GLES20.glGetAttribLocation(glProgramOES, VERTEX_TEXCOORD);
            glUniformTextureOES = GLES20.glGetUniformLocation(glProgramOES, FRAG_UNIFORM_TEX);
            glUniformMatrixOES = GLES20.glGetUniformLocation(glProgramOES, VERTEX_UNIFORM_MAT);
        }
        glProgram2D = OpenGlUtils.loadProgram(VERTEX_SHADER, Tex2D_FRAGMENT_SHADER);
        isValidate = OpenGlUtils.validateProgram(glProgram2D);
        if (isValidate) {
            glAttribPosition2D = GLES20.glGetAttribLocation(glProgram2D, VERTEX_POSITION);
            glAttribTexcoord2D = GLES20.glGetAttribLocation(glProgram2D, VERTEX_TEXCOORD);
            glUniformTexture2D = GLES20.glGetUniformLocation(glProgram2D, FRAG_UNIFORM_TEX);
            glUniformMatrix2D = GLES20.glGetUniformLocation(glProgram2D, VERTEX_UNIFORM_MAT);
        }
    }


    public void onDestroy() {
        deleteSurfaceTexture();
        GLES20.glDeleteProgram(glProgramOES);
        deleteRBO();
        deleteFBO();
        GLES20.glDeleteProgram(glProgram2D);
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
    private void createTextureOES() {
        textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NONE);
    }


    private void createTexture2D(int width, int height) {
        GLES20.glGenTextures(1, texDraw, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texDraw[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        // 边缘重复像素
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        // 缩小和放大时使用临近插值过滤
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE);
    }


    private void deleteFBO() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);
        GLES20.glDeleteFramebuffers(1, FBO, 0);
    }

    private void deleteRBO() {
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, GLES20.GL_NONE);
        GLES20.glDeleteFramebuffers(1, RBO, 0);
    }


    public void drawTexture() {
        Matrix.setIdentityM(matrix, 0);
        // 使用中间自定义fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, FBO[0]);
        drawOES();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        // 后置摄像头
//        MatrixUtils.rotate(matrix,90);
        // 前置摄像头
        MatrixUtils.flip(matrix,true,false);
        MatrixUtils.rotate(matrix,270);
        drawTexture2D();
    }

    /**
     * 绘制纹理
     */
    public void drawOES() {
        GLES20.glUseProgram(glProgramOES);
        // 矩阵变换
        GLES20.glUniformMatrix4fv(glUniformMatrixOES, 1, false, matrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        GLES20.glUniform1i(glUniformTextureOES, 0);


        vert.clear();
        vert.put(vertices).position(0);
        GLES20.glVertexAttribPointer(glAttribPositionOES, 2, GLES20.GL_FLOAT, false, 4 * 2, vert);
        GLES20.glEnableVertexAttribArray(glAttribPositionOES);

        texOES.clear();
        texOES.put(texCoordOES).position(0);
        GLES20.glVertexAttribPointer(glAttribTexcoordOES, 2, GLES20.GL_FLOAT, false, 4 * 2, texOES);
        GLES20.glEnableVertexAttribArray(glAttribTexcoordOES);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(glAttribPositionOES);
        GLES20.glDisableVertexAttribArray(glAttribTexcoordOES);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NONE);
    }

    /**
     * 创建离屏渲染 fbo
     */
    private void createFBO(int width, int height) {
//        createTexture2D(width, height);
        // 创建2d纹理用于FBO附着
        GLES20.glGenTextures(1, texFBO, 0);
        // 绑定2d纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texFBO[0]);
        // 申请2d纹理的存储空间
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        // 设置2d参数
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // 创建FBO，帧缓冲对象
        GLES20.glGenFramebuffers(1, FBO, 0);
        // 绑定FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, FBO[0]);

        // 创建RBO，渲染缓冲对象
        GLES20.glGenRenderbuffers(1, RBO, 0);
        // 绑定RBO
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, RBO[0]);
        // 申请渲染数据存储空间
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
        // 将2d纹理附着到FBO上
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texFBO[0], 0);
        // 将渲染缓冲附着到FBO上
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, RBO[0]);


        int fboStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (fboStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("initFBO failed, status:" + fboStatus);
        }
        // 切换到window上默认FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        mFBOWidth = width;
        mFBOHeight = height;
    }


    public void drawTexture2D() {
        // use 2d draw
        GLES20.glUseProgram(glProgram2D);
        // extra deal
        GLES20.glUniformMatrix4fv(glUniformMatrix2D, 1, false, matrix, 0);

        // bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texFBO[0]);
        GLES20.glUniform1i(glUniformTexture2D, 0);

//        vert.clear();
//        vert.put(vertices).position(0);
        GLES20.glVertexAttribPointer(glAttribPosition2D, 2, GLES20.GL_FLOAT, false, 4 * 2, vert);
        GLES20.glEnableVertexAttribArray(glAttribPosition2D);

//        tex2D.clear();
//        tex2D.put(texCoord2D).position(0);
        GLES20.glVertexAttribPointer(glAttribTexcoord2D, 2, GLES20.GL_FLOAT, false, 4 * 2, tex2D);
        GLES20.glEnableVertexAttribArray(glAttribTexcoord2D);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(glAttribPosition2D);
        GLES20.glDisableVertexAttribArray(glAttribTexcoord2D);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE);
    }
}
