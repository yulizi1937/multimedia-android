package com.supertramp.multimedia_android.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import com.supertramp.camera2library.record.ScreenRecordUtil;
import com.supertramp.multimedia_android.R;

/**
 * Created by supertramp on 16/12/6.
 */
public class RecordScreenService extends Service implements View.OnClickListener {

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private LinearLayout mFloatLayout;
    private LayoutInflater mInflater;
    private Button btn_start;
    private Button btn_stop;
    private Button btn_home;

    private final int flag = 1; //flag为1表示
    public static final String ACTION_SCREEN_SHOT = "action_screen_shot";

    private ScreenRecordUtil mUtil;
    private MyBinder myBinder = new MyBinder();

    @Override
    public void onCreate()
    {
        super.onCreate();
        createFloatView();
        initListener();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    private void initListener()
    {

        btn_start.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        btn_home.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_start:
                screenShot();
                break;
            case R.id.btn_stop:

                break;
            case R.id.btn_home:

                break;
        }

    }

    public void createFloatView()
    {

        mParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getSystemService(getApplication().WINDOW_SERVICE);
        mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mParams.format = PixelFormat.RGBA_8888;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.x = 0;
        mParams.y = 200;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mInflater = LayoutInflater.from(getApplicationContext());
        mFloatLayout = (LinearLayout) mInflater.inflate(R.layout.service_float, null);

        mWindowManager.addView(mFloatLayout, mParams);

        btn_start = (Button) mFloatLayout.findViewById(R.id.btn_start);
        btn_stop = (Button) mFloatLayout.findViewById(R.id.btn_stop);
        btn_home = (Button) mFloatLayout.findViewById(R.id.btn_home);

        btn_start.setOnTouchListener(new View.OnTouchListener()
        {

            int lastX, lastY;
            int paramX, paramY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {

                switch (motionEvent.getAction())
                {

                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) motionEvent.getRawX();
                        lastY = (int) motionEvent.getRawY();
                        paramX = mParams.x;
                        paramY = mParams.y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) motionEvent.getRawX() - lastX;
                        int dy = (int) motionEvent.getRawY() - lastY;
                        mParams.x = paramX + dx;
                        mParams.y = paramY + dy;
                        // 更新悬浮窗位置
                        mWindowManager.updateViewLayout(mFloatLayout, mParams);
                        break;

                }
                return false;

            }
        });

    }

    //截屏
    public void screenShot()
    {

        Intent intent = new Intent();
        intent.setAction(ACTION_SCREEN_SHOT);
        sendBroadcast(intent);

    }

    //录屏
    public void recordScreen()
    {

    }

    //停止录屏
    public void stopRecord()
    {

    }

    public class MyBinder extends Binder
    {

    }

}
