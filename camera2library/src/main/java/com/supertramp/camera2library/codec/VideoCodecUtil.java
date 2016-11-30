package com.supertramp.camera2library.codec;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by supertramp on 16/11/23.
 */
public class VideoCodecUtil
{

    private Context mContext;
    private MediaCodec mMediaCodec;
    private Thread mThread;
    private int mWidth;
    private int mHeight;
    private boolean isRunning;
    private byte[] configByte;
    private DataOutputStream outputStream;

    private ArrayBlockingQueue<byte[]> YUVQueue;

    public VideoCodecUtil(Context context, int width, int height)
    {
        this.mContext = context;
        this.mWidth = width;
        this.mHeight = height;
        YUVQueue = new ArrayBlockingQueue<byte[]>(10);
    }

    public void init() throws IOException
    {

        mMediaCodec = MediaCodec.createEncoderByType("video/avc");
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", mWidth, mHeight);
        int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        int bitrate = mWidth*mHeight*5;
        int fps = 25;
        int iFrameInterval = 1;
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval);

        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();

        mMediaCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(MediaCodec mediaCodec, int i) {

            }

            @Override
            public void onOutputBufferAvailable(MediaCodec mediaCodec, int i, MediaCodec.BufferInfo bufferInfo) {

            }

            @Override
            public void onError(MediaCodec mediaCodec, MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(MediaCodec mediaCodec, MediaFormat mediaFormat) {

            }
        });

    }

    public void enqueueYUV(byte[] data)
    {
        if (YUVQueue.size() >= 10)
        {
            YUVQueue.poll();
        }
        YUVQueue.add(data);
    }

    public byte[] pollYUV()
    {
        if (YUVQueue.size() == 0)
        {
            return null;
        }
        return YUVQueue.poll();
    }

    public void startEncode()
    {

        mThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {

                isRunning = true;
                byte[] input = null;
                long pts = 0;
                long generateIndex = 0;

                while (isRunning)
                {
                    if (YUVQueue.size() > 0)
                    {
                        input = pollYUV();
                    }

                    if (input != null)
                    {

                        try
                        {

                            int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
                            if (inputBufferIndex >= 0)
                            {
                                pts = computePresentationTime(generateIndex);
                                ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
                                inputBuffer.clear();
                                inputBuffer.put(input);
                                mMediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
                                generateIndex += 1;
                            }

                            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                            int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 12000);
                            while (outputBufferIndex >= 0)
                            {

                                ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                                byte[] outData = new byte[bufferInfo.size];
                                outputBuffer.get(outData);
                                if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG)
                                {
                                    configByte = new byte[bufferInfo.size];
                                    configByte = outData;
                                }
                                else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME)
                                {
                                    byte[] keyframe = new byte[bufferInfo.size + configByte.length];
                                    System.arraycopy(configByte, 0, keyframe, 0, configByte.length);
                                    System.arraycopy(outData, 0, keyframe, configByte.length, outData.length);

                                    outputStream.write(keyframe, 0, keyframe.length);
                                }
                                else
                                {
                                    outputStream.write(outData, 0, outData.length);
                                }

                                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 12000);
                            }

                        }catch (IOException e)
                        {
                            e.printStackTrace();
                        }

                    }
                    else
                    {
                        try{

                            Thread.sleep(200);

                        }catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });

        mThread.start();

    }

    public void stopEncode()
    {
        isRunning = false;
        if (mMediaCodec != null)
        {
            mMediaCodec.stop();
            mMediaCodec.release();
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

    private long computePresentationTime(long frameIndex)
    {
        return 132 + frameIndex * 1000000 / (mWidth*mHeight*5);
    }

    //创建编码后的视频文件
    public void createVideoFile() throws IOException
    {
        File mFile = new File(mContext.getExternalCacheDir(), "videoCodec.h264");
        if (mFile.exists())
        {
            mFile.delete();
        }
        mFile.createNewFile();
        outputStream = new DataOutputStream(new FileOutputStream(mFile));
    }

}
