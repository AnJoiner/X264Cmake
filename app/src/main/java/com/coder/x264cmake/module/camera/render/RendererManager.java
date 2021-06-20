package com.coder.x264cmake.module.camera.render;


import android.content.Context;
import android.util.SparseArray;

import com.coder.x264cmake.module.filter.GLImageBaseFilter;
import com.coder.x264cmake.module.filter.GLImageOESFilter;
import com.coder.x264cmake.utils.TextureCoordinateUtils;

import java.nio.FloatBuffer;

public class RendererManager {
    // 滤镜数量
    private static final int mFilterCount = 2;
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
        mFilterArrays.put(RendererIndex.PREVIEW_INDEX, new GLImageBaseFilter(mContext));
    }


    /**
     * 释放滤镜
     */
    public void releaseImageFilters(){
        for (int i = 0; i < mFilterCount; i++) {
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
            adjustCoordinateSize();
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
            adjustCoordinateSize();
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

        // 预览输出渲染
        currentTexture = mFilterArrays.get(RendererIndex.PREVIEW_INDEX)
                .onDrawFrame(currentTexture,mDisplayVertexBuffer, mDisplayTextureBuffer, false);

        return currentTexture;
    }

    /**
     * 调整滤镜变换
     */
    private void onFilterChanged() {
        for (int i = 0; i < mFilterCount; i++) {
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

    }

    /**
     * 自动适应调整窗口大小和图像大小不一致
     */
    private void adjustCoordinateSize() {
        float[] textureCoord = null;
        float[] vertexCoord = null;
        float[] textureVertices = TextureCoordinateUtils.texCoords;
        float[] vertexVertices = TextureCoordinateUtils.vertices;
        float ratioMax = Math.max((float) mDisplayWidth / mImageWidth,
                (float) mDisplayHeight / mImageHeight);
        // 新的宽高
        int imageWidth = Math.round(mImageWidth * ratioMax);
        int imageHeight = Math.round(mImageHeight * ratioMax);
        // 获取视图跟texture的宽高比
        float ratioWidth = (float) imageWidth / (float) mDisplayWidth;
        float ratioHeight = (float) imageHeight / (float) mDisplayHeight;
        if (mImageScaleType == ImageScaleType.CENTER_INSIDE) {
            vertexCoord = new float[] {
                    vertexVertices[0] / ratioHeight, vertexVertices[1] / ratioWidth,
                    vertexVertices[2] / ratioHeight, vertexVertices[3] / ratioWidth,
                    vertexVertices[4] / ratioHeight, vertexVertices[5] / ratioWidth,
                    vertexVertices[6] / ratioHeight, vertexVertices[7] / ratioWidth,
            };
        } else if (mImageScaleType == ImageScaleType.CENTER_CROP) {
            float distHorizontal = (1 - 1 / ratioWidth) / 2;
            float distVertical = (1 - 1 / ratioHeight) / 2;
            textureCoord = new float[] {
                    calculateDistance(textureVertices[0], distHorizontal), calculateDistance(textureVertices[1], distVertical),
                    calculateDistance(textureVertices[2], distHorizontal), calculateDistance(textureVertices[3], distVertical),
                    calculateDistance(textureVertices[4], distHorizontal), calculateDistance(textureVertices[5], distVertical),
                    calculateDistance(textureVertices[6], distHorizontal), calculateDistance(textureVertices[7], distVertical),
            };
        }
        if (vertexCoord == null) {
            vertexCoord = vertexVertices;
        }
        if (textureCoord == null) {
            textureCoord = textureVertices;
        }

        if (mDisplayVertexBuffer == null || mDisplayTextureBuffer == null) {
            initBuffers();
        }
        mDisplayVertexBuffer.clear();
        mDisplayVertexBuffer.put(vertexCoord).position(0);
        mDisplayTextureBuffer.clear();
        mDisplayTextureBuffer.put(textureCoord).position(0);
    }

    /**
     * 计算距离
     */
    private float calculateDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }
}
