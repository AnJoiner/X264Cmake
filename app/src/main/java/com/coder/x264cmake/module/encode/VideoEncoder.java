package com.coder.x264cmake.module.encode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

import com.coder.x264cmake.module.encode.config.IConfig;
import com.coder.x264cmake.module.encode.config.VideoConfig;
import com.coder.x264cmake.module.encode.enums.EncodeEncState;
import com.coder.x264cmake.module.encode.enums.EncodeState;
import com.coder.x264cmake.utils.LogUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
import static android.media.MediaFormat.KEY_BIT_RATE;
import static android.media.MediaFormat.KEY_COLOR_FORMAT;
import static android.media.MediaFormat.KEY_FRAME_RATE;
import static android.media.MediaFormat.KEY_I_FRAME_INTERVAL;
import static android.media.MediaFormat.KEY_MAX_INPUT_SIZE;

public class VideoEncoder extends BaseMediaEncoder {
    // 视频编码配置文件
    private VideoConfig mVideoConfig;
    // 视频编码器
    MediaCodec mVideoCodec;
    // 编码器状态
    private EncodeState mState = EncodeState.UNINITIALIZED;
    // 编码状态
    private EncodeEncState mEncState = EncodeEncState.STOPPED;
    // 缓存队列
    LinkedBlockingQueue<byte[]> mQueue;
    // 编码线程
    private Thread mEncodeThread;

    // 数据回调
    private OnVideoEncodeCallback mOnVideoEncodeCallback;
    // 编码的planar - i420
    private final int mKeyColorFormat = COLOR_FormatYUV420Planar;

    public void setOnVideoEncodeCallback(OnVideoEncodeCallback onVideoEncodeCallback) {
        mOnVideoEncodeCallback = onVideoEncodeCallback;
    }

    @Override
    public void setup(IConfig iConfig) {
        if (mState.ordinal() > EncodeState.UNINITIALIZED.ordinal()) {
            LogUtils.e("Cannot setup video encoder again!");
            return;
        }
        if (!(iConfig instanceof VideoConfig)) {
            LogUtils.e("Failed to setup video config!");
            return;
        }
        mVideoConfig = (VideoConfig) iConfig;

        initEnCoder();
    }

    private void initEnCoder() {
        // 使用h.264 avc编码
        String mimeType = "video/avc";
        // 防止设置的宽高不是2的倍数，出现IllegalStateException错误
        int width = mVideoConfig.width / 2 * 2;
        int height = mVideoConfig.height / 2 * 2;

        MediaFormat format = MediaFormat.createVideoFormat(mimeType, width, height);
        format.setInteger(KEY_MAX_INPUT_SIZE, 10 * 1024);
        format.setInteger(KEY_BIT_RATE, mVideoConfig.bitrate);
        format.setInteger(KEY_COLOR_FORMAT, mKeyColorFormat);
        format.setInteger(KEY_FRAME_RATE, mVideoConfig.fps);
        format.setInteger(KEY_I_FRAME_INTERVAL, 1);

        try {
            MediaCodecInfo mediaCodecInfo = getCodecInfoByMimeType(mimeType);
            if (mediaCodecInfo == null) return;
            mVideoCodec = MediaCodec.createByCodecName(mediaCodecInfo.getName());
        } catch (IOException e) {
            LogUtils.e("Failed to create video encoder!");
        }

        // 配置状态configured
        mVideoCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        // 建立一个缓存队列
        mQueue = new LinkedBlockingQueue<>();
        // 变更状态为已初始化
        mState = EncodeState.INITIALIZED;
    }


    @Override
    public void start() {
        if (mState == EncodeState.UNINITIALIZED) {
            LogUtils.e("Failed to start. Please execute 'setup' method!");
            return;
        }
        mEncodeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isPresentationTimeUs){
                    presentationTimeUs = System.nanoTime();
                    isPresentationTimeUs = true;
                }
                if (mVideoCodec != null) mVideoCodec.start();
                while (mEncState == EncodeEncState.ENCODING || !mEncodeThread.isInterrupted()) {
                    try {
                        byte[] data = mQueue.take();
                        encodeData(data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (mVideoCodec != null) {
                    mVideoCodec.stop();
                    mVideoCodec.release();
                    mVideoCodec = null;
                }
            }
        });
        // 更改编码状态
        mEncState = EncodeEncState.ENCODING;
        mEncodeThread.start();
    }

    @Override
    public void pushData(byte[] data) {
        if (mState == EncodeState.UNINITIALIZED) {
            LogUtils.w("Failed to push data. Please execute 'setup' method first!");
            return;
        }
        if (mEncState == EncodeEncState.STOPPED) {
            LogUtils.w("Failed to push data. Please execute 'start' method first!");
            return;
        }
        try {
            mQueue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void encodeData(byte[] data) {
        // 获取缓存id
        int inputBufferId = mVideoCodec.dequeueInputBuffer(-1);
        if (inputBufferId >= 0) {
            ByteBuffer[] inputBuffers = mVideoCodec.getInputBuffers();
            ByteBuffer inputBuffer = inputBuffers[inputBufferId];
            inputBuffer.clear();
            // 放入数据
            inputBuffer.put(data);
            // 提交到编码队列
            mVideoCodec.queueInputBuffer(inputBufferId, 0, data.length, System.nanoTime() - presentationTimeUs, 0);
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferId = mVideoCodec.dequeueOutputBuffer(bufferInfo, 0);
        while (outputBufferId >= 0) {
            ByteBuffer[] outputBuffers = mVideoCodec.getOutputBuffers();
            ByteBuffer outputBuffer = outputBuffers[outputBufferId];
            if (mOnVideoEncodeCallback != null) {
                mOnVideoEncodeCallback.onVideoEncode(outputBuffer, bufferInfo);
            }
            // 释放输出缓冲区
            mVideoCodec.releaseOutputBuffer(outputBufferId, false);

            outputBufferId = mVideoCodec.dequeueOutputBuffer(bufferInfo, 0);
        }
    }

    @Override
    public void stop() {
        if (mState == EncodeState.UNINITIALIZED) {
            LogUtils.e("Current encoder is not initialized!");
            return;
        }
        if (mEncState == EncodeEncState.STOPPED) {
            LogUtils.e("Current encoder has been stopped!");
            return;
        }
        // 恢复初始状态
        mState = EncodeState.UNINITIALIZED;
        mEncState = EncodeEncState.STOPPED;

        if (mEncodeThread != null) {
            // 打断线程执行
            mEncodeThread.interrupt();
        }

    }


    private MediaCodecInfo getCodecInfoByMimeType(String mimeType) {
        for (int i = 0; i < MediaCodecList.getCodecCount(); i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
                    for (int k = 0; k < capabilities.colorFormats.length; k++) {
                        int colorFormat = capabilities.colorFormats[k];
                        if (colorFormat == mKeyColorFormat) {
                            return codecInfo;
                        }
                    }
                }
            }
        }
        return null;
    }

    public interface OnVideoEncodeCallback {
        void onVideoEncode(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo);
    }
}
