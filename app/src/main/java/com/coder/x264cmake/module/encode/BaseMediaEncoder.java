package com.coder.x264cmake.module.encode;

public abstract class BaseMediaEncoder implements IMediaEncoder{
    protected boolean isPresentationTimeUs = false;
    // pts
    protected long presentationTimeUs = 0;
}
