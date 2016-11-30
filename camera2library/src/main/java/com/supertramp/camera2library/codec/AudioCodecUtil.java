package com.supertramp.camera2library.codec;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by supertramp on 16/11/21.
 */
public class AudioCodecUtil {

    private Context mContext;
    private MediaCodec mMediaCodec;
    private MediaFormat mMediaFormat;

    private File mAudioFile;
    private BufferedOutputStream outputStream;

    private int mMinBufferSize;
    private String AAC_MIME = "audio/mp4a-latm";    //aac编码
    private int DEFAULT_SAMPLE_RATE = 44100;//44.1khz采样率
    private int DEFAULT_BIT_RATE = 64000;

    public AudioCodecUtil(Context context, int bufferSize)
    {
        this.mContext = context;
        this.mMinBufferSize = bufferSize;
    }

    public void init() throws IOException
    {

        mMediaCodec = MediaCodec.createEncoderByType(AAC_MIME);
        mMediaFormat = new MediaFormat();
        mMediaFormat.setString(MediaFormat.KEY_MIME, AAC_MIME);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, DEFAULT_BIT_RATE);
        mMediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2);
        mMediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, DEFAULT_SAMPLE_RATE);
        mMediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mMediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mMinBufferSize * 2);
        mMediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

    }

    public void createFile() throws IOException
    {
        mAudioFile = new File(mContext.getExternalCacheDir(), "supertramp.m4a");
        if (mAudioFile.exists())
        {
            mAudioFile.delete();
        }
        mAudioFile.createNewFile();
        outputStream = new BufferedOutputStream(new FileOutputStream(mAudioFile));
    }

    public void encodePCM(byte[] input)
    {
        try
        {

            int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0)
            {

                ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
                inputBuffer.clear();
                inputBuffer.put(input);
                mMediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, System.nanoTime(), 0);
                input = null;

            }
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            while (outputBufferIndex >= 0)
            {

                ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                outputBuffer.position(bufferInfo.offset);
                outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                byte[] outData = new byte[bufferInfo.size + 7];
                addADTSToPacket(outData, bufferInfo.size + 7);
                outputBuffer.get(outData, 7, bufferInfo.size);//从 position7 开始,采集bufferInfo.size个数据
                outputBuffer.position(bufferInfo.offset);

                outputStream.write(outData, 0, outData.length);
                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);

            }

        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void startEncode() throws IOException
    {

        createFile();
        mMediaCodec.start();

    }

    public void stopEncode() throws IOException
    {

        if (mMediaCodec != null)
        {
            mMediaCodec.flush();
            mMediaCodec.stop();
            mMediaCodec.release();
            outputStream.flush();
            outputStream.close();
        }

    }

    //ADTS头中相对有用的信息:采样率、声道数、帧长度
    private void addADTSToPacket(byte[] packet, int packetLen)
    {

        int profile = 2;    //AAC LC
        int freqIdx = 4;    //44.1khz
        int chanCfg = 2;    //CPE

        // fill in ADTS data
        packet[0] = (byte)0xFF;
        packet[1] = (byte)0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;

    }

}
