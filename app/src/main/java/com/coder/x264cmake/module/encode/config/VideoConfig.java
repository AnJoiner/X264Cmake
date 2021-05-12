package com.coder.x264cmake.module.encode.config;

public class VideoConfig implements IConfig {
    // 视频宽
    public int width;
    // 视频高
    public int height;
    // 帧率
    public int fps;
    // 码率
    public int bitrate;

    private VideoConfig() {
    }

    public static class Builder {
        // 视频宽
        private int width;
        // 视频高
        private int height;
        // 帧率 720 * 1280 通常为 30
        private int fps = 30;
        // 码率 720 * 1280 通常为 2M
        private int bitrate = 2 * 1024 * 1024;

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setFps(int fps) {
            this.fps = fps;
            return this;
        }

        public Builder setBitrate(int bitrate) {
            this.bitrate = bitrate;
            return this;
        }

        public VideoConfig create(){
            VideoConfig config = new VideoConfig();
            config.width = width;
            config.height = height;
            config.bitrate = bitrate;
            config.fps = fps;
            return config;
        }
    }
}
