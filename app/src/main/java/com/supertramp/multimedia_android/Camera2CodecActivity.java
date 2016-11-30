package com.supertramp.multimedia_android;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import com.supertramp.camera2library.basic.Camera2Util;
import com.supertramp.camera2library.widget.AutoFitTextureView;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by supertramp on 16/11/30.
 */
public class Camera2CodecActivity extends Activity implements TextureView.SurfaceTextureListener {

    private AutoFitTextureView textureView;
    private Camera2Util mCamera2Util;
    private MediaCodec mMediaCodec;
    private Surface mCodecSurface;
    private byte[] configBytes;
    private BufferedOutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2codec);

        initView();
        initData();
        initListener();

    }

    private void initView()
    {
        textureView = (AutoFitTextureView) findViewById(R.id.textureview);
    }

    private void initData()
    {

        mCamera2Util = new Camera2Util(this, textureView);
        try
        {

            outputStream = new BufferedOutputStream(new FileOutputStream(new File(getExternalCacheDir(), "camera2codec.h264")));

        }catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private void initListener()
    {
        textureView.setSurfaceTextureListener(this);
    }

    private void initCodec(Size size)
    {

        try
        {

            mMediaCodec = MediaCodec.createEncoderByType("video/avc");
            MediaFormat format = MediaFormat.createVideoFormat("video/avc", size.getWidth(), size.getHeight());
            int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
            int videoBitrate = 90000;
            int videoFramePerSecond = 25;
            int iframeInterval = 2;
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
            format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, videoFramePerSecond);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iframeInterval);

            mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mCodecSurface = mMediaCodec.createInputSurface();

            mMediaCodec.setCallback(new MediaCodec.Callback()
            {
                @Override
                public void onInputBufferAvailable(MediaCodec mediaCodec, int i)
                {
                    Log.i("mediacodec:", "onInputBufferAcailable");
                }

                @Override
                public void onOutputBufferAvailable(MediaCodec mediaCodec, int index, MediaCodec.BufferInfo bufferInfo)
                {
                    Log.i("mediacodec:", "onOutputBufferAvailable");

                    try
                    {

                        ByteBuffer buffer = mediaCodec.getOutputBuffer(index);
                        byte[] outData = new byte[bufferInfo.size];
                        buffer.get(outData);

                        if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME)
                        {
                            configBytes = new byte[bufferInfo.size];
                            configBytes = outData;
                        }
                        else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME)
                        {
                            byte[] keyframe = new byte[bufferInfo.size + configBytes.length];
                            System.arraycopy(configBytes, 0, keyframe, 0, configBytes.length);
                            System.arraycopy(outData, 0, keyframe, configBytes.length, outData.length);

                            outputStream.write(keyframe, 0, keyframe.length);
                        }
                        else
                        {
                            outputStream.write(outData, 0, outData.length);
                        }

                        mMediaCodec.releaseOutputBuffer(index, false);

                    }catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onError(MediaCodec mediaCodec, MediaCodec.CodecException e)
                {

                }

                @Override
                public void onOutputFormatChanged(MediaCodec mediaCodec, MediaFormat mediaFormat)
                {

                }
            });

        }catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height)
    {

        try
        {

            mCamera2Util.init(width, height);
            mCamera2Util.requestPermission();
            initCodec(mCamera2Util.getPreviewSize());

        }catch (CameraAccessException e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1)
    {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture)
    {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture)
    {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {

                try
                {

                    mCamera2Util.openCamera();
                    mCamera2Util.createCodecSession(mCodecSurface);
                    mMediaCodec.start();

                }catch (CameraAccessException e)
                {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaCodec.release();
        mCamera2Util.release();
    }
}
