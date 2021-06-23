package com.coder.x264cmake.module.camera.anotations;

import androidx.annotation.IntDef;

import com.coder.x264cmake.module.camera.ICameraLoader;

@IntDef({
        ICameraLoader.CAMERA_PREVIEW_16To9,
        ICameraLoader.CAMERA_PREVIEW_4To3,
        ICameraLoader.CAMERA_PREVIEW_1To1
})
public @interface CameraPreviewType {
}
