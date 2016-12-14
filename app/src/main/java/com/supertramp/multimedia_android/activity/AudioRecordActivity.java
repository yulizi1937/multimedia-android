package com.supertramp.multimedia_android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.supertramp.camera2library.record.MediaRecordUtil;
import com.supertramp.multimedia_android.R;

import java.io.IOException;

/**
 * Created by supertramp on 16/11/21.
 */
public class AudioRecordActivity extends Activity implements View.OnClickListener {

    private TextView tvStart;
    private TextView tvStop;
    private TextView tvPlay;
    private MediaRecordUtil mMediaRecordUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audiorecord);

        initView();
        initData();
        initListener();

    }

    private void initView()
    {
        tvStart = (TextView) findViewById(R.id.tv_record_start);
        tvStop = (TextView) findViewById(R.id.tv_record_stop);
        tvPlay = (TextView) findViewById(R.id.tv_record_play);
    }

    private void initData()
    {
        mMediaRecordUtil = new MediaRecordUtil(this);
    }

    private void initListener()
    {
        tvStart.setOnClickListener(this);
        tvStop.setOnClickListener(this);
        tvPlay.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.tv_record_start:
                start();
                break;
            case R.id.tv_record_stop:
                stop();
                break;
            case R.id.tv_record_play:
                play();
                break;
        }
    }

    private void start()
    {
        try
        {

            mMediaRecordUtil.startRecordAudio();

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void stop()
    {

        try
        {

            mMediaRecordUtil.stopRecordAudio();

        }catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private void play()
    {
        try
        {

            mMediaRecordUtil.playAudio();

        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
