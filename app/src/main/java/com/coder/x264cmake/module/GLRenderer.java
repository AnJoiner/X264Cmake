package com.coder.x264cmake.module;


import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import com.coder.x264cmake.module.filter.GLTestFilter;
import com.coder.x264cmake.utils.OpenGlUtils;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {
    public static String A_POSITION = "a_Position";
    public static String U_COLOR = "u_Color";

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int BITS_PER_FLOAT = 4;

    private float[] tableVertices = {
            0f,0f,
            0f,14f,
            9f,14f,
            9f,0f
    };

//    private float[] tableVerticesWithTriangles = {
//            0f,0f,
//            9f,14f,
//            0f,14f,
//
//            0f,0f,
//            9f,0f,
//            9f,14f,
//
//            0f,7f,
//            9f,7f,
//
//            4.5f,2f,
//            4.5f,12f
//    };

    private float[] tableVerticesWithTriangles = {
            -0.5f,-0.5f,
            0.5f,0.5f,
            -0.5f,0.5f,

            -0.5f,-0.5f,
            0.5f,-0.5f,
            0.5f,0.5f,

            -0.5f,0f,
            0.5f,0f,

            0f,-0.25f,
            0f,0.25f
    };

    private FloatBuffer vertexData;

    private int program;
    private int uColorLocation;
    private int aPositionLocation;

    public GLRenderer() {
        vertexData = ByteBuffer.allocateDirect(tableVerticesWithTriangles.length * BITS_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        vertexData.put(tableVerticesWithTriangles);
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0,0,0,1);

        program = OpenGlUtils.loadProgram(GLTestFilter.VERTEX_TEST_SHADER, GLTestFilter.FRAGMENT_TEST_SHADER);
        if (OpenGlUtils.validateProgram(program)) {
            // 开始使用
            GLES20.glUseProgram(program);
            // 获取定义的位置
            uColorLocation = GLES20.glGetUniformLocation(program, U_COLOR);
            aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
            // 将数据定位到开头
            vertexData.position(0);
            // 通知opengl去找数据
            GLES20.glVertexAttribPointer(aPositionLocation,POSITION_COMPONENT_COUNT,GLES20.GL_FLOAT
            ,false,0,vertexData);
            GLES20.glEnableVertexAttribArray(aPositionLocation);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUniform4f(uColorLocation,1.0f,1.0f,1.0f,1.0f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6);

        GLES20.glUniform4f(uColorLocation,1.0f,0.0f,0.0f,1.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES,6,2);

        GLES20.glUniform4f(uColorLocation,0.0f,0.0f,1.0f,1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS,8,1);

        GLES20.glUniform4f(uColorLocation,1.0f,0.0f,0.0f,1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS,9,1);
    }
}
