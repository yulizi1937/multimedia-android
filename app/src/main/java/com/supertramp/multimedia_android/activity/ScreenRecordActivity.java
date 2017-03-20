package com.supertramp.multimedia_android.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.MemoryFile;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.supertramp.camera2library.record.ScreenRecordUtil;
import com.supertramp.multimedia_android.R;
import com.supertramp.multimedia_android.service.RecordScreenService;
import com.supertramp.multimedia_android.utils.FFMpegUtil;

/**
 * Created by supertramp on 16/12/1.
 */
public class ScreenRecordActivity extends Activity implements View.OnClickListener {

    private Button btn_screenshot;
    private Button btn_screenrecord;
    private Button btn_startservice;
    private ScreenRecordUtil mUtil;
    private FFMpegUtil mFUtils;

    private IBinder mIBinder;
    private MyBRReceiver myBRReceiver;

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            mIBinder = iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediaprojection);
        initView();
        initData();
        initListener();

    }

    private void initView()
    {

        btn_screenshot = (Button) findViewById(R.id.btn_screenshot);
        btn_screenrecord = (Button) findViewById(R.id.btn_screenrecord);
        btn_startservice = (Button) findViewById(R.id.btn_startservice);

    }

    private void initData()
    {

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mUtil = new ScreenRecordUtil(this);
        mUtil.init(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
        startActivityForResult(mUtil.getIntent(), ScreenRecordUtil.REQUEST_MEDIA_PROJECTION);

        mFUtils = new FFMpegUtil();
        btn_screenshot.setText("");

    }

    private void initListener()
    {

        btn_screenshot.setOnClickListener(this);
        btn_screenrecord.setOnClickListener(this);
        btn_startservice.setOnClickListener(this);

        myBRReceiver = new MyBRReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RecordScreenService.ACTION_SCREEN_SHOT);
        registerReceiver(myBRReceiver, filter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ScreenRecordUtil.REQUEST_MEDIA_PROJECTION)
        {
            mUtil.setProjection(resultCode, data);
        }
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_screenshot:
                mUtil.screenShot();
                break;
            case R.id.btn_screenrecord:
                mUtil.startRecord();
                break;
            case R.id.btn_startservice:
                startService();
                break;
        }
    }

    public void startService()
    {
        Intent intent = new Intent(this, RecordScreenService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(myBRReceiver);
        if (mUtil != null)
        {
            mUtil.release();
        }
    }

    public class MyBRReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            Toast.makeText(ScreenRecordActivity.this, "开始截屏喽!", Toast.LENGTH_SHORT).show();
            mUtil.screenShot();
        }

    }

}
