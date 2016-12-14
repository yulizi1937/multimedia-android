package com.supertramp.multimedia_android.activity;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import com.supertramp.camera2library.basic.Camera2Util;
import com.supertramp.camera2library.codec.VideoCodecUtil;
import com.supertramp.camera2library.widget.AutoFitTextureView;
import com.supertramp.multimedia_android.R;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by supertramp on 16/11/23.
 */
public class VideoCodecActivity extends Activity implements View.OnClickListener, TextureView.SurfaceTextureListener {

    private AutoFitTextureView mTextureview;
    private Button btnStart;
    private Button btnStop;
    private Camera2Util mCamera2Util;
    private VideoCodecUtil mVideoCodecUtil;

    public static final int COLOR_FORMATI420 = 1;
    public static final int COLOR_FORMATNV21 = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videocodec);
        initView();
        initData();
        initListener();

    }

    private void initView()
    {
        mTextureview = (AutoFitTextureView) findViewById(R.id.codec_textureview);
        btnStart = (Button) findViewById(R.id.btn_codec_start);
        btnStop = (Button) findViewById(R.id.btn_codec_stop);
    }

    private void initData()
    {
        mCamera2Util = new Camera2Util(this, mTextureview);

        mCamera2Util.setCameraStateCallback(new CameraDevice.StateCallback()
        {
            @Override
            public void onOpened(CameraDevice cameraDevice)
            {
                try
                {

                    mCamera2Util.setCameraDevice(cameraDevice);
                    mCamera2Util.createCodecSession();
                    mVideoCodecUtil = new VideoCodecUtil(VideoCodecActivity.this, mCamera2Util.getPreviewSize().getWidth(), mCamera2Util.getPreviewSize().getHeight());
                    mVideoCodecUtil.init();

                }catch (CameraAccessException e)
                {

                    e.printStackTrace();

                }catch (IOException e1)
                {

                    e1.printStackTrace();

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
        mCamera2Util.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener()
        {
            @Override
            public void onImageAvailable(ImageReader imageReader)
            {
                Image image = imageReader.acquireNextImage();
                byte[] input = transformFromImage(image, COLOR_FORMATNV21);
                mVideoCodecUtil.enqueueYUV(input);
                image.close();

            }
        });
    }

    private void initListener()
    {
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        mTextureview.setSurfaceTextureListener(this);
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
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_codec_start:
                start();
                break;
            case R.id.btn_codec_stop:
                stop();
                break;
        }
    }

    public void start()
    {

        try
        {

            mVideoCodecUtil.createVideoFile();
            mVideoCodecUtil.startEncode();

        }catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public void stop()
    {
        mVideoCodecUtil.stopEncode();
    }

    //Image中存储的是YUV420Flexible格式(YUV数据格式的集合),数据存储在planes中,planes[0]存储Y,planes[1]存储U,planes[2]存储V
    //该方法将YUV420Flexible转化为特定的YUV格式
    public byte[] transformFromImage(Image image, int colorFormat)
    {

        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++)
        {
            switch (i)
            {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (colorFormat == COLOR_FORMATI420)
                    {
                        channelOffset = width * height;
                        outputStride = 1;
                    }
                    else if (colorFormat == COLOR_FORMATNV21)
                    {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (colorFormat == COLOR_FORMATI420)
                    {
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    }
                    else if (colorFormat == COLOR_FORMATNV21)
                    {
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++)
            {
                int length;
                if (pixelStride == 1 && outputStride == 1)
                {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                }
                else
                {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++)
                    {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1)
                {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;

    }

}
