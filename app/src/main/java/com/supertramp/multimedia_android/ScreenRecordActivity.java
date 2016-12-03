package com.supertramp.multimedia_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import com.supertramp.camera2library.record.ScreenRecordUtil;

/**
 * Created by supertramp on 16/12/1.
 */
public class ScreenRecordActivity extends Activity implements View.OnClickListener {

    private Button btn_screenshot;
    private Button btn_screenrecord;
    private ScreenRecordUtil mUtil;

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

    }

    private void initData()
    {

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mUtil = new ScreenRecordUtil(this);
        mUtil.init(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
        startActivityForResult(mUtil.getIntent(), ScreenRecordUtil.REQUEST_MEDIA_PROJECTION);

    }

    private void initListener()
    {

        btn_screenshot.setOnClickListener(this);
        btn_screenrecord.setOnClickListener(this);

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
        }
    }

}
