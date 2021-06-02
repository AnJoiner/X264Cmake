package com.coder.x264cmake.module;


import android.graphics.SurfaceTexture;
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

    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 1);


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
    }

}
