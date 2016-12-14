package com.supertramp.multimedia_android.activity;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.media.ImageReader;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import com.supertramp.camera2library.basic.Camera2Util;
import com.supertramp.camera2library.widget.AutoFitTextureView;
import com.supertramp.multimedia_android.R;

/**
 * Created by supertramp on 16/11/17.
 */
public class TakePictureActivity extends Activity implements View.OnClickListener, TextureView.SurfaceTextureListener {

    private AutoFitTextureView mTextureview;
    private Button button;
    private Camera2Util mCamera2Util;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takepicture);

        initView();
        initData();
        initListener();

    }

    private void initView()
    {

        mTextureview = (AutoFitTextureView) findViewById(R.id.textureview);
        button = (Button) findViewById(R.id.btn_takepic);

    }

    private void initData()
    {

        mCamera2Util = new Camera2Util(this, mTextureview);
        mCamera2Util.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener()
        {
            @Override
            public void onImageAvailable(ImageReader imageReader)
            {
                mCamera2Util.savePicture(imageReader, System.currentTimeMillis() + ".yuv");
            }
        });

    }

    private void initListener()
    {
        button.setOnClickListener(this);
        //mTextureview.setSurfaceTextureListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.btn_takepic:
                takePicture();
                break;
        }

    }

    private void takePicture()
    {
        try
        {

            mCamera2Util.lockFocus();

        }catch (CameraAccessException e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                try {

                    mCamera2Util.openCamera();

                }catch (CameraAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }
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
        mCamera2Util.orientationTransform();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture)
    {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture)
    {

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mCamera2Util.release();
    }
}
