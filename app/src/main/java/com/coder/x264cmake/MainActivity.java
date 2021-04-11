package com.coder.x264cmake;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author: AnJoiner
 * @datetime: 21-4-10
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 0x01;
    // x264编码
    private X264Encode mX264Encode;
    // yuv 和 h264路径
    private String yuvPath;
    private String h264Path;
    // 视频宽高
    private static final  int width = 720;
    private static final int height = 1280;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button executeBtn = findViewById(R.id.execute_btn);

        yuvPath = getExternalCacheDir() + File.separator +"test.yuv";
        h264Path = getExternalCacheDir()+File.separator +"test.h264";

        mX264Encode = new X264Encode();

        FileUtils.copyFilesAssets(this,"test.yuv",yuvPath);

        executeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mX264Encode.encode(width,height,yuvPath,h264Path,YUVFormat.YUV_420);
            }
        });
    }

}
