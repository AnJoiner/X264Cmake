package com.coder.x264cmake.module.camera.anotations;

import androidx.annotation.IntDef;

import com.coder.x264cmake.module.camera.ICameraLoader;

@IntDef({
        ICameraLoader.CAMERA_1,
        ICameraLoader.CAMERA_2,
        ICameraLoader.CAMERA_X
    })
    public @interface CameraType{
        
    }
    