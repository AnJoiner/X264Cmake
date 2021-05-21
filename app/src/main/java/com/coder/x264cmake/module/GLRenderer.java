package com.coder.x264cmake.module;


import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.coder.x264cmake.module.filter.GLImageFilter;
import com.coder.x264cmake.utils.OpenGlUtils;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class GLRenderer implements GLSurfaceView.Renderer {
    public static String A_POSITION = "aPosition";
    public static String A_TEX_COORD = "aTexCoord";

    public static String Y_TEXTURE = "yTexture";
    public static String U_TEXTURE = "uTexture";
    public static String V_TEXTURE = "vTexture";

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int BITS_PER_FLOAT = 4;

    //加入三维顶点数据 两个三角形组成正方形
    private final float vers[] = {
            1.0f, -1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f
    };

    //加入材质坐标数据
    private final float txts[] = {
            1.0f, 0.0f, //右下
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
    };


    private final int texts[] = {
            0, 0, 0
    };

    private FloatBuffer vertexData;
    private FloatBuffer txtCoordData;
    private IntBuffer textData;

    private int program;
    private int aTxtCoordLocation;
    private int aPositionLocation;

    SurfaceTexture surfaceTexture;

    private int width = 720;
    private int height = 1280;

    public GLRenderer() {
        vertexData = ByteBuffer.allocateDirect(vers.length * BITS_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        vertexData.put(vers);


        txtCoordData = ByteBuffer.allocateDirect(txts.length * BITS_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        txtCoordData.put(txts);


        textData = ByteBuffer.allocateDirect(texts.length * BITS_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 1);

        program = OpenGlUtils.loadProgram(GLImageFilter.VERTEX_SHADER, GLImageFilter.FRAGMENT_SHADER);
        if (OpenGlUtils.validateProgram(program)) {
            // 开始使用
            GLES20.glUseProgram(program);

            // 获取定义的位置
            aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
            // 通知opengl去找数据
            GLES20.glEnableVertexAttribArray(aPositionLocation);
            // 将数据定位到开头
            vertexData.position(0);
            GLES20.glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GLES20.GL_FLOAT,
                    false, 12, vertexData);

            aTxtCoordLocation = GLES20.glGetAttribLocation(program, A_TEX_COORD);
            GLES20.glEnableVertexAttribArray(aTxtCoordLocation);
            txtCoordData.position(0);
            GLES20.glVertexAttribPointer(aTxtCoordLocation, 2, GLES20.GL_FLOAT, false, 8, txtCoordData);

            //材质纹理初始化
            //设置纹理层
            GLES20.glUniform1i(GLES20.glGetUniformLocation(program, Y_TEXTURE), 0); //对于纹理第1层
            GLES20.glUniform1i(GLES20.glGetUniformLocation(program, U_TEXTURE), 1); //对于纹理第2层
            GLES20.glUniform1i(GLES20.glGetUniformLocation(program, V_TEXTURE), 2); //对于纹理第3层

            //创建opengl纹理
            int[] texts = new int[3];
            GLES20.glGenTextures(3, texts,0);

//            surfaceTexture = new SurfaceTexture(texts[0]);

            //设置纹理属性
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texts[0]);
            //过滤器
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //设置纹理的格式和大小
            GLES20. glTexImage2D(GLES20.GL_TEXTURE_2D,
                    0,           //细节基本 0默认
                    GLES20.GL_LUMINANCE,//gpu内部格式 亮度，灰度图
                    width,height, //拉升到全屏
                    0,             //边框
                    GLES20.GL_LUMINANCE,//数据的像素格式 亮度，灰度图 要与上面一致
                    GLES20.GL_UNSIGNED_BYTE, //像素的数据类型
                    null                    //纹理的数据
            );

            //设置纹理属性
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texts[1]);
            //过滤器
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //设置纹理的格式和大小
            GLES20. glTexImage2D(GLES20.GL_TEXTURE_2D,
                    0,           //细节基本 0默认
                    GLES20.GL_LUMINANCE,//gpu内部格式 亮度，灰度图
                    width/2,height/2, //拉升到全屏
                    0,             //边框
                    GLES20.GL_LUMINANCE,//数据的像素格式 亮度，灰度图 要与上面一致
                    GLES20.GL_UNSIGNED_BYTE, //像素的数据类型
                    null                    //纹理的数据
            );


            //设置纹理属性
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texts[2]);
            //过滤器
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //设置纹理的格式和大小
            GLES20. glTexImage2D(GLES20.GL_TEXTURE_2D,
                    0,           //细节基本 0默认
                    GLES20.GL_LUMINANCE,//gpu内部格式 亮度，灰度图
                    width/2,height/2, //拉升到全屏
                    0,             //边框
                    GLES20.GL_LUMINANCE,//数据的像素格式 亮度，灰度图 要与上面一致
                    GLES20.GL_UNSIGNED_BYTE, //像素的数据类型
                    null                    //纹理的数据
            );
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//        GLES20.glUniform4f(uColorLocation,1.0f,1.0f,1.0f,1.0f);
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6);
//
//        GLES20.glUniform4f(uColorLocation,1.0f,0.0f,0.0f,1.0f);
//        GLES20.glDrawArrays(GLES20.GL_LINES,6,2);
//
//        GLES20.glUniform4f(uColorLocation,0.0f,0.0f,1.0f,1.0f);
//        GLES20.glDrawArrays(GLES20.GL_POINTS,8,1);
//
//        GLES20.glUniform4f(uColorLocation,1.0f,0.0f,0.0f,1.0f);
//        GLES20.glDrawArrays(GLES20.GL_POINTS,9,1);
    }


    private int createTextureID() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }
}
