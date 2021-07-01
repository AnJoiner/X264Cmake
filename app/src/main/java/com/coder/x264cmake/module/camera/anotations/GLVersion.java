package com.coder.x264cmake.module.camera.anotations;

import androidx.annotation.IntDef;

import com.coder.x264cmake.module.camera.egl.EGLManager;

@IntDef({
        EGLManager.GL_VERSION_2,
        EGLManager.GL_VERSION_3
})
public @interface GLVersion {

}
