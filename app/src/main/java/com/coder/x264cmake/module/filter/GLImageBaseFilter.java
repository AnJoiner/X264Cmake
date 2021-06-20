package com.coder.x264cmake.module.filter;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;

import com.coder.x264cmake.utils.LogUtils;
import com.coder.x264cmake.utils.OpenGlUtils;
import com.coder.x264cmake.utils.TextureCoordinateUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;

/**
 * 滤镜基础类
 *
 * @author anjoiner
 */
public class GLImageBaseFilter {
    // 定点着色器
    protected static final String VERTEX_SHADER = "" +
            "attribute vec4 a_position;\n" +
            "attribute vec2 inputTextureCoordinate;\n" +
            "varying vec2 textureCoordinate;\n" +
            "void main()\n" +
            "{\n" +
            "    textureCoordinate = inputTextureCoordinate;\n" +
            "    gl_Position = a_position;\n" +
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
    protected static final String VERTEX_POSITION = "a_position";
    protected static final String VERTEX_TEXCOORD = "inputTextureCoordinate";
    protected static final String VERTEX_UNIFORM_MAT = "u_matrix";
    protected static final String FRAG_UNIFORM_TEX = "inputImageTexture";

    protected final LinkedList<Runnable> mRunOnDraw;
    protected Context mContext;

    // 纹理字符串
    protected String mVertexShader;
    protected String mFragmentShader;
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

    // 显示输出的宽高
    protected int mDisplayWidth;
    protected int mDisplayHeight;

    // FBO的宽高
    protected int mFrameWidth = -1;
    protected int mFrameHeight = -1;

    // 帧缓冲对象id和纹理id
    protected int[] mFrameBuffer;
    protected int[] mFrameBufferTexture;
    // 渲染缓存id
    protected int[] mRenderBuffer;
    // 是否初始化GL
    protected boolean isInitializedGL;


    public GLImageBaseFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageBaseFilter(Context context, String vertexShader, String fragmentShader) {
        mContext = context;
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
        mRunOnDraw = new LinkedList<>();

        initGLProgram();
    }


    /**
     * 初始化openGL程序
     */
    public void initGLProgram() {
        mGLProgramId = OpenGlUtils.loadProgram(mVertexShader, mFragmentShader);
        boolean isValidate = OpenGlUtils.validateProgram(mGLProgramId);
        if (isValidate) {
            mGLVertexPosition = GLES20.glGetAttribLocation(mGLProgramId, VERTEX_POSITION);
            mGLTextureCoordinate = GLES20.glGetAttribLocation(mGLProgramId, VERTEX_TEXCOORD);
            mGLUniformTexture = GLES20.glGetUniformLocation(mGLProgramId, FRAG_UNIFORM_TEX);

            isInitializedGL = true;
        } else {
            mGLVertexPosition = OpenGlUtils.NO_GL;
            mGLTextureCoordinate = OpenGlUtils.NO_GL;
            mGLUniformTexture = OpenGlUtils.NO_GL;

            isInitializedGL = false;
        }

        onInitGLProgram(isValidate);
    }

    protected void onInitGLProgram(boolean isValidate) {

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
     * 创建FBO
     */
    public void onCreateFrameBuffer(int width, int height) {
        // 创建之前如果存在先进行销毁
        if (mFrameBuffer != null && (mFrameWidth != width || mFrameHeight != height)) {
            onDestroyFrameBuffer();
        }
        // 正式开始创建
        if (mFrameBuffer == null || mFrameBufferTexture == null) {
            mFrameWidth = width;
            mFrameHeight = height;
            mFrameBufferTexture = new int[1];
            mFrameBuffer = new int[1];
            mRenderBuffer = new int[1];
            OpenGlUtils.createFrameBuffer(mFrameBufferTexture, mFrameBuffer, mRenderBuffer, width, height);
        }
    }

    /**
     * 滤镜渲染
     *
     * @param textureId          纹理id
     * @param vertexFloatBuffer  顶点坐标缓冲区
     * @param textureFloatBuffer 纹理坐标缓冲区
     * @param isFrameBuffer      是否是帧缓冲渲染
     */
    public int onDrawFrame(int textureId, FloatBuffer vertexFloatBuffer, FloatBuffer textureFloatBuffer, boolean isFrameBuffer) {
        // 未初始GL程序
        if (!isInitializedGL) {
            LogUtils.e("Failed to draw frame. Initialize is failure");
            return OpenGlUtils.NO_TEXTURE;
        }

        if (textureId == OpenGlUtils.NO_TEXTURE){
            LogUtils.e("Failed to draw frame. Texture is error");
            return OpenGlUtils.NO_TEXTURE;
        }
        if (!isFrameBuffer) {
            // 设置窗口大小
            GLES20.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
            onClear();
        }

        // 如果FrameBuffer离屏渲染就需绑定FrameBuffer
        if (isFrameBuffer) {
            onBindFrameBuffer();
        }

        // 绘制
        drawFrame(textureId, vertexFloatBuffer, textureFloatBuffer);

        // 如果FrameBuffer离屏渲染就需解绑FrameBuffer
        if (isFrameBuffer) {
            onUnBindFrameBuffer();
        }

        return isFrameBuffer ? mFrameBufferTexture[0] : textureId;
    }

    /**
     * 销毁
     */
    public void release() {
        if (isInitializedGL) {
            GLES20.glDeleteProgram(mGLProgramId);
            mGLProgramId = OpenGlUtils.NO_GL;
        }
        onDestroyFrameBuffer();
    }

    /**
     * 销毁帧缓冲
     */
    public void onDestroyFrameBuffer() {
        if (!isInitializedGL) return;
        // 销毁帧缓冲纹理
        if (mFrameBufferTexture != null) {
            GLES20.glDeleteTextures(1, mFrameBufferTexture, 0);
            mFrameBufferTexture = null;
        }
        // 销毁渲染缓冲
        if (mRenderBuffer != null) {
            GLES20.glDeleteRenderbuffers(1, mRenderBuffer, 0);
            mRenderBuffer = null;
        }
        // 销毁帧缓冲
        if (mFrameBuffer != null) {
            GLES20.glDeleteFramebuffers(1, mFrameBuffer, 0);
            mFrameBuffer = null;
        }
        // 重置宽高
        mFrameWidth = -1;
        mFrameHeight = -1;
    }

    /**
     * 绑定FBO
     */
    protected void onBindFrameBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffer[0]);
    }

    /**
     * 解绑FBO
     */
    protected void onUnBindFrameBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
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
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
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
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run();
        }
    }

    /**
     * 绑定纹理
     */
    protected void onBindTexture(int textureId) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(getTextureType(), textureId);
        GLES20.glUniform1i(mGLUniformTexture, 0);
    }

    /**
     * 启用顶点坐标和纹理坐标进行绘制
     */
    protected void onDraw(FloatBuffer vertexFloatBuffer, FloatBuffer textureFloatBuffer) {
        vertexFloatBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLVertexPosition, 2, GLES20.GL_FLOAT, false,
                TextureCoordinateUtils.COORDINATE_COUNT * 2, vertexFloatBuffer);
        GLES20.glEnableVertexAttribArray(mGLVertexPosition);

        textureFloatBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLTextureCoordinate, 2, GLES20.GL_FLOAT, false,
                TextureCoordinateUtils.COORDINATE_COUNT * 2, textureFloatBuffer);
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
        GLES20.glBindTexture(getTextureType(), GLES20.GL_NONE);
    }


    /**
     * 常用为Texture 2d 或者 oes
     *
     * @return 纹理类型
     */
    protected int getTextureType() {
        return GLES20.GL_TEXTURE_2D;
    }


    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.addLast(runnable);
        }
    }

    //---------------------------------------Common Method--------------------------------------//

    /**
     * 向GL设置点
     *
     * @param location 位置
     * @param point    点
     */
    public void setPoint(int location, PointF point) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                float[] vec2 = new float[2];
                vec2[0] = point.x;
                vec2[1] = point.y;
                GLES20.glUniform2fv(location, 1, vec2, 0);
            }
        });
    }

    /**
     * 设置int值
     *
     * @param location 位置
     * @param value    值
     */
    public void setInteger(int location, int value) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1i(location, value);
            }
        });
    }

    /**
     * 设置int一维向量
     */
    public void setIntVec(int location, int[] array) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1iv(location, array.length, IntBuffer.wrap(array));
            }
        });
    }

    /**
     * 设置int二维向量
     */
    public void setIntVec2(int location, int[] array) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform2iv(location, array.length, IntBuffer.wrap(array));
            }
        });
    }

    /**
     * 设置int三维向量
     */
    public void setIntVec3(int location, int[] array) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform3iv(location, array.length, IntBuffer.wrap(array));
            }
        });
    }

    /**
     * 设置int四维向量
     */
    public void setIntVec4(int location, int[] array) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform4iv(location, array.length, IntBuffer.wrap(array));
            }
        });
    }

    /**
     * 设置浮点一维向量
     */
    public void setFloatVec(int location, float[] array) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1fv(location, array.length, FloatBuffer.wrap(array));
            }
        });
    }

    /**
     * 设置浮点二维向量
     */
    public void setFloatVec2(int location, float[] array) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform2fv(location, array.length, FloatBuffer.wrap(array));
            }
        });
    }

    /**
     * 设置浮点三维向量
     */
    public void setFloatVec3(int location, float[] array) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform3fv(location, array.length, FloatBuffer.wrap(array));
            }
        });
    }

    /**
     * 设置浮点四维向量
     */
    public void setFloatVec4(int location, float[] array) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform4fv(location, array.length, FloatBuffer.wrap(array));
            }
        });
    }

    /**
     * 设置二阶矩阵
     *
     * @param location 位置
     * @param matrix   矩阵
     */
    public void setUniformMatrix2fv(int location, float[] matrix) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniformMatrix2fv(location, 1, false, matrix, 0);
            }
        });
    }

    /**
     * 设置三阶矩阵
     *
     * @param location 位置
     * @param matrix   矩阵
     */
    public void setUniformMatrix3fv(int location, float[] matrix) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniformMatrix3fv(location, 1, false, matrix, 0);
            }
        });
    }

    /**
     * 设置四阶矩阵
     *
     * @param location 位置
     * @param matrix   矩阵
     */
    public void setUniformMatrix4fv(int location, float[] matrix) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0);
            }
        });
    }
}
