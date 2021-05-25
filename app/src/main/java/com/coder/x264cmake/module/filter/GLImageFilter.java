package com.coder.x264cmake.module.filter;


import android.opengl.GLES20;

import com.coder.x264cmake.utils.OpenGlUtils;

import java.nio.FloatBuffer;
import java.util.LinkedList;

public class GLImageFilter {

    public static final String VERTEX_SHADER = "" +
            "attribute vec4 aPosition; //顶点坐标\n" +
            "attribute vec2 aTexCoord; //材质顶点坐标\n" +
            "varying vec2 vTexCoord;   //输出的材质坐标\n" +
            "void main(){\n" +
            "    vTexCoord = vec2(aTexCoord.x,1.0-aTexCoord.y);\n" +
            "    gl_Position = aPosition;\n" +
            "}";
    public static final String FRAGMENT_SHADER = "" +
            "precision mediump float;    //精度\n" +
            "varying vec2 vTexCoord;     //顶点着色器传递的坐标\n" +
            "uniform sampler2D yTexture; //输入的材质（不透明灰度，单像素）\n" +
            "uniform sampler2D uTexture;\n" +
            "uniform sampler2D vTexture;\n" +
            "void main(){\n" +
            "    vec3 yuv;\n" +
            "    vec3 rgb;\n" +
            "    yuv.r = texture2D(yTexture,vTexCoord).r;\n" +
            "    yuv.g = texture2D(uTexture,vTexCoord).r - 0.5;\n" +
            "    yuv.b = texture2D(vTexture,vTexCoord).r - 0.5;\n" +
            "    rgb = mat3(1.0,     1.0,    1.0,\n" +
            "               0.0,-0.39465,2.03211,\n" +
            "               1.13983,-0.58060,0.0)*yuv;\n" +
            "    //输出像素颜色\n" +
            "    gl_FragColor = vec4(rgb,1.0);\n" +
            "}";


    public static final String NO_FILTER_VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "}";
    public static final String NO_FILTER_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";
    private final LinkedList<Runnable> mRunnables;
    private final String vertexShader;
    private final String fragmentShader;
    private int glProgramId;
    private int glAttribPosition;
    private int glUniformTexture;
    private int glAttribTextureCoordinate;
    private int outputWidth;
    private int outputHeight;
    private boolean isInitialized;

    public GLImageFilter() {
        this(NO_FILTER_VERTEX_SHADER,NO_FILTER_FRAGMENT_SHADER);
    }

    public GLImageFilter(String vertexShader, String fragmentShader) {
        mRunnables = new LinkedList<>();
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
    }

    private void init() {
        onInit();
        onInitialized();
    }


    public void onInit() {
        glProgramId = OpenGlUtils.loadProgram(vertexShader, fragmentShader);
        glAttribPosition = GLES20.glGetAttribLocation(glProgramId, "position");
        glUniformTexture = GLES20.glGetUniformLocation(glProgramId, "inputImageTexture");
        glAttribTextureCoordinate = GLES20.glGetAttribLocation(glProgramId, "inputTextureCoordinate");
        isInitialized = true;
    }

    public void onInitialized() {
    }

    public void ifNeedInit() {
        if (!isInitialized) init();
    }

    public final void destroy() {
        isInitialized = false;
        GLES20.glDeleteProgram(glProgramId);
        onDestroy();
    }

    public void onDestroy() {

    }

    /**
     * opengl绘制数据
     * @param textureId 纹理id
     * @param cubeBuffer 顶点
     * @param textureBuffer 片元
     */
    public void glDraw(int textureId, FloatBuffer cubeBuffer, FloatBuffer textureBuffer){
        GLES20.glUseProgram(glProgramId);
        if (!isInitialized) {
            return;
        }
        cubeBuffer.position(0);
        GLES20.glVertexAttribPointer(glAttribPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer);
        GLES20.glEnableVertexAttribArray(glAttribPosition);

        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(glAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
                textureBuffer);
        GLES20.glEnableVertexAttribArray(glAttribTextureCoordinate);

        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(glUniformTexture, 0);
        }

        onDrawArraysPre();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(glAttribPosition);
        GLES20.glDisableVertexAttribArray(glAttribTextureCoordinate);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }


    protected void onDrawArraysPre() {

    }

    protected void runPendingOnDrawTasks() {
        synchronized (mRunnables) {
            while (!mRunnables.isEmpty()) {
                mRunnables.removeFirst().run();
            }
        }
    }


    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunnables) {
            mRunnables.addLast(runnable);
        }
    }
}
