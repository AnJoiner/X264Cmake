package com.coder.x264cmake.utils;

import com.orhanobut.logger.Logger;

public class LogUtils {

    public static void d(String debug) {
        Logger.d(debug);
    }

    public static void e(String error) {
        Logger.e(error);
    }

    public static void w(String warning) {
        Logger.w(warning);
    }

    public static void v(String verbose) {
        Logger.v(verbose);
    }

    public static void i(String info){
        Logger.i(info);
    }

    public static void wtf(String wtf){
        Logger.wtf(wtf);
    }

    public static void json(String json){
        Logger.json(json);
    }

    public static void xml(String xml){
        Logger.xml(xml);
    }
}
