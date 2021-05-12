package com.coder.x264cmake.module.encode.config;

import android.media.AudioFormat;

public class AudioConfig implements IConfig {

//    private int[] sampleRates = {8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100,
//            48000, 64000, 88200, 96000 };
    // 采样率
    public int sampleRate;
    // 码率
    public int bitRate;
    // 音频格式
    public int audioFormat;
    // 声道数量
    public int channelCount;
    // 输出格式
    public int outputFormat;

    private AudioConfig() {
    }


    public static class Builder {
        // 音频格式，默认16bit
        private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        // 声道数量，默认双声道
        private int channelCount = 2;
        // 采样率, 默认44100
        private int sampleRate = 44100;
        // 输出格式，0 raw 1 adts
        private int outputFormat = 0;
        // 码率
        private int bitRate = 96000;

        public Builder setAudioFormat(int audioFormat) {
            this.audioFormat = audioFormat;
            return this;
        }

        public Builder setChannelCount(int channels) {
            this.channelCount = channels;
            return this;
        }

        public Builder setSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
            return this;
        }

        public Builder setOutputFormat(int outputFormat) {
            this.outputFormat = outputFormat;
            return this;
        }

        public Builder setBitRate(int bitRate) {
            this.bitRate = bitRate;
            return this;
        }

        public AudioConfig create(){
            AudioConfig config = new AudioConfig();
            config.audioFormat = audioFormat;
            config.channelCount = channelCount;
            config.sampleRate = sampleRate;
            config.outputFormat = outputFormat;
            return config;
        }
    }
}
