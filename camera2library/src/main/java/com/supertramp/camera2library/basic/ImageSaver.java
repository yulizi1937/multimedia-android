package com.supertramp.camera2library.basic;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.ImageReader;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by supertramp on 16/11/18.
 */
public class ImageSaver implements Runnable {

    private ImageReader mReader;
    private Image mImage;
    private File mFile;
    private int mType;

    private BufferedOutputStream outputStream;

    public static final int FILE_TYPE_JPEG = 0;
    public static final int FILE_TYPE_YUV = 1;

    public ImageSaver(ImageReader reader, File file, int type)
    {
        this.mReader = reader;
        this.mFile = file;
        this.mType = type;
        this.mImage = mReader.acquireNextImage();
    }

    @Override
    public void run()
    {

        if (mType == FILE_TYPE_JPEG)
        {
            saveToJpeg();
        }
        else if (mType == FILE_TYPE_YUV)
        {
            saveToYUV();
        }

    }

    public void saveToJpeg()
    {
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        try
        {

            outputStream = new BufferedOutputStream(new FileOutputStream(mFile));
            outputStream.write(bytes);

        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void saveToYUV()
    {

        try
        {

            outputStream = new BufferedOutputStream(new FileOutputStream(mFile));
            byte[] data = YUVUtil.getYU12(mImage);
            outputStream.write(data, 0, data.length);

        }catch (IOException e)
        {
            e.printStackTrace();
        }finally {

            mImage.close();
            try
            {

                outputStream.close();

            }catch (IOException e1)
            {
                e1.printStackTrace();
            }

        }

    }

    public void saveYUVToJPEG()
    {

        try
        {
            outputStream = new BufferedOutputStream(new FileOutputStream(mFile));
            byte[] data = YUVUtil.getNV21(mImage);

            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, mImage.getWidth(), mImage.getHeight(), getStrides());
            Rect rect = new Rect(0, 0, mImage.getWidth(), mImage.getHeight());
            yuvImage.compressToJpeg(rect, 100, outputStream);
            outputStream.close();

        }catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public int[] getStrides()
    {

        Image image = mReader.acquireNextImage();
        Image.Plane plane_0 = image.getPlanes()[0];
        Image.Plane plane_1 = image.getPlanes()[1];
        Image.Plane plane_2 = image.getPlanes()[2];
        return new int[]{plane_0.getRowStride(), plane_1.getRowStride(), plane_2.getRowStride()};

    }

}
