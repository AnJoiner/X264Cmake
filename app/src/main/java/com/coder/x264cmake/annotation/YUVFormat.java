package com.coder.x264cmake.annotation;

import androidx.annotation.IntDef;

/**
 * @author: AnJoiner
 * @datetime: 21-4-10
 */
@IntDef({
        YUVFormat.YUV_420,
        YUVFormat.YUV_NV21,
        YUVFormat.YUV_422,
        YUVFormat.YUV_444
})
public @interface YUVFormat {
    int YUV_444 = 3;
    int YUV_422 = 2;
    int YUV_NV21 = 1;
    int YUV_420 = 0;
}
