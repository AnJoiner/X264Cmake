package com.coder.x264cmake.utils;

import java.nio.ByteBuffer;

public class YUV420Utils {

    /**
     * yuv420-nv21转yuv420-i420
     *
     * @param data   nv21数据
     * @param width  宽
     * @param height 高
     * @return 420数据
     */
    private byte[] nv21ToI420(byte[] data, int width, int height) {
        byte[] ret = new byte[data.length];
        int total = width * height;

        ByteBuffer bufferY = ByteBuffer.wrap(ret, 0, total);
        ByteBuffer bufferU = ByteBuffer.wrap(ret, total, total / 4);
        ByteBuffer bufferV = ByteBuffer.wrap(ret, total + total / 4, total / 4);

        bufferY.put(data, 0, total);
        for (int i = total; i < data.length; i += 2) {
            bufferV.put(data[i]);
            bufferU.put(data[i + 1]);
        }

        return ret;
    }


    /**
     * 将yuv420数据旋转90°
     * @param data yuv数据
     * @param width 旋转前的宽
     * @param height 旋转前的高
     * @return 旋转后的数据
     */
    public static byte[] rotate90(byte[] data, int width, int height) {
        byte[] yuv = new byte[width * height * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < width; x++) {
            for (int y = height - 1; y >= 0; y--) {
                yuv[i] = data[y * width + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = width * height * 3 / 2 - 1;
        for (int x = width - 1; x > 0; x = x - 2) {
            for (int y = 0; y < height / 2; y++) {
                yuv[i] = data[(width * height) + (y * width) + x];
                i--;
                yuv[i] = data[(width * height) + (y * width) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    /**
     * 将yuv420数据旋转180°
     * @param data yuv数据
     * @param width 旋转前的宽
     * @param height 旋转前的高
     * @return 旋转后的数据
     */
    public static byte[] rotate180(byte[] data, int width, int height) {
        byte[] yuv = new byte[width * height * 3 / 2];
        int i = 0;
        int count = 0;
        for (i = width * height - 1; i >= 0; i--) {
            yuv[count] = data[i];
            count++;
        }
        i = width * height * 3 / 2 - 1;
        for (i = width * height * 3 / 2 - 1; i >= width
                * height; i -= 2) {
            yuv[count++] = data[i - 1];
            yuv[count++] = data[i];
        }
        return yuv;
    }

    /**
     * 将yuv420数据旋转270°
     * @param data yuv数据
     * @param width 旋转前的宽
     * @param height 旋转前的高
     * @return 旋转后的数据
     */
    public static byte[] rotate270(byte[] data, int width,
                                               int height) {
        byte[] yuv = new byte[width * height * 3 / 2];
        int nWidth = 0, nHeight = 0;
        int wh = 0;
        int uvHeight = 0;
        if (width != nWidth || height != nHeight) {
            nWidth = width;
            nHeight = height;
            wh = width * height;
            uvHeight = height >> 1;// uvHeight = height / 2
        }
        // ??Y
        int k = 0;
        for (int i = 0; i < width; i++) {
            int nPos = 0;
            for (int j = 0; j < height; j++) {
                yuv[k] = data[nPos + i];
                k++;
                nPos += width;
            }
        }
        for (int i = 0; i < width; i += 2) {
            int nPos = wh;
            for (int j = 0; j < uvHeight; j++) {
                yuv[k] = data[nPos + i];
                yuv[k + 1] = data[nPos + i + 1];
                k += 2;
                nPos += width;
            }
        }
        return rotate180(yuv, width, height);
    }
}
