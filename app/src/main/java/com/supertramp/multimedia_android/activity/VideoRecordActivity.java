package com.supertramp.multimedia_android.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.IBinder;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.supertramp.camera2library.basic.Camera2Util;
import com.supertramp.camera2library.record.VideoRecordUtil;
import com.supertramp.camera2library.widget.AutoFitTextureView;
import com.supertramp.multimedia_android.R;
import com.supertramp.multimedia_android.service.RecordScreenService;
import java.io.IOException;

/**
 * Created by supertramp on 16/11/23.
 */
public class VideoRecordActivity extends Activity implements View.OnClickListener, TextureView.SurfaceTextureListener {

    private AutoFitTextureView mTextureview;
    private Button btnStart;
    private Button btnStop;
    private Camera2Util mCamera2Util;
    private VideoRecordUtil mVideoRecordUtil;

    private IBinder mIBinder;

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            mIBinder = iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videorecord);

        initView();
        initData();
        initListener();

    }

    private void initView()
    {
        mTextureview = (AutoFitTextureView) findViewById(R.id.textureview);
        btnStart = (Button) findViewById(R.id.btn_videorecord_start);
        btnStop = (Button) findViewById(R.id.btn_videorecord_stop);
    }

    private void initData()
    {

        mCamera2Util = new Camera2Util(this, mTextureview);
        mVideoRecordUtil = new VideoRecordUtil(this, mTextureview);

        try
        {

            mVideoRecordUtil.init();

        }catch (IOException e)
        {
            e.printStackTrace();
        }

        mCamera2Util.setCameraStateCallback(new CameraDevice.StateCallback()
        {
            @Override
            public void onOpened(CameraDevice cameraDevice)
            {
                try
                {

                    mCamera2Util.setCameraDevice(cameraDevice);
                    mCamera2Util.createPreviewSession(mVideoRecordUtil.getSurface());

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

        mCamera2Util.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener()
        {
            @Override
            public void onImageAvailable(ImageReader imageReader)
            {

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
            case R.id.btn_videorecord_start:
                start();
                break;
            case R.id.btn_videorecord_stop:
                stop();
                break;
        }
    }

    private void start()
    {

        //mVideoRecordUtil.start();
        Intent service = new Intent(this, RecordScreenService.class);
        bindService(service, serviceConnection, 0);

    }

    private void stop()
    {
        mVideoRecordUtil.stop();
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
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

