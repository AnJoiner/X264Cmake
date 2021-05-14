package com.coder.x264cmake.module.encode;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

import com.coder.x264cmake.module.encode.config.AudioConfig;
import com.coder.x264cmake.module.encode.config.IConfig;
import com.coder.x264cmake.module.encode.enums.EncodeEncState;
import com.coder.x264cmake.module.encode.enums.EncodeState;
import com.coder.x264cmake.utils.LogUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import static android.media.MediaFormat.KEY_BIT_RATE;
import static android.media.MediaFormat.KEY_MAX_INPUT_SIZE;

public class AudioEncoder extends BaseMediaEncoder {
    // 音频配置文件
    private AudioConfig mAudioConfig;
    // 音频编码器
    MediaCodec mAudioCodec;
    // 编码器状态
    private EncodeState mState = EncodeState.UNINITIALIZED;
    // 编码状态
    private EncodeEncState mEncState = EncodeEncState.STOPPED;
    // 缓存队列
    LinkedBlockingQueue<byte[]> mQueue;
    // 编码线程
    private Thread mEncodeThread;

    private OnAudioEncodeCallback mOnAudioEncodeCallback;

    public void setOnAudioEncodeCallback(OnAudioEncodeCallback onAudioEncodeCallback) {
        mOnAudioEncodeCallback = onAudioEncodeCallback;
    }

    @Override
    public void setup(IConfig iConfig) {
        if (mState.ordinal() > EncodeState.UNINITIALIZED.ordinal()) {
            LogUtils.e("Cannot setup audio encoder again!");
            return;
        }
        if (!(iConfig instanceof AudioConfig)) {
            LogUtils.e("Failed to setup audio config!");
            return;
        }
        mAudioConfig = (AudioConfig) iConfig;

        initEnCoder();
    }

    private void initEnCoder() {
        // 使用latm编码
        String mimeType = "audio/mp4a-latm";
        MediaFormat format = MediaFormat.createAudioFormat(mimeType,
                mAudioConfig.sampleRate,
                mAudioConfig.channelCount);

        int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        if (mAudioConfig.channelCount == 1) {
            channelConfig = AudioFormat.CHANNEL_IN_MONO;
        }
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(mAudioConfig.sampleRate,
                channelConfig, mAudioConfig.audioFormat);

        format.setInteger(KEY_MAX_INPUT_SIZE, 2*bufferSizeInBytes);
        // 设置ACC规格为LC
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        // 设置比特率
        format.setInteger(KEY_BIT_RATE, mAudioConfig.bitRate);

        try {
            MediaCodecInfo mediaCodecInfo =  getCodecInfoByMimeType(mimeType);
            if (mediaCodecInfo == null) return;
            mAudioCodec = MediaCodec.createByCodecName(mediaCodecInfo.getName());
        } catch (IOException e) {
            LogUtils.e("Failed to create audio encoder!");
        }

        // 配置状态configured
        mAudioCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
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
                if (mAudioCodec != null) mAudioCodec.start();
                while (mEncState == EncodeEncState.ENCODING || !mEncodeThread.isInterrupted()) {
                    try {
                        if (mQueue.isEmpty() || mQueue.size() == 0){
                            continue;
                        }
                        byte[] data = mQueue.take();
                        encodeData(data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (mAudioCodec != null) {
                    mAudioCodec.stop();
                    mAudioCodec.release();
                    mAudioCodec = null;
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
        }
        try {
            mQueue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    synchronized private void encodeData(byte[] data) {
        // 获取缓存id
//        LogUtils.d("Audio Encoder ===>>> start to dequeue input buffer");
        int inputBufferId = mAudioCodec.dequeueInputBuffer(-1);
//        LogUtils.d("Audio Encoder ===>>> dequeue input buffer");
        if (inputBufferId >= 0) {
            ByteBuffer[] inputBuffers = mAudioCodec.getInputBuffers();
            ByteBuffer inputBuffer = inputBuffers[inputBufferId];
            inputBuffer.clear();
            // 放入数据
            inputBuffer.put(data);
            // 提交到编码队列
            mAudioCodec.queueInputBuffer(inputBufferId, 0, data.length,
                    System.nanoTime() - presentationTimeUs, 0);
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferId = mAudioCodec.dequeueOutputBuffer(bufferInfo, 0);
        while (outputBufferId >= 0) {
            ByteBuffer[] outputBuffers = mAudioCodec.getOutputBuffers();
            ByteBuffer outputBuffer = outputBuffers[outputBufferId];
            if (mAudioConfig.outputFormat == 1) {
                addADTS(outputBuffer, bufferInfo);
            } else {
                if (mOnAudioEncodeCallback != null) {
                    byte[] bytes = new byte[bufferInfo.size];
                    outputBuffer.get(bytes);
                    mOnAudioEncodeCallback.onAudioEncode(bytes, bufferInfo);
                }
            }
            // 释放输出缓冲区
            mAudioCodec.releaseOutputBuffer(outputBufferId, false);
            outputBufferId = mAudioCodec.dequeueOutputBuffer(bufferInfo, 0);
        }
    }

    private void addADTS(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo) {
        // 测量输出缓冲区大小
        int bufferSize = bufferInfo.size;
        // 输出缓冲区实际大小，ADTS头部长度为7
        int bufferOutSize = bufferSize + 7;
        // 指定输出缓存区偏移位置以及限制大小
        outputBuffer.position(bufferInfo.offset);
        outputBuffer.limit(bufferInfo.offset + bufferSize);

        byte[] data = new byte[bufferOutSize];
        // 增加ADTS头部
        addADTStoPacket(data, bufferOutSize);
        // 将编码输出数据写入缓存空间
        outputBuffer.get(data, 7, bufferInfo.size);
        // 重新指定输出缓存区偏移
        outputBuffer.position(bufferInfo.offset);

        if (mOnAudioEncodeCallback != null) {
            bufferInfo.size = bufferSize + 7;
            mOnAudioEncodeCallback.onAudioEncode(data, bufferInfo);
        }
    }

    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 4; // 44.1KHz
        int chanCfg = 2; // CPE
        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte)0xF9;//-7 网上的都是这个，但不能在ios 播放
//        packet[1] = (byte) 0xF1;//-15 这个能在ios 能播放，
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
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
                    return codecInfo;
                }
            }
        }
        return null;
    }

    public interface OnAudioEncodeCallback {
        void onAudioEncode(byte[] bytes, MediaCodec.BufferInfo bufferInfo);
    }

}
