package com.supertramp.camera2library.record;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.util.ULocale;
import android.media.MediaMetadataEditor;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Surface;
import android.view.TextureView;
import java.io.File;
import java.io.IOException;

/**
 * Created by supertramp on 16/11/23.
 */

public class VideoRecordUtil {

    private Context mContext;
    private TextureView mTextureview;
    private MediaRecorder mMediaRecorder;
    private File mVideoFile;

    public VideoRecordUtil(Context context, TextureView textureView)
    {
        this.mContext = context;
        this.mTextureview = textureView;
    }

    public void init() throws IOException
    {

        createVideoFile();
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置从麦克风采集声音
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);//设置从view中获取视频帧
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(1280, 720);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);//设置声音编码格式
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        mMediaRecorder.setOutputFile(mVideoFile.getAbsolutePath());
        mMediaRecorder.prepare();

    }

    public Surface getSurface()
    {
        return mMediaRecorder.getSurface();
    }

    public void createVideoFile() throws IOException
    {

        mVideoFile = new File(mContext.getExternalCacheDir(), "supertramp.mp4");
        if (mVideoFile.exists())
        {
            mVideoFile.delete();
        }
        mVideoFile.createNewFile();

    }

    public void start()
    {

        mMediaRecorder.start();

    }

    public void stop()
    {
        if (mMediaRecorder != null)
        {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
        }
    }

}
