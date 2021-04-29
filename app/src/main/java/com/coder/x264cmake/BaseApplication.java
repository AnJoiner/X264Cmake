package com.coder.x264cmake;

import android.app.Application;
import android.content.Context;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.sample.breakpad.BreakpadInit;

import java.io.File;

/**
 * @author: AnJoiner
 * @datetime: 21-4-10
 */
public class BaseApplication extends Application {

    private static BaseApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance =  this;

        String dirPath = this.getExternalCacheDir()+ File.separator+"crashDump";
        File dumpDir = new File(dirPath);
        if (!dumpDir.exists()){
            dumpDir.mkdirs();
        }
        BreakpadInit.initBreakpad(dirPath);

        // 初始化日志
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .tag("X264")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
    }

    public static BaseApplication getInstance() {
        return instance;
    }
}
