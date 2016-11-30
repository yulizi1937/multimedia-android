package com.supertramp.camera2library.basic;

import android.graphics.ImageFormat;
import android.media.Image;
import java.nio.ByteBuffer;

/**
 * Created by supertramp on 16/11/25.
 */
public class YUVUtil {

    /**
     * 获取原始图片
     */
    public static byte[] getYU12(Image image)
    {

        int format = image.getFormat();
        int mWidth = image.getWidth();
        int mHeight = image.getHeight();
        Image.Plane[] planes = image.getPlanes();

        byte[] data = new byte[mWidth * mHeight * ImageFormat.getBitsPerPixel(format)/8];

        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i ++)
        {
            switch (i)
            {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = mWidth * mHeight + 1;
                    outputStride = 2;
                    break;
                case 2:
                    channelOffset = mWidth * mHeight;
                    outputStride = 2;
                    break;
            }

            ByteBuffer buffer_y = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();

            int shift = (i == 0) ? 0:1;

        }
        return data;

    }

    public static byte[] getNV21(Image image)
    {

        int format = image.getFormat();
        int mWidth = image.getWidth();
        int mHeight = image.getHeight();
        Image.Plane[] planes = image.getPlanes();

        byte[] data = new byte[mWidth * mHeight * ImageFormat.getBitsPerPixel(format)/8];

        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i ++)
        {
            switch (i)
            {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = mWidth * mHeight + 1;
                    outputStride = 2;
                    break;
                case 2:
                    channelOffset = mWidth * mHeight;
                    outputStride = 2;
                    break;
            }

            ByteBuffer buffer_y = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();

            int shift = (i == 0) ? 0:1;

        }

        return data;

    }

    public static byte[] getNV12(Image image)
    {
        int mWidth = image.getWidth();
        int mHeight = image.getHeight();
        Image.Plane[] planes = image.getPlanes();

        ByteBuffer buffer_y = planes[0].getBuffer();
        ByteBuffer buffer_u = planes[1].getBuffer();
        ByteBuffer buffer_v = planes[2].getBuffer();

        byte[] byte_y = new byte[buffer_y.remaining()];
        byte[] byte_u = new byte[buffer_u.remaining()];
        byte[] byte_v = new byte[buffer_v.remaining()];

        buffer_y.get(byte_y);
        buffer_u.get(byte_u);
        buffer_v.get(byte_v);
        byte[] data = new byte[byte_y.length + byte_u.length + byte_v.length];
        System.arraycopy(byte_y, 0, data, 0, byte_y.length);

        int len = byte_u.length;
        int pos = byte_y.length;
        for (int i = 0;i < len;i ++)
        {
            data[pos] = byte_u[i];
            pos ++;
            data[pos] = byte_v[i];
            pos ++;
        }

        return data;
    }

    /**
     *
     * 将YUV420像素数据去掉颜色(变成灰度图)
     * 只需将U、V分量设置成128即可,这是因为U、V是图像中的经过偏置处理的色度分量。色度分量在偏置处理前的取值范围是-128至127，
     * 这时候的无色对应的是“0”值。经过偏置后色度分量取值变成了0至255，因而此时的无色对应的就是128了。
     *
     */
    public static byte[] getGray(Image image)
    {

        int mWidth = image.getWidth();
        int mHeight = image.getHeight();
        byte[] data = new byte[mWidth*mHeight*3/2];
        Image.Plane[] planes = image.getPlanes();

        ByteBuffer buffer_y = planes[0].getBuffer();
        ByteBuffer buffer_u = planes[1].getBuffer();
        ByteBuffer buffer_v = planes[2].getBuffer();

        byte[] byte_y = new byte[buffer_y.remaining()];
        byte[] byte_u = new byte[buffer_u.remaining()];
        byte[] byte_v = new byte[buffer_v.remaining()];

        buffer_y.get(byte_y);
        for (int i = 0;i < byte_u.length;i ++)
        {
            byte_u[i] = Integer.valueOf(128).byteValue();
        }

        for (int i = 0;i < byte_v.length;i ++)
        {
            byte_v[i] = Integer.valueOf(128).byteValue();
        }

        System.arraycopy(byte_y, 0, data, 0, mWidth*mHeight);
        System.arraycopy(byte_u, 0, data, mWidth*mHeight, mWidth*mHeight/4);
        System.arraycopy(byte_v, 0, data, mWidth*mHeight*5/4, mWidth*mHeight/4);
        return data;

    }

    //亮度减半
    public static void getHalfBright(Image image)
    {

    }

    public void compressToJpeg()
    {

    }

}
