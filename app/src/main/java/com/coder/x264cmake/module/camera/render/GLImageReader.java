package com.coder.x264cmake.module.camera.render;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.Looper;

import com.coder.x264cmake.BaseApplication;
import com.coder.x264cmake.module.camera.egl.EGLManager;
import com.coder.x264cmake.module.camera.egl.InputSurface;
import com.coder.x264cmake.module.filter.GLImageBaseFilter;
import com.coder.x264cmake.utils.FileUtils;
import com.coder.x264cmake.utils.TextureCoordinateUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class GLImageReader {
    public static final int READ_MAX_IMAGES = 1;
    // 渲染surface
    private InputSurface mInputSurface;
    // egl管理
    private EGLManager mEGLManager;
    // 读取image数据渲染surface
    private ImageReader mImageReader;
    // 渲染滤镜
    private GLImageBaseFilter mImageFilter;

    // 纹理顶点缓冲
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    public GLImageReader(EGLContext context) {
        mEGLManager = new EGLManager(context,EGLManager.FLAG_RECORDABLE,EGLManager.GL_VERSION_2);
        mVertexBuffer = TextureCoordinateUtils.createFloatBuffer(TextureCoordinateUtils.vertices);
        mTextureBuffer = TextureCoordinateUtils.createFloatBuffer(TextureCoordinateUtils.TEXTURE_NO_ROTATION);
    }

    /**
     * 初始化ImageReader
     * @param width 宽度
     * @param height 高度
     */
    public void init(int width, int height){
        if (mImageReader == null){
            mImageReader = ImageReader.newInstance(width,height, PixelFormat.RGBA_8888,READ_MAX_IMAGES);
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    generateBitmap();
                }
            },null);
            mInputSurface = new InputSurface(mEGLManager,mImageReader.getSurface(),true);
        }

        if (mImageFilter == null) {
            // 创建录制用的滤镜
            mImageFilter = new GLImageBaseFilter(null);
            mImageFilter.onSurfaceChanged(width, height);
            mImageFilter.onDisplaySizeChanged(width, height);
        }
    }

    public void drawFrame(int texture) {
        makeCurrent();
        if (mImageFilter != null) {
            mImageFilter.onDrawFrame(texture, mVertexBuffer, mTextureBuffer,false);
        }
        swapBuffers();
    }

    private void makeCurrent() {
        if (mInputSurface != null) {
            mInputSurface.makeCurrent();
        }
    }

    private void swapBuffers() {
        if (mInputSurface != null) {
            mInputSurface.swapBuffers();
        }
    }

    public void release() {
        makeCurrent();
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
        if (mImageFilter != null) {
            mImageFilter.release();
            mImageFilter = null;
        }
        if (mInputSurface != null) {
            mInputSurface.release();
            mInputSurface = null;
        }
        if (mEGLManager != null) {
            mEGLManager.release();
            mEGLManager = null;
        }
    }

    private void generateBitmap(){
        Image image = mImageReader.acquireNextImage();
        Image.Plane[] planes = image.getPlanes();
        int width = image.getWidth();//设置的宽
        int height = image.getHeight();//设置的高
        int pixelStride = planes[0].getPixelStride();//像素个数，RGBA为4
        int rowStride = planes[0].getRowStride();//这里除pixelStride就是真实宽度
        int rowPadding = rowStride - pixelStride * width;//计算多余宽度

        byte[] data = new byte[rowStride * height];
        ByteBuffer buffer = planes[0].getBuffer();
        buffer.get(data);

        int[] pixelData = new int[width * height];

        int offset = 0;
        int index = 0;
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                int pixel = 0;
                pixel |= (data[offset] & 0xff) << 16;     // R
                pixel |= (data[offset + 1] & 0xff) << 8;  // G
                pixel |= (data[offset + 2] & 0xff);       // B
                pixel |= (data[offset + 3] & 0xff) << 24; // A
                pixelData[index++] = pixel;
                offset += pixelStride;
            }
            offset += rowPadding;
        }
        Bitmap bitmap = Bitmap.createBitmap(pixelData,
                width, height,
                Bitmap.Config.ARGB_8888);

        FileUtils.saveBitmap(BaseApplication.getInstance(),bitmap);
        image.close();
    }
}
