package com.coder.x264cmake.module.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;


public class AudioLoader {
    // Android提供音频录制
    private AudioRecord audioRecord;

    private OnAudioRecordListener onAudioRecordListener;
    // 最小可缓存
    private int bufferSizeInBytes;

    public void setOnAudioRecordListener(OnAudioRecordListener onAudioRecordListener) {
        this.onAudioRecordListener = onAudioRecordListener;
    }

    public void init() {
        init(0, 0, 0);
    }

    public void init(int sampleRateInHz, int channelConfig, int audioFormat) {
        if (sampleRateInHz <= 0) {
            sampleRateInHz = 48000;
        }
        if (channelConfig <= 0) {
            channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        }
        if (audioFormat <= 0) {
            audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        }
        // 计算最小的录制缓存
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER,
                sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);

    }

    public void startRecord() {
        // 初始化完成，可开始录制音频
        if (audioRecord != null && audioRecord.getState() == AudioRecord.STATE_INITIALIZED
                && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
            audioRecord.startRecording();

            executeReadData();
        }
    }

    private void executeReadData() {
        // 正在录制
        byte[] bytes = new byte[bufferSizeInBytes];
        while (audioRecord.getState() == AudioRecord.STATE_INITIALIZED
                && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.read(bytes, 0, bufferSizeInBytes);
            if (onAudioRecordListener != null) {
                onAudioRecordListener.onAudioRecord(bytes, 0, bufferSizeInBytes);
            }
        }
    }

    public void stopRecord() {
        // 当正在录制音频即可停止
        if (audioRecord != null && audioRecord.getState() == AudioRecord.STATE_INITIALIZED
                && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop();
        }
    }

    public void release(){
        audioRecord.release();
        audioRecord = null;
    }


    public interface OnAudioRecordListener {
        void onAudioRecord(byte[] bytes, int offsetInBytes, int sizeInBytes);
    }
}
