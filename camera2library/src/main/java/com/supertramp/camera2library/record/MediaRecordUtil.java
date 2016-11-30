package com.supertramp.camera2library.record;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Process;
import com.supertramp.camera2library.codec.AudioCodecUtil;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by supertramp on 16/11/21.
 */
public class MediaRecordUtil {

    private Context mContext;
    private boolean isRecording;
    private MediaRecorder mMediaRecorder;//集成录制,编码,压缩等功能

    //通过AudioRecord录制可对语音进行实时处理,可用代码实现各种音频的封装
    private AudioRecord mAudioRecord; //输出PCM语音数据,当保存为音频文件,是不能被播放器播放(但可以被AudioTrack播放),必须先写代码实现数据编码以及压缩
    private AudioTrack mAudioTrack;
    private int bufSize;    //AudioRecord内部音频缓冲区的大小,该缓冲区的值不能低于一帧"音频帧"的大小(int size = 采样率 * 位宽 * 采样时间 * 通道数)
    private int DEFAULT_SAMPLE_RATE = 44100;  //44100Hz是唯一可以保证兼容所有Android手机的采样率
    private int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;//通道数设置
    private int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;   //音频数据格式:脉冲编码调制(PCM)每个样品16位,16位保证兼容所有Android手机
    private Thread mAudioThread;

    private AudioCodecUtil mAudioCodecUtil;
    private FileOutputStream outputStream;

    public MediaRecordUtil(Context context)
    {
        this.mContext = context;
    }

    public void createAudioRecord()
    {

        bufSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT, bufSize);//第一个参数为设置音频源
        mAudioThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {

                try
                {

                    createPCMFile();
                    mAudioCodecUtil.startEncode();
                    while (isRecording)
                    {
                        byte[] bytes = new byte[bufSize];
                        int read = mAudioRecord.read(bytes, 0, bufSize);
                        if (read != AudioRecord.ERROR_INVALID_OPERATION)
                        {
                            writeAudioDataToFile(bytes);
                            mAudioCodecUtil.encodePCM(bytes);
                        }
                    }

                }catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        }, "AudioRecord Thread");

    }

    public void startRecordAudio() throws InterruptedException, IOException
    {
        createAudioRecord();
        mAudioRecord.startRecording();
        isRecording = true;
        mAudioThread.start();

        mAudioCodecUtil = new AudioCodecUtil(mContext, bufSize);
        mAudioCodecUtil.init();
        mAudioCodecUtil.startEncode();

    }

    public void stopRecordAudio() throws IOException
    {
        if (mAudioRecord != null)
        {
            isRecording = false;
            mAudioRecord.stop();
            mAudioRecord.release();

            mAudioRecord = null;
            mAudioThread = null;

            mAudioCodecUtil.stopEncode();
        }
    }

    public void playAudio() throws IOException
    {
        File file = new File(mContext.getExternalCacheDir(), "supertramp.pcm");
        if (!file.exists())
        {
            return;
        }

        bufSize = AudioTrack.getMinBufferSize(DEFAULT_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, DEFAULT_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufSize, AudioTrack.MODE_STREAM);
        final DataInputStream dis = new DataInputStream(new FileInputStream(file));

        Thread playThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                byte[] tempBuffer = new byte[bufSize];
                int readCount = 0;
                try
                {

                    while (dis.available() > 0)
                    {
                        readCount = dis.read(tempBuffer);
                        if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE)
                        {
                            continue;
                        }

                        if (readCount != 0 && readCount != -1)
                        {
                            mAudioTrack.play();
                            mAudioTrack.write(tempBuffer, 0, readCount);
                        }
                    }
                    stopPlay();

                }catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        });
        playThread.start();

    }

    public void stopPlay()
    {
        if (mAudioTrack != null)
        {
            mAudioTrack.stop();
            mAudioTrack.release();

            mAudioTrack = null;
        }
    }

    public void writeAudioDataToFile(byte[] data) throws IOException
    {

        if (outputStream != null)
        {
            outputStream.write(data);
        }

    }

    private void createPCMFile() throws IOException
    {

        File file = new File(mContext.getExternalCacheDir(), "supertramp.pcm");
        if (file.exists())
        {
            file.delete();
        }
        file.createNewFile();
        outputStream = new FileOutputStream(file);

    }

}
