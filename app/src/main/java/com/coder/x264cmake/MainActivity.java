package com.coder.x264cmake;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.coder.x264cmake.annotation.YUVFormat;
import com.coder.x264cmake.jni.X264Encode;
import com.coder.x264cmake.utils.FileUtils;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.h264.H264TrackImpl;


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
    // 最终的mp4路径
    private String mp4Path;

    // 视频宽高
    private int width = 720;
    private int height = 1280;

    private Button mExecuteBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initParams();
        initData();
        initListener();
    }

    private void initView() {
        mExecuteBtn = findViewById(R.id.execute_btn);
    }

    private void initParams() {
        // 替换成你自己的视频宽高
        width = 720;
        height = 1280;

        yuvPath = getExternalCacheDir() + File.separator + "test.yuv";
        h264Path = getExternalCacheDir() + File.separator + "test.h264";
        mp4Path = getExternalCacheDir() + File.separator + "test.mp4";

        FileUtils.copyFilesAssets(this, "test.yuv", yuvPath);
    }

    private void initData() {
        mX264Encode = new X264Encode();
    }


    private void initListener() {
        mExecuteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mX264Encode.encode(width, height, yuvPath, h264Path, YUVFormat.YUV_420);
                h264ToMp4();
            }
        });
    }

    /**
     * 将h264转成mp4
     */
    private void h264ToMp4(){
        H264TrackImpl h264Track;
        try {
            h264Track = new H264TrackImpl(new FileDataSourceImpl(h264Path));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Movie movie = new Movie();
        movie.addTrack(h264Track);

        Container mp4file = new DefaultMp4Builder().build(movie);

        FileChannel fc = null;
        try {
            fc = new FileOutputStream(new File(mp4Path)).getChannel();
            mp4file.writeContainer(fc);
            fc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
