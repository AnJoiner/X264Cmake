package com.coder.x264cmake.module.camera.anotations;

import androidx.annotation.IntDef;

import com.coder.x264cmake.module.camera.egl.EGLManager;

@IntDef({
        EGLManager.FLAG_RECORDABLE,
        EGLManager.FLAG_WINDOW
})
public @interface EGLFlag {
}
