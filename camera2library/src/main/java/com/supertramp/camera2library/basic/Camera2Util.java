package com.supertramp.camera2library.basic;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import com.supertramp.camera2library.widget.AutoFitTextureView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by supertramp on 16/11/17.
 */
public class Camera2Util {

    private Context mContext;
    private AutoFitTextureView mTextureView;
    private String mCameraId;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraStateCallback;
    private CameraCaptureSession.StateCallback mSessionStateCallback;
    private CaptureRequest.Builder mRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraCaptureSession.CaptureCallback mCaptureCallback;
    private ImageReader mImageReader;
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener;

    private Handler mHandler;
    private HandlerThread mThread;
    //预览宽高,最大预览宽高(屏幕宽高),
    private Size mPreviewSize;
    private Size jpegSize;
    private int maxPreviewWidth;
    private int maxPreviewHeight;
    private int viewWidth;
    private int viewHeight;
    private int mSensorOrientation;

    public int mState; //相机状态
    public static final int STATE_PREVIEW = 0;
    public static final int STATE_WATTING_LOCK= 1; //等待对焦锁定
    public static final int STATE_TAKEN = 2;  //拍照

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private Comparator<Size> comparator = new Comparator<Size>()
    {
        @Override
        public int compare(Size size, Size t1)
        {
            return Long.signum((long)size.getWidth() * size.getHeight() - (long)size.getWidth() * size.getHeight());
        }
    };

    public Camera2Util(Context context, AutoFitTextureView textureView) {
        this.mContext = context;
        this.mTextureView = textureView;
    }

    public Size getPreviewSize()
    {
        return mPreviewSize;
    }

    public void setCameraDevice(CameraDevice device)
    {
        this.mCameraDevice = device;
    }

    public void setCameraStateCallback(CameraDevice.StateCallback stateCallback)
    {
        this.mCameraStateCallback = stateCallback;
    }

    public void setOnImageAvailableListener(ImageReader.OnImageAvailableListener listener) {
        this.mOnImageAvailableListener = listener;
    }

    /**
     * 初始化
     * @param width textureview的宽
     * @param height textureview的高
     */
    public void init(int width, int height) throws CameraAccessException
    {
        this.viewWidth = width;
        this.viewHeight = height;
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mThread = new HandlerThread("Camera2");
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
        mCameraId = "0";

        final CameraCharacteristics mCameraCharacteristers = mCameraManager.getCameraCharacteristics(mCameraId);
        StreamConfigurationMap map = mCameraCharacteristers.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        Size[] previewSizes = map.getOutputSizes(SurfaceTexture.class);
        mPreviewSize = previewSizes[0];
        mSensorOrientation = mCameraCharacteristers.get(CameraCharacteristics.SENSOR_ORIENTATION);
        orientationTransform();

        Size[] jpegSizes = map.getOutputSizes(ImageFormat.JPEG);
        this.jpegSize = Collections.max(Arrays.asList(jpegSizes), comparator);

        if (mCameraStateCallback == null)
        {
            mCameraStateCallback = new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice cameraDevice) {
                    try {

                        mCameraDevice = cameraDevice;
                        createPreviewSession();

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onDisconnected(CameraDevice cameraDevice)
                {
                    cameraDevice.close();
                    mCameraDevice = null;
                }

                @Override
                public void onError(CameraDevice cameraDevice, int i)
                {
                    cameraDevice.close();
                    mCameraDevice = null;
                }
            };
        }

        mSessionStateCallback = new CameraCaptureSession.StateCallback()
        {
            @Override
            public void onConfigured(CameraCaptureSession cameraCaptureSession) {

                try {

                    mCameraCaptureSession = cameraCaptureSession;
                    updatePreview();

                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

            }
        };

        mCaptureCallback = new CameraCaptureSession.CaptureCallback()
        {

            public void process(CaptureResult result) {
                switch (mState) {
                    case STATE_PREVIEW:
                        //啥都不做
                        break;
                    case STATE_WATTING_LOCK:
                        mState = STATE_TAKEN;
                        takePicture();
                        break;
                }
            }

            @Override
            public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
                super.onCaptureProgressed(session, request, partialResult);
                process(partialResult);
            }

            @Override
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                process(result);
            }

        };

    }

    //选择最优的预览大小,大于view宽高的最小尺寸
    public Size chooseOptimalPreviewSize(Size[] choices)
    {

        List<Size> biggerSizes = new ArrayList<>();
        List<Size> smallerSizes = new ArrayList<>();
        for (Size option : choices)
        {
            if ( option.getWidth() <= maxPreviewWidth && option.getHeight() <= maxPreviewHeight && option.getHeight() == option.getWidth() * viewHeight / viewWidth)
            {
                if (option.getWidth() >= viewWidth && option.getHeight() >= viewHeight)
                {
                    biggerSizes.add(option);
                }
                else
                {
                    smallerSizes.add(option);
                }
            }
        }

        if (biggerSizes.size() > 0)
        {
            return Collections.min(biggerSizes, comparator);
        }
        else if (smallerSizes.size() > 0)
        {
            return Collections.max(smallerSizes, comparator);
        }
        else
        {
            return choices[0];
        }

    }

    //调整图片方向
    public void orientationTransform()
    {

        int orientation = mContext.getResources().getConfiguration().orientation;
        //调整view的宽高比例与preview的宽高比例一致
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        }
        else
        {
            mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
        }

        //当横屏时调整view宽高
        int rotation = ((Activity)mContext).getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);//view
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());//因为相机方向传感器默认横屏采集,高>宽
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation)
        {

            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);//放大或缩小到view的大小
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);

        }

        //该方法不影响view的大小和位置,只对view中的内容起作用
        mTextureView.setTransform(matrix);

    }

    //请求摄像头权限
    public void requestPermission() throws CameraAccessException
    {

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)//检测是否拥有访问摄像头的权限
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.CAMERA)) //检测是否需要显示申请权限对话框,6.0以下恒为false,因为还没有默认的对话框
            {
                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.CAMERA}, 1); //显示对话框去请求权限,发出请求后，会在 onRequestPermissionsResult 方法中获得回调结果
            }
        }
        else
        {
            openCamera();
        }

    }

    //打开摄像头
    public void openCamera() throws CameraAccessException
    {

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mCameraManager.openCamera(mCameraId, mCameraStateCallback, mHandler);

    }

    //创建预览session
    public void createPreviewSession() throws CameraAccessException
    {

        mImageReader = ImageReader.newInstance(jpegSize.getWidth(), jpegSize.getHeight(), ImageFormat.YUV_420_888, 1);    //注意这里设置宽高会影响预览
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mHandler);

        /**
         * request需要为camera指定一组目标surface
         */
        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture);

        ArrayList<Surface> surfaces = new ArrayList<>();
        surfaces.add(surface);
        surfaces.add(mImageReader.getSurface());

        mRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mRequestBuilder.addTarget(surface);
        mCameraDevice.createCaptureSession(surfaces, mSessionStateCallback, mHandler);

    }

    //预览时加入新的surface
    public void createPreviewSession(Surface mSurface) throws CameraAccessException
    {

        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.YUV_420_888, 1);    //注意这里设置宽高会影响预览
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mHandler);

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(texture);

        ArrayList<Surface> surfaces = new ArrayList<>();
        surfaces.add(previewSurface);
        surfaces.add(mSurface);

        mRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mRequestBuilder.addTarget(previewSurface);
        mRequestBuilder.addTarget(mSurface);
        mCameraDevice.createCaptureSession(surfaces, mSessionStateCallback, mHandler);

    }

    public void createCodecSession() throws CameraAccessException
    {

        /**
         * YUV_420_888表示YUV420格式的集合 888表示Y、U、V分量中每个颜色占8bit
         * YUV_420根据颜色数据存储顺序不同,又分为多种不同格式:YUV420Planar(I420)、YUV420PackedPlanar(NV12)、YUV420SemiPlanar(NV21)
         */
        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.JPEG, 1);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mHandler);

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture);

        ArrayList<Surface> surfaces = new ArrayList<>();
        surfaces.add(surface);
        surfaces.add(mImageReader.getSurface());

        mRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mRequestBuilder.addTarget(surface);
        mRequestBuilder.addTarget(mImageReader.getSurface());
        mCameraDevice.createCaptureSession(surfaces, mSessionStateCallback, mHandler);

    }

    public void createCodecSession(Surface surface) throws CameraAccessException
    {

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(texture);
        ArrayList<Surface> surfaces = new ArrayList<>();
        surfaces.add(previewSurface);
        surfaces.add(surface);

        mRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        mRequestBuilder.addTarget(previewSurface);
        mRequestBuilder.addTarget(surface);
        mCameraDevice.createCaptureSession(surfaces, mSessionStateCallback, mHandler);

    }

    //锁定对焦
    public void lockFocus() throws CameraAccessException
    {
        mRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);//设置自动对焦触发模式
        mState = STATE_WATTING_LOCK;
        mCameraCaptureSession.capture(mRequestBuilder.build(), mCaptureCallback, mHandler);

    }

    //释放锁定对焦并回到预览模式
    public void unlockFocus() throws CameraAccessException
    {
        mRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
        mRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        mCameraCaptureSession.capture(mRequestBuilder.build(), mCaptureCallback, mHandler);

        //返回预览
        mState = STATE_PREVIEW;
        mCameraCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mHandler);
    }

    //设置预览请求
    public void updatePreview() throws CameraAccessException
    {
        mPreviewRequest = mRequestBuilder.build();
        mCameraCaptureSession.setRepeatingRequest(mPreviewRequest, null, mHandler);
    }

    //拍照
    public void takePicture()
    {

        try
        {

            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.addTarget(mImageReader.getSurface());
            int rotation = ((Activity)mContext).getWindowManager().getDefaultDisplay().getRotation();
            builder.set(CaptureRequest.JPEG_ORIENTATION, (ORIENTATIONS.get(rotation) + mSensorOrientation + 270)%360);

            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result)
                {
                    super.onCaptureCompleted(session, request, result);
                    try
                    {

                        unlockFocus();

                    }catch (CameraAccessException e)
                    {
                        e.printStackTrace();
                    }
                }
            };

            mCameraCaptureSession.stopRepeating();
            mCameraCaptureSession.capture(builder.build(), captureCallback, mHandler);

        }catch (CameraAccessException e)
        {
            e.printStackTrace();
        }

    }

    //保存图片
    public void savePicture(final ImageReader reader, String imgName)
    {

        final File file = new File(mContext.getExternalCacheDir(), imgName);
        if (file.exists())
        {
            file.delete();
        }

        try
        {

            file.createNewFile();

        }catch (IOException e)
        {
            e.printStackTrace();
        }

        int fileType = ImageSaver.FILE_TYPE_JPEG;
        if (imgName.endsWith("jpg"))
        {
            fileType = ImageSaver.FILE_TYPE_JPEG;
        }
        else if (imgName.endsWith("yuv"))
        {
            fileType = ImageSaver.FILE_TYPE_YUV;
        }

        mHandler.post(new ImageSaver(reader, file, fileType));

    }

    //释放所有资源
    public void release()
    {
        mCameraDevice.close();
        mCameraCaptureSession.close();
        mCameraDevice = null;
        mCameraCaptureSession = null;
    }

}
