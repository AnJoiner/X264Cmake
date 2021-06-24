package com.coder.x264cmake.utils;

import android.widget.Toast;

import com.coder.x264cmake.BaseApplication;

/**
 * @auther: AnJoiner
 * @datetime: 2021/6/24
 */
public class ToastUtils {

    public static void show(String msg){
        Toast.makeText(BaseApplication.getInstance(),msg,Toast.LENGTH_SHORT).show();
    }
}
