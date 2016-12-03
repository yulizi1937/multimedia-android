package com.supertramp.camera2library.record;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.view.Surface;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by supertramp on 16/12/1.
 *
 * 截屏:将ImageReader传入createVirtualDisplay
 * 录屏:将MediaCodec传入createVirtualDisplay
 */
public class ScreenRecordUtil {

    private Context mContext;
    private MediaProjectionManager mManager;
    private MediaProjection mProjection;
    private ImageReader mImageReader;
    private MediaCodec mMediaCodec;
    private Surface encodeSurface;

    private int mWidth;
    private int mHeight;
    private int mDensity;
    private BufferedOutputStream outputStream;

    private final String MIME_TYPE = "video/avc";
    private final int KEY_BIT_RATE = 3000000;
    private final int KEY_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
    private final int KEY_FRAME_RATE = 30;
    private final int KEY_I_FRAME_INTERVAL = 1;

    public static final int REQUEST_MEDIA_PROJECTION = 1;

    public ScreenRecordUtil(Context context)
    {
        this.mContext = context;
    }

    public void init(int width, int height, int density)
    {

        this.mWidth = width;
        this.mHeight = height;
        this.mDensity = density;
        mManager = (MediaProjectionManager) ((Activity)mContext).getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);

    }

    public Intent getIntent()
    {

        if (mManager != null)
        {
            return mManager.createScreenCaptureIntent();
        }
        return null;

    }

    public void setProjection(int resultCode, Intent data)
    {
        this.mProjection = mManager.getMediaProjection(resultCode, data);
    }

    //截屏
    public void screenShot()
    {

        mImageReader = ImageReader.newInstance(mWidth, mHeight, ImageFormat.JPEG, 1);
        try
        {

            outputStream = new BufferedOutputStream(new FileOutputStream(new File(mContext.getExternalCacheDir(), "supertramp.jpg")));
            mProjection.createVirtualDisplay("screen-mirror", mWidth, mHeight, mDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader.getSurface(), null, null);

            Image image = mImageReader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            outputStream.write(bytes, 0, bytes.length);

        }catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    //开始录制
    public void startRecord()
    {
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
        format.setInteger(MediaFormat.KEY_BIT_RATE, KEY_BIT_RATE);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, KEY_COLOR_FORMAT);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, KEY_FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, KEY_I_FRAME_INTERVAL);

        try
        {

            mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
            mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            encodeSurface = mMediaCodec.createInputSurface();
            mMediaCodec.start();

            outputStream = new BufferedOutputStream(new FileOutputStream(new File(mContext.getExternalCacheDir(), "supertramp.h264")));
            mProjection.createVirtualDisplay("screen-mirror", mWidth, mHeight, mDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, encodeSurface, null, null);

        }catch (IOException e)
        {
            e.printStackTrace();
        }

        Thread mThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {

            }
        });
        mThread.start();

    }

    public void release()
    {
        if (mProjection != null)
        {
            mProjection.stop();
            mMediaCodec.release();
            try
            {

                outputStream.close();

            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}
