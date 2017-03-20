package com.supertramp.camera2library.record;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
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
 * 截屏:将ImageReader传入createVirtualDisplay
 * 录屏:将MediaCodec传入createVirtualDisplay
 */
public class ScreenRecordUtil {

    private Context mContext;
    private MediaProjectionManager mManager;
    private MediaProjection mProjection;
    private VirtualDisplay mVirtualDisplay;
    private ImageReader mImageReader;
    private MediaRecorder mMediaRecorder;
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
        mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);

    }

    //初始化MediaRecorder
    public void initMediaRecorder()
    {

        File file = new File(mContext.getExternalCacheDir(), System.currentTimeMillis() + ".mp4");
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        mMediaRecorder.setVideoSize(mWidth, mHeight);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
        mMediaRecorder.setVideoFrameRate(30);

        try
        {

            mMediaRecorder.prepare();

        }catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    //初始化MediaCodec
    public void initMediaCodec()
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
            mVirtualDisplay = mProjection.createVirtualDisplay("screen-mirror", mWidth, mHeight, mDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, encodeSurface, null, null);

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

    private void createVirtualDisplay()
    {

        mVirtualDisplay = mProjection.createVirtualDisplay(

                "MainScreen",
                mWidth,
                mHeight,
                mDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(),
                null, null

        );

    }

    //截屏
    public void screenShot()
    {

        try
        {

            outputStream = new BufferedOutputStream(new FileOutputStream(new File(mContext.getExternalCacheDir(), System.currentTimeMillis() + ".png")));
            mVirtualDisplay = mProjection.createVirtualDisplay("screen-mirror", mWidth, mHeight, mDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader.getSurface(), null, null);

            Image image = mImageReader.acquireNextImage();
            savePicture(image);

        }catch (IOException e)
        {

            e.printStackTrace();

        } finally
        {

            try
            {

                outputStream.flush();
                outputStream.close();

            }catch (IOException e)
            {
                e.printStackTrace();
            }

        }

    }

    private void savePicture(Image image)
    {

        if (image == null)
        {
            return;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding/pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();

        try
        {

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

        }catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    //开始录制
    public void startRecord()
    {

        initMediaRecorder();
        createVirtualDisplay();
        mMediaRecorder.start();

    }

    public void release()
    {
        if (mProjection != null)
        {
            mProjection.stop();
            mVirtualDisplay.release();

            if (mMediaCodec != null)
            {
                mMediaCodec.release();
            }

            if (mMediaRecorder != null)
            {
                mMediaRecorder.stop();
                mMediaRecorder.release();
            }

            if (outputStream != null)
            {
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

}
