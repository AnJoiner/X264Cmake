package com.coder.x264cmake.utils;

import android.content.Context;
import android.content.res.Resources;

/**
 * 像素密度计算工具
 */
@SuppressWarnings("unused")
public class DensityUtils {

    public float density;

    public DensityUtils() {
        density = Resources.getSystem().getDisplayMetrics().density;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     *
     * @param dpValue 虚拟像素
     * @return 像素
     */
    public static int dp2px(float dpValue) {
        int density = (int) (Resources.getSystem().getDisplayMetrics().density + 0.5f);
        return (int) (0.5f + dpValue * density);
    }


    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * @return px
     */
    public static int sp2px(float spValue) {
        return (int) (0.5f + spValue * Resources.getSystem().getDisplayMetrics().scaledDensity);
    }

    public static float sp2dp(float spValue) {
        float pxValue = (0.5f + spValue * Resources.getSystem().getDisplayMetrics().scaledDensity);
        return (pxValue / Resources.getSystem().getDisplayMetrics().density);
    }


    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * @return sp
     */
    public static int px2sp(float pxValue) {
        return (int) (0.5f + pxValue / Resources.getSystem().getDisplayMetrics().scaledDensity);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     *
     * @param pxValue 像素
     * @return 虚拟像素
     */
    public static float px2dp(int pxValue) {
        return (pxValue / Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     *
     * @param dpValue 虚拟像素
     * @return 像素
     */
    public int dip2px(float dpValue) {
        return (int) (0.5f + dpValue * density);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     *
     * @param pxValue 像素
     * @return 虚拟像素
     */
    public float px2dip(int pxValue) {
        return (pxValue / density);
    }


    public static int width() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int height() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    /**
     * 获取顶部状态高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    /**
     * 获取底部状态栏高度
     *
     * @param mContext
     * @return
     */
    public static int getNavigationBarHeight(Context mContext) {
        Resources resources = mContext.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }
}