package com.supertramp.camera2library.record;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import com.supertramp.camera2library.codec.AudioCodecUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by supertramp on 16/11/22.
 */
public class AudioRecordUtil {

    private Context mContext;
    private AudioCodecUtil mAudioCodecUtil;

    private int mMinBufferSize;
    private AudioRecord mAudioRecord; //输出PCM语音数据,当保存为音频文件,是不能被播放器播放(但可以被AudioTrack播放),必须先写代码实现数据编码以及压缩
    private AudioTrack mAudioTrack;
    private Thread mEncodeThread;
    private File mAudioFile;
    private FileOutputStream outputStream;
    private boolean isRecording;

    public static final int DEFAULT_SAMPLE_RATE = 44100;  //44100Hz是唯一可以保证兼容所有Android手机的采样率
    public static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;//通道数设置
    public static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;   //音频数据格式:脉冲编码调制(PCM)每个样品16位,16位保证兼容所有Android手机

    public AudioRecordUtil(Context context)
    {
        this.mContext = context;
    }

    public void init()
    {
        mMinBufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT, mMinBufferSize);//第一个参数为设置音频源
        mAudioCodecUtil = new AudioCodecUtil(mContext, mMinBufferSize);
        mEncodeThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {

                try
                {

                    mAudioCodecUtil.startEncode();
                    while (isRecording)
                    {
                        byte[] bytes = new byte[mMinBufferSize];
                        int read = mAudioRecord.read(bytes, 0, mMinBufferSize);
                        if (read != AudioRecord.ERROR_INVALID_OPERATION)
                        {
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

    //创建原始文件
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

    //存储音频流到PCM文件
    public void writePCMToFile(byte[] data) throws IOException
    {

        if (outputStream != null)
        {
            outputStream.write(data);
        }

    }

}
