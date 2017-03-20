package com.supertramp.multimedia_android.utils;

/**
 * Created by supertramp on 16/12/19.
 */

public class FFMpegUtil {

    public FFMpegUtil()
    {
        System.loadLibrary("ffmpeg-jni");
        System.loadLibrary("ffmpeg");
    }

    public native static String avcodec_init();



}
