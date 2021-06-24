package com.coder.x264cmake.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;


public class GlideUtils {

    public static void show(Context context, String url, ImageView imageView, int corner) {
        RequestOptions requestOptions = new RequestOptions()
                .transform(new CenterCrop(), new RoundedCorners(DensityUtils.dp2px(corner)));

        Glide.with(context).load(url)
                .apply(requestOptions)
                .into(imageView);
    }

    public static void show(Context context, int res, ImageView imageView, int corner) {
        RequestOptions requestOptions = new RequestOptions()
                .transform(new CenterCrop(), new RoundedCorners(DensityUtils.dp2px(corner)));

        Glide.with(context).load(res)
                .apply(requestOptions)
                .into(imageView);
    }

    public static void show(Context context, String url, ImageView imageView) {
        Glide.with(context).load(url).into(imageView);
    }

    public static void show(Context context, String url, int placeholder, ImageView imageView) {
        Glide.with(context).load(url).
                placeholder(placeholder).into(imageView);
    }

    public static void show(Context context, Uri uri, ImageView imageView) {
        Glide.with(context).load(uri).into(imageView);
    }

    public static void show(Context context, Bitmap bitmap, ImageView imageView) {
        Glide.with(context).load(bitmap).into(imageView);
    }

    public static void show(Context context, int drawableRes, ImageView imageView) {
        Glide.with(context).load(drawableRes).into(imageView);
    }

    public static void show(Context context, String url, ImageView imageView, boolean isCache){
        if (isCache){
            show(context, url, imageView);
        }else {
            showNotCache(context, url, imageView);
        }
    }

    public static void showGif(Context context, int drawableResGif, ImageView imageView) {
        Glide.with(context)
                .asGif()
                .load(drawableResGif)
                .into(imageView);
    }

    private static void showNotCache(Context context, String url, ImageView imageView){
        RequestOptions requestOptions = RequestOptions.skipMemoryCacheOf(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE);
        Glide.with(context).load(url)
                .apply(requestOptions)
                .into(imageView);
    }
}
