package com.coder.x264cmake.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class TextureCoordinateUtils {

    // 顶点坐标数量
    public static final int COORDINATE_COUNT = 4;

    public static final int SIZE_OF_FLOAT = 4;

    // 顶点坐标
    public static final float vertices[] = {
            -1.0f, -1.0f,   // 左下
            1.0f, -1.0f,    // 右下
            -1.0f, 1.0f,    // 左上
            1.0f, 1.0f      // 右上
    };


    // 纹理坐标，以左下角 (0,0) 作为坐标原点
    public static final float texCoords[] = {
            0.0f, 0.0f,     // 左下
            1.0f, 0.0f,     // 右下
            0.0f, 1.0f,     // 左上
            1.0f, 1.0f      // 右上
    };


    public static final float TEXTURE_NO_ROTATION[] = {
            0.0f, 0.0f,     // 左下
            1.0f, 0.0f,     // 右下
            0.0f, 1.0f,     // 左上
            1.0f, 1.0f,     // 右上
    };

    public static final float TEXTURE_ROTATED_90[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    public static final float TEXTURE_ROTATED_180[] = {
            1.0f, 1.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };

    public static final float TEXTURE_ROTATED_270[] = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
    };

    public static FloatBuffer createFloatBuffer(float[] coords){
       FloatBuffer floatBuffer = ByteBuffer
                .allocateDirect(coords.length * TextureCoordinateUtils.SIZE_OF_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        floatBuffer.put(coords);
        floatBuffer.position(0);

        return floatBuffer;
    }


}
