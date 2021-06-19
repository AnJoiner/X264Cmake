package com.coder.x264cmake.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.hardware.Camera.Size;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.IntBuffer;

public class OpenGlUtils {

    public static final int NO_GL = -1;
    public static final int NO_TEXTURE = -1;

    public static int loadTexture(final Bitmap img, final int usedTexId) {
        return loadTexture(img, usedTexId, true);
    }

    public static int loadTexture(final Bitmap img, final int usedTexId, final boolean recycle) {
        int textures[] = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId);
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, img);
            textures[0] = usedTexId;
        }
        if (recycle) {
            img.recycle();
        }
        return textures[0];
    }

    public static int loadTexture(final IntBuffer data, final int width, final int height, final int usedTexId) {
        int textures[] = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                    0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width,
                    height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data);
            textures[0] = usedTexId;
        }
        return textures[0];
    }


    /**
     * 创建FBO
     * @param frameBufferTexture 2d绘制纹理
     * @param frameBuffer 帧缓冲
     * @param renderBuffer 渲染缓冲
     * @param width 帧宽
     * @param height 帧高
     */
    public static void createFrameBuffer(int[] frameBufferTexture, int[] frameBuffer, int[] renderBuffer,int width,  int height){
        GLES20.glGenTextures(1, frameBufferTexture, 0);
        // 绑定2d纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameBufferTexture[0]);
        // 申请2d纹理的存储空间
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        // 设置2d参数
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // 创建FBO，帧缓冲对象
        GLES20.glGenFramebuffers(1, frameBuffer, 0);
        // 绑定FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);

        // 创建RBO，渲染缓冲对象
        GLES20.glGenRenderbuffers(1, renderBuffer, 0);
        // 绑定RBO
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBuffer[0]);
        // 申请渲染数据存储空间
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
        // 将2d纹理附着到FBO上
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, frameBufferTexture[0], 0);
        // 将渲染缓冲附着到FBO上
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderBuffer[0]);

        int fboStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (fboStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            LogUtils.e("initFBO failed, status:" + fboStatus);
            return ;
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public static int loadTextureAsBitmap(final IntBuffer data, final Size size, final int usedTexId) {
        Bitmap bitmap = Bitmap
                .createBitmap(data.array(), size.width, size.height, Config.ARGB_8888);
        return loadTexture(bitmap, usedTexId);
    }


    public static int loadShader(final String strSource, final int iType) {
        int[] compiled = new int[1];
        // 创建着色器
        int iShader = GLES20.glCreateShader(iType);
        // 加载着色器源码
        GLES20.glShaderSource(iShader, strSource);
        // 编译着色器
        GLES20.glCompileShader(iShader);
        // 获取着色器状态
        GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0);

        if (compiled[0] == 0) {
            Log.d("Load Shader Failed", "Compilation\n" + GLES20.glGetShaderInfoLog(iShader));
            GLES20.glDeleteShader(iShader);
            return 0;
        }
        return iShader;
    }

    public static int loadProgram(final String strVSource, final String strFSource) {
        int vertexShaderId;
        int fragmentShaderId;
        int programObjectId;
        int[] link = new int[1];
        vertexShaderId = loadShader(strVSource, GLES20.GL_VERTEX_SHADER);
        if (vertexShaderId == 0) {
            Log.d("Load Program", "Vertex Shader Failed");
            return 0;
        }
        fragmentShaderId = loadShader(strFSource, GLES20.GL_FRAGMENT_SHADER);
        if (fragmentShaderId == 0) {
            Log.d("Load Program", "Fragment Shader Failed");
            return 0;
        }

        programObjectId = GLES20.glCreateProgram();
        if (programObjectId == 0) {
            return 0;
        }
        // 附上着色器
        GLES20.glAttachShader(programObjectId, vertexShaderId);
        GLES20.glAttachShader(programObjectId, fragmentShaderId);
        // 将着色器链接起来
        GLES20.glLinkProgram(programObjectId);

        GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS, link, 0);
        if (link[0] <= 0) {
            Log.d("Load Program", "Linking Failed");
            GLES20.glDeleteProgram(programObjectId);
            return 0;
        }
        // 当链接成功，就可以删除着色器
        GLES20.glDeleteShader(vertexShaderId);
        GLES20.glDeleteShader(fragmentShaderId);

        return programObjectId;
    }

    /**
     * 验证opengl 是否可用
     * @param programObjectId 程序id
     * @return 是否可用
     */
    public static boolean validateProgram(int programObjectId) {
        GLES20.glValidateProgram(programObjectId);

        int[] validateStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0);
        return validateStatus[0] != 0;
    }

}
