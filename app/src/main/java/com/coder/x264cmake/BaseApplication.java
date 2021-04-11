package com.coder.x264cmake;

import android.app.Application;

import com.sample.breakpad.BreakpadInit;

import java.io.File;

/**
 * @author: AnJoiner
 * @datetime: 21-4-10
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        String dirPath = this.getExternalCacheDir()+ File.separator+"crashDump";
        File dumpDir = new File(dirPath);
        if (!dumpDir.exists()){
            dumpDir.mkdirs();
        }
        BreakpadInit.initBreakpad(dirPath);
    }
}
