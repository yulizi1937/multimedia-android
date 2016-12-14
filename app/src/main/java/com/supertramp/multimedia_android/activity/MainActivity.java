package com.supertramp.multimedia_android.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.supertramp.multimedia_android.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvTakepic;
    private TextView tvRecordAudio;
    private TextView tvRecordVideo;
    private TextView tvCodecVideo;
    private TextView tvScreenShot;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        initListener();

    }

    private void initView()
    {

        tvTakepic = (TextView) findViewById(R.id.tv_takepic);
        tvRecordAudio = (TextView) findViewById(R.id.tv_record_audio);
        tvRecordVideo = (TextView) findViewById(R.id.tv_record_video);
        tvCodecVideo = (TextView) findViewById(R.id.tv_codec_video);
        tvScreenShot = (TextView) findViewById(R.id.tv_screen_shot);

    }

    private void initData()
    {

    }

    private void initListener()
    {
        tvTakepic.setOnClickListener(this);
        tvRecordAudio.setOnClickListener(this);
        tvRecordVideo.setOnClickListener(this);
        tvCodecVideo.setOnClickListener(this);
        tvScreenShot.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        Intent intent;
        switch (view.getId())
        {
            case R.id.tv_takepic:
                intent = new Intent(this, TakePictureActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_record_audio:
                intent = new Intent(this, AudioRecordActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_record_video:
                intent = new Intent(this, VideoRecordActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_codec_video:
                intent = new Intent(this, Camera2CodecActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_screen_shot:
                intent = new Intent(this, ScreenRecordActivity.class);
                startActivity(intent);
                break;
        }
    }

}
