/*
 * dslrdashboard.h
 *
 *  Created on: Nov 16, 2012
 *      Author: hubaiz
 */

#include <string.h>
#include <jni.h>
#include <cstring>
#include <sstream>
#include <iostream>
#include <math.h>
#include <time.h>
#include <algorithm>
#include <android/log.h>
#include <android/bitmap.h>
#include <exiv2/exiv2.hpp>
#include <libraw/libraw.h>
#include <netinet/in.h>

#ifndef DSLRDASHBOARD_H_
#define DSLRDASHBOARD_H_


#define LOG_TAG "dslrdashboard_jni"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

typedef struct
{
	uint8_t red;
	uint8_t green;
	uint8_t blue;
	uint8_t alpha;
} argb;

typedef Exiv2::ExifData::const_iterator (*EasyAccessFct)(const Exiv2::ExifData& ed);


extern "C" {
JNIEXPORT jobject JNICALL Java_com_dslr_dashboard_NativeMethods_loadRawImage(
	JNIEnv* env, jobject thiz, jstring imgPath);


JNIEXPORT jboolean JNICALL Java_com_dslr_dashboard_NativeMethods_loadRawImageThumb(
	JNIEnv* env, jobject thiz, jstring imgPath, jstring thumbPath);

//JNIEXPORT jobjectArray JNICALL Java_com_dslr_dashboard_NativeMethods_getExifInfo(
//	JNIEnv* env, jobject thiz, jstring imgPath);

JNIEXPORT jint JNICALL Java_com_dslr_dashboard_NativeMethods_getExifData(
	JNIEnv* env, jobject thiz, jstring imgPath, jint count, jobject callback);

JNIEXPORT jint JNICALL Java_com_dslr_dashboard_NativeMethods_setGPSExifData(
	JNIEnv* env, jobject thiz, jstring imgPath, jdouble latitude, jdouble longitude, jdouble altitude);

JNIEXPORT jint JNICALL Java_com_dslr_dashboard_NativeMethods_copyExifData(
	JNIEnv* env, jobject thiz, jstring source, jstring target);

JNIEXPORT jint JNICALL Java_com_dslr_dashboard_NativeMethods_getRGBHistogram(
	JNIEnv* env, jobject thiz, jobject lvImage, jintArray rArray, jintArray gArray, jintArray bArray, jintArray lumaArray );
}



#endif /* DSLRDASHBOARD_H_ */
