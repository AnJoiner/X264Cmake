package com.coder.x264cmake.module.camera.render;


import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.coder.x264cmake.module.filter.GLImageBaseFilter;
import com.coder.x264cmake.module.filter.GLImageOESFilter;
import com.coder.x264cmake.module.filter.beauty.GLImageBeautyFilter;
import com.coder.x264cmake.module.filter.color.GLImageAmaroFilter;
import com.coder.x264cmake.utils.TextureCoordinateUtils;

import java.nio.FloatBuffer;

public class RendererManager {
    // 上下文
    private Context mContext;
    // 滤镜列表
    private SparseArray<GLImageBaseFilter> mFilterArrays;
    // 纹理顶点缓冲
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    // 用于显示的纹理顶点缓冲
    private FloatBuffer mDisplayVertexBuffer;
    private FloatBuffer mDisplayTextureBuffer;
    // 缩放类型
    private ImageScaleType mImageScaleType = ImageScaleType.CENTER_INSIDE;

    // 渲染的Image的宽高
    protected int mImageWidth;
    protected int mImageHeight;

    // 显示输出的宽高
    protected int mDisplayWidth;
    protected int mDisplayHeight;

    // Controls the alignment between frame size and surface size, 0.5f default is centered.
    private float alignmentHorizontal = 0.5f;
    private float alignmentVertical = 0.5f;

    public RendererManager(Context context) {
        mContext = context;
        mFilterArrays = new SparseArray<>();
        init();
    }


    protected void init(){
        initBuffers();
        initImageFilters();
    }

    /**
     * 初始化顶点缓冲区
     */
    public void initBuffers() {
        releaseBuffer();

        mVertexBuffer = TextureCoordinateUtils.createFloatBuffer(TextureCoordinateUtils.vertices);
        mTextureBuffer = TextureCoordinateUtils.createFloatBuffer(TextureCoordinateUtils.texCoords);

        mDisplayVertexBuffer = TextureCoordinateUtils.createFloatBuffer(TextureCoordinateUtils.vertices);
        mDisplayTextureBuffer = TextureCoordinateUtils.createFloatBuffer(TextureCoordinateUtils.texCoords);
    }

    /**
     * 初始化滤镜
     */
    public void initImageFilters(){
        releaseImageFilters();

        mFilterArrays.put(RendererIndex.OES_INDEX, new GLImageOESFilter(mContext));
        mFilterArrays.put(RendererIndex.BEAUTY_INDEX, new GLImageBeautyFilter(mContext));
        mFilterArrays.put(RendererIndex.COLOR_INDEX, new GLImageAmaroFilter(mContext));
//        mFilterArrays.put(RendererIndex.EFFECT_INDEX, new GLImageEffectSoulStuffFilter(mContext));
        mFilterArrays.put(RendererIndex.PREVIEW_INDEX, new GLImageBaseFilter(mContext));
    }


    /**
     * 释放滤镜
     */
    public void releaseImageFilters(){
        for (int i = 0; i < RendererIndex.RENDERER_COUNT; i++) {
            GLImageBaseFilter filter = mFilterArrays.get(i);
            if (filter!= null){
                filter.release();
            }
        }
        mFilterArrays.clear();
    }

    /**
     * 释放缓冲区
     */
    public void releaseBuffer() {
        if (mVertexBuffer != null) {
            mVertexBuffer.clear();
            mVertexBuffer = null;
        }
        if (mTextureBuffer != null) {
            mTextureBuffer.clear();
            mTextureBuffer = null;
        }
        if (mDisplayVertexBuffer!=null){
            mDisplayVertexBuffer.clear();
            mDisplayVertexBuffer = null;
        }
        if (mDisplayTextureBuffer != null) {
            mDisplayTextureBuffer.clear();
            mDisplayTextureBuffer = null;
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        releaseBuffer();
        releaseImageFilters();
        mContext = null;
    }

    /**
     * 设置窗口大小
     * @param width 宽度
     * @param height 高度
     */
    public void setDisplaySize(int width, int height){
        mDisplayWidth = width;
        mDisplayHeight = height;
        if (mImageWidth!=0 && mImageHeight != 0){
            adjustImageScaling();
//            calculateSize();
            onFilterChanged();
        }
    }

    /**
     * 设置图像大小
     * @param width 宽度
     * @param height 高度
     */
    public void setImageSize(int width, int height){
        mImageWidth = width;
        mImageHeight = height;
        if (mDisplayWidth!= 0 && mDisplayHeight!=0){
            adjustImageScaling();
//            calculateSize();
            onFilterChanged();
        }
    }

    /**
     * 滤镜渲染绘制
     * @param inputTexture 输入纹理
     * @param matrix 变换矩阵
     * @return 最终的纹理id
     */
    public int drawFrame(int inputTexture, float[] matrix){
        int currentTexture = inputTexture;
        // 判断OES滤镜和预览滤镜是否存在，两者必须同事满足
        if (mFilterArrays.get(RendererIndex.OES_INDEX) == null
                || mFilterArrays.get(RendererIndex.PREVIEW_INDEX) == null) {
            return currentTexture;
        }
        // 设置变换矩阵
        if (mFilterArrays.get(RendererIndex.OES_INDEX) instanceof GLImageOESFilter) {
            ((GLImageOESFilter)mFilterArrays.get(RendererIndex.OES_INDEX)).setMatrix(matrix);
        }

        // OES输入渲染
        currentTexture = mFilterArrays.get(RendererIndex.OES_INDEX)
                .onDrawFrame(currentTexture, mVertexBuffer, mTextureBuffer, true);

        // 其他滤镜

        // 1. 美颜滤镜
        currentTexture = mFilterArrays.get(RendererIndex.BEAUTY_INDEX)
                .onDrawFrame(currentTexture, mVertexBuffer, mTextureBuffer,true);

        // 2. 颜色滤镜
        currentTexture = mFilterArrays.get(RendererIndex.COLOR_INDEX)
                .onDrawFrame(currentTexture, mVertexBuffer, mTextureBuffer,true);

        // 3. 特效滤镜
//        currentTexture = mFilterArrays.get(RendererIndex.EFFECT_INDEX)
//                .onDrawFrame(currentTexture, mVertexBuffer, mTextureBuffer,true);

        // 预览输出渲染
        currentTexture = mFilterArrays.get(RendererIndex.PREVIEW_INDEX)
                .onDrawFrame(currentTexture,mDisplayVertexBuffer, mDisplayTextureBuffer, false);

        return currentTexture;
    }

    /**
     * 调整滤镜变换
     */
    private void onFilterChanged() {
        for (int i = 0; i < RendererIndex.RENDERER_COUNT ; i++) {
            if (mFilterArrays.get(i) != null) {
                mFilterArrays.get(i).onSurfaceChanged(mImageWidth, mImageHeight);
                // 到显示之前都需要创建FBO，这里限定是防止创建多余的FBO，节省GPU资源
                if (i < RendererIndex.PREVIEW_INDEX) {
                    mFilterArrays.get(i).onCreateFrameBuffer(mImageWidth, mImageHeight);
                }
                mFilterArrays.get(i).onDisplaySizeChanged(mDisplayWidth, mDisplayHeight);
            }
        }
    }

    private void calculateSize(){
        float ratioMax = Math.min((float) mDisplayWidth / mImageWidth,
                (float) mDisplayHeight / mImageHeight);

        mDisplayWidth = (int) (ratioMax * mImageWidth);
        mDisplayHeight = (int) (ratioMax * mImageHeight);

    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    private void adjustImageScaling() {
        float ratio1 = ((float)mDisplayWidth )/ mImageWidth;
        float ratio2 = ((float)mDisplayHeight) / mImageHeight;

        float ratioMax = Math.max(ratio1, ratio2);
        int imageWidthNew = Math.round(mImageWidth * ratioMax);
        int imageHeightNew = Math.round(mImageHeight * ratioMax);

        float ratioWidth = imageWidthNew / ((float)mDisplayWidth );
        float ratioHeight = imageHeightNew / ((float)mDisplayHeight);

        float[] vertexCords = TextureCoordinateUtils.vertices;
        float[] textureCords = TextureCoordinateUtils.texCoords;
        if (mImageScaleType == ImageScaleType.CENTER_CROP) {
            float distHorizontal = (1 - 1 / ratioWidth) / 2;
            float distVertical = (1 - 1 / ratioHeight) / 2;
            textureCords = new float[]{
                    addDistance(textureCords[0], distHorizontal), addDistance(textureCords[1], distVertical),
                    addDistance(textureCords[2], distHorizontal), addDistance(textureCords[3], distVertical),
                    addDistance(textureCords[4], distHorizontal), addDistance(textureCords[5], distVertical),
                    addDistance(textureCords[6], distHorizontal), addDistance(textureCords[7], distVertical),
            };
        } else {
            vertexCords = new float[]{
                    vertexCords[0] / ratioHeight, vertexCords[1] / ratioWidth,
                    vertexCords[2] / ratioHeight, vertexCords[3] / ratioWidth,
                    vertexCords[4] / ratioHeight, vertexCords[5] / ratioWidth,
                    vertexCords[6] / ratioHeight, vertexCords[7] / ratioWidth,
            };
        }

        if (mDisplayVertexBuffer == null || mDisplayTextureBuffer == null) {
            initBuffers();
        }
        mDisplayVertexBuffer.clear();
        mDisplayVertexBuffer.put(vertexCords).position(0);
        mDisplayTextureBuffer.clear();
        mDisplayTextureBuffer.put(textureCords).position(0);
    }


}
