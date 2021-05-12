package com.coder.x264cmake.module.encode;

import com.coder.x264cmake.module.encode.config.IConfig;

public interface IMediaEncoder {

    void setup(IConfig iConfig);

    void start();

    void pushData(byte[] data);

    void stop();
}
