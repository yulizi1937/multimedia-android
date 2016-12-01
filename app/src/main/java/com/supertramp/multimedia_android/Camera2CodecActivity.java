package com.supertramp.multimedia_android;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
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
public class Camera2CodecActivity extends Activity implements TextureView.SurfaceTextureListener, View.OnClickListener {

    private AutoFitTextureView textureView;
    private Camera2Util mCamera2Util;
    private MediaCodec mMediaCodec;
    private Surface mCodecSurface;
    private byte[] configBytes;
    private BufferedOutputStream outputStream;
    private Button btnStart;
    private Button btnStop;

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
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStop = (Button) findViewById(R.id.btn_stop);
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
        mCamera2Util.setCameraStateCallback(new CameraDevice.StateCallback()
        {
            @Override
            public void onOpened(CameraDevice cameraDevice)
            {
                try
                {

                    initCodec(mCamera2Util.getPreviewSize());
                    mCamera2Util.setCameraDevice(cameraDevice);
                    mCamera2Util.createCodecSession(mCodecSurface);

                }catch (CameraAccessException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(CameraDevice cameraDevice)
            {

            }

            @Override
            public void onError(CameraDevice cameraDevice, int i)
            {

            }
        });
        textureView.setSurfaceTextureListener(this);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                try
                {

                    startCodec();
                    mCamera2Util.startRecord();

                }catch (CameraAccessException e)
                {
                    e.printStackTrace();
                }

            }
        });
        btnStop.setOnClickListener(this);
    }

    private void initCodec(Size size)
    {

        try
        {

            MediaFormat format = MediaFormat.createVideoFormat("video/avc", 640, 480);
            int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
            int videoBitrate = 600000;
            int videoFramePerSecond = 30;
            int iframeInterval = 1;
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
            format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, videoFramePerSecond);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iframeInterval);

            mMediaCodec = MediaCodec.createEncoderByType("video/avc");
            mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mCodecSurface = mMediaCodec.createInputSurface();
            mMediaCodec.start();

        }catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private void startCodec()
    {

        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                while (true)
                {
                    int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, -1);

                    if (outputBufferIndex >= 0)
                    {

                        Log.i("dequeueOutputBuffer", "hahhahhahahhahhahah");
                        try
                        {

                            ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                            byte[] byteBuffer = new byte[bufferInfo.size];
                            outputBuffer.get(byteBuffer);
                            outputStream.write(byteBuffer, 0, byteBuffer.length);

                        }catch (IOException e)
                        {
                            e.printStackTrace();
                        }


                    }

                }

            }
        });
        thread.start();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height)
    {

        try
        {

            mCamera2Util.init(width, height);
            mCamera2Util.requestPermission();

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

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_start:
                try
                {

                    startCodec();
                    mCamera2Util.startRecord();

                }catch (CameraAccessException e)
                {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_stop:
                break;
        }
    }
}
