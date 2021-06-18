package com.coder.x264cmake.module.filter;

import android.opengl.GLES20;

import com.coder.x264cmake.utils.LogUtils;
import com.coder.x264cmake.utils.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

/**
 * 滤镜基础类
 *
 * @author anjoiner
 */
public class GLImageBaseFilter {
    // 定点着色器
    protected static final String VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec2 inputTextureCoordinate;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform mat4 u_matrix;\n" +
            "void main()\n" +
            "{\n" +
            "    textureCoordinate = inputTextureCoordinate;\n" +
            "    gl_Position = u_matrix * position;\n" +
            "}";

    // 片元着色器
    protected static final String FRAGMENT_SHADER = ""
            + "precision mediump float;\n"
            + "uniform sampler2D inputImageTexture;\n"
            + "varying vec2 textureCoordinate;\n"
            + "void main() {\n"
            + "  gl_FragColor = texture2D( inputImageTexture, textureCoordinate );\n"
            + "}";

    // gl 相关变量名称
    protected static final String VERTEX_POSITION = "position";
    protected static final String VERTEX_TEXCOORD = "inputTextureCoordinate";
    protected static final String VERTEX_UNIFORM_MAT = "u_matrix";
    protected static final String FRAG_UNIFORM_TEX = "inputImageTexture";

    // 缓存空间
    private FloatBuffer mVertexFloatBuffer;
    private FloatBuffer mTextureFloatBuffer;
    // 纹理字符串
    protected String mVertexShader;
    protected String mFragmentShader;
    // 顶点坐标数量
    protected final int mCoordinateCount = 4;
    // GL程序id
    protected int mGLProgramId;
    // GL顶点着色器位置
    protected int mGLVertexPosition;
    // GL纹理坐标
    protected int mGLTextureCoordinate;
    // GL矩阵
    protected int mGLUniformMatrix;
    // GL纹理
    protected int mGLUniformTexture;

    // 渲染的Image的宽高
    protected int mImageWidth;
    protected int mImageHeight;

    // FBO的宽高
    protected int mFBOWidth = -1;
    protected int mFBOHeight = -1;

    // 显示输出的宽高
    protected int mDisplayWidth;
    protected int mDisplayHeight;

    // 帧缓冲对象id和纹理id
    protected int[] mFrameBuffer = {0};
    protected int[] mFrameBufferTexture = {0};
    // 渲染缓存id
    protected int[] mRenderBuffer = {0};
    // 是否初始化GL
    protected boolean isInitializedGL;

    // 矩阵
    private float[] matrix;

    // 顶点坐标
    protected float vertices[] = {
            -1.0f, -1.0f,   // 左下
            1.0f, -1.0f,    // 右下
            -1.0f, 1.0f,    // 左上
            1.0f, 1.0f      // 右上
    };

    // 纹理坐标，以左下角 (0,0) 作为坐标远点
    protected final float texCoords[] = {
            1.0f, 0.0f,     // 右下
            0.0f, 0.0f,     // 左下
            1.0f, 1.0f,     // 右上
            0.0f, 1.0f      // 左上
    };
//    protected final float texCoords[] = {
//            0.0f, 0.0f,     // 左下
//            1.0f, 0.0f,     // 右下
//            0.0f, 1.0f,     // 左上
//            1.0f, 1.0f      // 右上
//    };

    public GLImageBaseFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageBaseFilter(String vertexShader, String fragmentShader) {
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;

        initFloatBuffer();
        initGLProgram();
    }


    /**
     * 初始化顶点缓冲空间
     */
    public void initFloatBuffer() {
        mVertexFloatBuffer = ByteBuffer
                .allocateDirect(vertices.length * mCoordinateCount)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexFloatBuffer.put(vertices);
        mVertexFloatBuffer.position(0);


        mTextureFloatBuffer = ByteBuffer
                .allocateDirect(texCoords.length * mCoordinateCount)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTextureFloatBuffer.put(texCoords);
        mTextureFloatBuffer.position(0);
    }

    /**
     * 初始化openGL程序
     */
    public void initGLProgram() {
        mGLProgramId = OpenGlUtils.loadProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        boolean isValidate = OpenGlUtils.validateProgram(mGLProgramId);
        if (isValidate) {
            mGLVertexPosition = GLES20.glGetAttribLocation(mGLProgramId, VERTEX_POSITION);
            mGLTextureCoordinate = GLES20.glGetAttribLocation(mGLProgramId, VERTEX_TEXCOORD);
            mGLUniformMatrix = GLES20.glGetUniformLocation(mGLProgramId, VERTEX_UNIFORM_MAT);
            mGLUniformTexture = GLES20.glGetUniformLocation(mGLProgramId, FRAG_UNIFORM_TEX);

            isInitializedGL = true;
        } else {
            mGLVertexPosition = OpenGlUtils.NO_GL;
            mGLTextureCoordinate = OpenGlUtils.NO_GL;
            mGLUniformMatrix = OpenGlUtils.NO_GL;
            mGLUniformTexture = OpenGlUtils.NO_GL;

            isInitializedGL = false;
        }
    }

    public void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }

    public float[] getMatrix() {
        return matrix;
    }

    /**
     * Surface发生变化
     */
    public void onSurfaceChanged(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
    }


    /**
     * 显示视图发生变化
     */
    public void onDisplaySizeChanged(int width, int height) {
        mDisplayWidth = width;
        mDisplayHeight = height;
    }

    /**
     * 滤镜渲染
     *
     * @param textureId          纹理id
     * @param vertexFloatBuffer  顶点坐标缓冲区
     * @param textureFloatBuffer 纹理坐标缓冲区
     */
    public void onDrawFrame(int textureId, FloatBuffer vertexFloatBuffer, FloatBuffer textureFloatBuffer) {
        // 未初始GL程序
        if (!isInitializedGL || textureId == OpenGlUtils.NO_TEXTURE) {
            LogUtils.e("Failed to draw frame.");
            return;
        }
        // 设置窗口大小
        GLES20.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
        onClear();

        drawFrame(textureId,vertexFloatBuffer,textureFloatBuffer);
    }


    /**
     * 渲染全流程
     *
     * @param textureId 纹理id
     */
    protected void drawFrame(int textureId, FloatBuffer vertexFloatBuffer, FloatBuffer textureFloatBuffer) {
        onUseProgram();
        onPreExtra();
        onBindTexture(textureId);
        onDraw(vertexFloatBuffer, textureFloatBuffer);
        onPostExtra();
        onUnBindTexture();
    }

    /**
     * 清除画布
     */
    protected void onClear() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    /**
     * 使用GL程序
     */
    protected void onUseProgram() {
        GLES20.glUseProgram(mGLProgramId);
    }

    /**
     * 在绘制之前的预处理
     */
    protected void onPreExtra() {
        GLES20.glUniformMatrix4fv(mGLUniformMatrix, 1, false, matrix, 0);
    }

    /**
     * 绑定纹理
     */
    protected void onBindTexture(int textureId) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(mGLUniformTexture, 0);
    }

    /**
     * 启用顶点坐标和纹理坐标进行绘制
     */
    protected void onDraw(FloatBuffer vertexFloatBuffer, FloatBuffer textureFloatBuffer) {
        vertexFloatBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLVertexPosition, 2, GLES20.GL_FLOAT, false, mCoordinateCount * 2, vertexFloatBuffer);
        GLES20.glEnableVertexAttribArray(mGLVertexPosition);

        textureFloatBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLTextureCoordinate, 2, GLES20.GL_FLOAT, false, mCoordinateCount * 2, textureFloatBuffer);
        GLES20.glEnableVertexAttribArray(mGLTextureCoordinate);

        onPreDraw();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        onPostDraw();

        GLES20.glDisableVertexAttribArray(mGLVertexPosition);
        GLES20.glDisableVertexAttribArray(mGLTextureCoordinate);
    }

    /**
     * 在绘制之前
     */
    protected void onPreDraw() {

    }

    /**
     * 在绘制之后
     */
    protected void onPostDraw() {

    }

    /**
     * 在绘制之后处理
     */
    protected void onPostExtra() {

    }

    /**
     * 解除纹理绑定
     */
    protected void onUnBindTexture() {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE);
    }
}
