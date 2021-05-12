package com.coder.x264cmake;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.coder.x264cmake.annotation.YUVFormat;
import com.coder.x264cmake.databinding.ActivityCameraBinding;
import com.coder.x264cmake.jni.RtmpPusher;
import com.coder.x264cmake.jni.X264Encode;
import com.coder.x264cmake.module.audio.AudioLoader;
import com.coder.x264cmake.module.camera.CameraLoader;
import com.coder.x264cmake.module.encode.AudioEncoder;
import com.coder.x264cmake.module.encode.VideoEncoder;
import com.coder.x264cmake.module.encode.config.AudioConfig;
import com.coder.x264cmake.module.encode.config.VideoConfig;
import com.coder.x264cmake.utils.LogUtils;
import com.coder.x264cmake.utils.YUV420Utils;
import com.coder.x264cmake.widgets.CameraPreview;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.AudioFormat.CHANNEL_IN_STEREO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityCameraBinding mViewBinding;
    // 相机
    private CameraLoader mCameraLoader;
    // 音频录制
    private AudioLoader mAudioLoader;
    // 相机预览
    private CameraPreview mPreview;
    // 是否正在录制
    private boolean isRecording;
    // h264编码器
    private VideoEncoder mVideoEncoder;
    private AudioEncoder mAudioEncoder;
    // 视频配置
    private  VideoConfig mVideoConfig;
    private AudioConfig mAudioConfig;

//    private X264Encode x264Encode;
    // h264视频存储地址
    private String h264Path;
    // aac音频存储地址
    private String aacPath;

    private final int sampleRate = 44100;
    private final int channel = 2;
    private final int bitrate = 96000;


    private RtmpPusher mRtmpPusher;

    public static void start(Context context) {
        Intent starter = new Intent(context, CameraActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = ActivityCameraBinding.inflate(LayoutInflater.from(this));
        setContentView(mViewBinding.getRoot());

        init();
    }

    private void init() {
        initData();
        initCamera();
        initAudio();
        initListener();
    }

    private void initData() {
        mVideoConfig = new VideoConfig.Builder()
                .setWidth(720)
                .setHeight(1440)
                .create();

        mAudioConfig = new AudioConfig.Builder()
                .setChannelCount(CHANNEL_IN_STEREO)
                .setSampleRate(sampleRate)
                .setBitRate(bitrate)
                .create();

        mVideoEncoder = new VideoEncoder();
        mAudioEncoder = new AudioEncoder();


//        x264Encode = new X264Encode();
        mRtmpPusher = new RtmpPusher();
    }

    private void initCamera() {
        mCameraLoader = new CameraLoader();
        mCameraLoader.setOnCameraPreCallback(new CameraLoader.OnCameraPreCallback() {
            @Override
            public void onCameraPreFrame(byte[] data, int width, int height) {
                if (isRecording) {
                    LogUtils.d("h264-encode data size ===>>> " + data.length);
                    int rotate = mCameraLoader.getRotation();
                    if (mCameraLoader.cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        if (rotate == 90) {
                            mVideoEncoder.pushData(YUV420Utils.rotate90(data, width, height));
//                            x264Encode.encode_x264_data(YUV420Utils.rotate90(data, width, height));
                        }
                    } else {
                        if (rotate == 90) {
                            mVideoEncoder.pushData(YUV420Utils.rotate270(data, width, height));
//                            x264Encode.encode_x264_data(YUV420Utils.rotate270(data, width, height));
                        }
                    }

                }
            }
        });
        mPreview = new CameraPreview(this, mCameraLoader);
        mViewBinding.cameraPreview.addView(mPreview);
    }


    private void initAudio() {
        mAudioLoader = new AudioLoader();
        mAudioLoader.init(sampleRate, CHANNEL_IN_STEREO, ENCODING_PCM_16BIT);
        mAudioLoader.setOnAudioRecordListener(new AudioLoader.OnAudioRecordListener() {
            @Override
            public void onAudioRecord(byte[] data, int offsetInBytes, int sizeInBytes) {
                if (isRecording) {
                    LogUtils.d("fdkaac-encode data size ===>>> " + data.length);
                    mAudioEncoder.pushData(data);
//                    x264Encode.encode_aac_data(data);
                }
            }
        });
    }

    private void initListener() {

        mViewBinding.cameraBtn.setOnClickListener(this);
        mViewBinding.backBtn.setOnClickListener(this);
        mViewBinding.switchBtn.setOnClickListener(this);

        mVideoEncoder.setOnVideoEncodeCallback(new VideoEncoder.OnVideoEncodeCallback() {
            @Override
            public void onVideoEncode(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
                byte[] data = new byte[bufferInfo.size];
                byteBuffer.get(data);
                mRtmpPusher.rtmp_pusher_push_video(data,bufferInfo.size,
                        0);
            }
        });

        mAudioEncoder.setOnAudioEncodeCallback(new AudioEncoder.OnAudioEncodeCallback() {
            @Override
            public void onAudioEncode(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
                byte[] data = new byte[bufferInfo.size];
                byteBuffer.get(data);
                mRtmpPusher.rtmp_pusher_push_audio(data,bufferInfo.size,
                        0);
            }
        });

//        x264Encode.setOnEncodeListener(new X264Encode.OnEncodeListener() {
//            @Override
//            public void onEncodeH264(byte[] bytes, int size) {
//                mRtmpPusher.rtmp_pusher_push_video(bytes,size,System.currentTimeMillis()/1000);
//            }
//
//            @Override
//            public void onEncodeAAC(byte[] bytes, int size) {
//            }
//        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.camera_btn) {
            isRecording = !isRecording;
            mViewBinding.cameraImage.setSelected(isRecording);
            if (!isRecording) {
                mVideoEncoder.stop();
                mAudioEncoder.stop();
//                x264Encode.release_x264();
//                x264Encode.release_aac();
                mAudioLoader.stopRecord();
                mRtmpPusher.rtmp_pusher_close();
            } else {
                mRtmpPusher.rtmp_pusher_open("rtmp://192.168.10.161:8080/toto/live",720,1440);
                h264Path = getExternalCacheDir() + File.separator + System.currentTimeMillis() + ".h264";
//                x264Encode.init_x264(720, 1440, h264Path, YUVFormat.YUV_NV21);
                mVideoEncoder.setup(mVideoConfig);
                mVideoEncoder.start();

                aacPath = getExternalCacheDir() + File.separator + System.currentTimeMillis() + ".aac";
//                x264Encode.init_aac(sampleRate, channel, bitrate, aacPath);
                mAudioEncoder.setup(mAudioConfig);
                mAudioEncoder.start();

                mAudioLoader.startRecord();
            }
        } else if (v.getId() == R.id.back_btn) {
            finish();
        } else if (v.getId() == R.id.switch_btn) {
            // 切换摄像头
            onSwitchCamera();
        }
    }


    /**
     * 切换摄像头
     */
    private void onSwitchCamera() {
        if (mCameraLoader != null) {
            mCameraLoader.switchCamera();
            SurfaceHolder holder = mPreview.getHolder();
            if (holder != null) {
                try {
                    mCameraLoader.cameraInstance.setPreviewDisplay(holder);
                    mCameraLoader.cameraInstance.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAudioLoader != null) {
            mAudioLoader.release();
        }
    }
}
