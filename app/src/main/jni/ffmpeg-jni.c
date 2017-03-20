//
// Created by Huangheting on 16/12/18.
//
#include <jni.h>
#include "include/libavcodec/avcodec.h"

AVCodecContext *pCodecCtx = NULL;
AVCodec *pCodec;
AVCodecID codec_id = AV_CODEC_ID_H264;
File *fp_out;
AVFrame *pFrame;
AVPacket *pPacket;

char filename_out[] = "ds.h264";

JNIEXPORT jstring JNICALL
Java_com_supertramp_multimedia_1android_utils_FFMpegUtil_avcodec_1init(JNIEnv *env, jobject instance, jint mWidth, jint mHeight) {

    avcodec_register_all();

    pCodec = avcodec_find_encoder(codec_id);
    if (!pCodec)
    {
        printf("Codec not found\n");
        return -1;
    }

    pCodecCtx = avcodec_alloc_context3(pCodec);
    if(!pCodecCtx)
    {
        printf("Could not allocate video codec context\n");
        return -1;
    }
    pCodecCtx -> bit_rate = 400000;
    pCodecCtx -> width = mWidth;
    pCodecCtx -> height = mHeight;
    pCodecCtx->time_base.num=1;
    pCodecCtx->time_base.den=25;
    pCodecCtx->gop_size = 10;
    pCodecCtx->max_b_frames = 1;
    pCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;



}
