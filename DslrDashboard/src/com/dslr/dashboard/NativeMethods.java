// Copyright 2012 by Zoltan Hubai <hubaiz@gmail.com>
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version as long the source code includes
// this copyright notice.
// 

package com.dslr.dashboard;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public class NativeMethods {
	private final static String TAG = "NativeMethods";
	
    static {
        System.loadLibrary("dslrdashboard");
    }
	
    public native Object loadRawImage(String imgPath);
    public native boolean loadRawImageThumb(String imgPath, String thumbPath);
    //public native String[] getExifInfo(String imgPath);
    public native int getExifData(String path, int count, Object obj);
    public native int setGPSExifData(String path, double latitude, double longitude, double altitude);
    public native int copyExifData(String source, String target);
    public native int getRGBHistogram(Bitmap lvImage, int[] rArray, int[] gArray, int[] bArray, int[] lumaArray);
    
    private static NativeMethods _instance = null;
    
    public static NativeMethods getInstance() {
    	if (_instance == null)
    		_instance = new NativeMethods();
    	return _instance;
    }
    
    private NativeMethods() {
        mPaintBlack = new Paint();
        mPaintBlack.setStyle(Paint.Style.FILL);
        mPaintBlack.setColor(Color.BLACK);
        mPaintBlack.setTextSize(25);
        mPaintBlack.setAlpha(100);

        mPaintWhite = new Paint();
        mPaintWhite.setStyle(Paint.Style.STROKE);
        mPaintWhite.setColor(Color.WHITE);
        mPaintWhite.setTextSize(25);
        
        mPaintYellow = new Paint();
        mPaintYellow.setStyle(Paint.Style.FILL);
        mPaintYellow.setColor(Color.YELLOW);
        mPaintYellow.setTextSize(25);
        
        mPaintRed = new Paint();
        mPaintRed.setStyle(Paint.Style.FILL);
        mPaintRed.setColor(Color.RED);
        mPaintRed.setTextSize(25);
        mPaintRed.setAlpha(180);
        
        mPaintGreen = new Paint();
        mPaintGreen.setStyle(Paint.Style.FILL);
        mPaintGreen.setColor(Color.GREEN);
        mPaintGreen.setTextSize(25);
        mPaintGreen.setAlpha(180);
        
        mPaintBlue = new Paint();
        mPaintBlue.setStyle(Paint.Style.FILL);
        mPaintBlue.setColor(Color.BLUE);
        mPaintBlue.setTextSize(25);
        mPaintBlue.setAlpha(180);
    }
    
    public ArrayList<ExifDataHelper> getImageExif(String[] exifNames, String path){
    	ArrayList<ExifDataHelper> helper = new ArrayList<ExifDataHelper>();
    	for(String name : exifNames) {
    		String[] tmp = name.split("@");
    		helper.add(new ExifDataHelper(tmp[0], tmp[1]));
    	}
    	
    	getExifData(path, helper.size(), helper);
    	return helper;
    }
    
	public void exifValueCallback(String test, int index, Object obj) {
		ArrayList<ExifDataHelper> helper = (ArrayList<ExifDataHelper>)obj;
		//Log.d(TAG, "Exif value callback " + test + " index: " + index + " helper test: " + helper.size());
		helper.get(index).mExifValue = test;
	}
	public String exifNameCallback(int index, Object obj) {
		ArrayList<ExifDataHelper> helper = (ArrayList<ExifDataHelper>)obj;
		//Log.d(TAG, "Name callback " + helper.get(index).mExifName + " index: " + index + " helper test: " + helper.size());
		return helper.get(index).mExifName;
	}
	
	private Paint mPaintWhite;
	private Paint mPaintBlack;
	private Paint mPaintYellow;
	private Paint mPaintRed;
	private Paint mPaintGreen;
	private Paint mPaintBlue;
	
	private int[] mrArray = new int[257];
	private int[] mgArray = new int[257];
	private int[] mbArray = new int[257];
	private int[] mlumaArray = new int[257];
	private float mHistogramWidth = 450;
	private float mHistogramHeight = 150;
	private float dx = (float)mHistogramWidth / (float)256;
	
	public Bitmap createHistogramBitmap(Bitmap source, boolean separate) {
		getRGBHistogram(source, mrArray, mgArray, mbArray, mlumaArray);
	
		if (source != null) {
		Log.d(TAG, "drawHistogram");
		
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		int cHeight = separate ? (int)mHistogramHeight * 4 : (int)mHistogramHeight * 2;
		Bitmap hist = Bitmap.createBitmap((int)mHistogramWidth, cHeight, conf);
				
		Canvas canvas = new Canvas(hist);
		
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mPaintBlack);
		//canvas.drawRect(1, 1, canvas.getWidth()-1, canvas.getHeight()-1, mPaintWhite);
		int count = separate ? 4 : 2;
		for (int i = 0; i < count ; i++) {
			int hpos = (int)mHistogramHeight * i;
			canvas.drawRect(1,  hpos + 1, canvas.getWidth() - 1, hpos + (int)mHistogramHeight - 1, mPaintWhite);
		}
		
		int maxVal = Math.max(mrArray[256], mbArray[256]);
		maxVal = Math.max(maxVal, mgArray[256]);
		
		for (int i =0; i < 256; i++ ){
			float left = (float)i * dx;// + canvas.getWidth() - mHistogramWidth;
			
			if (!separate) {
				canvas.drawRect(left, mHistogramHeight -1, left + dx, mHistogramHeight - (((float)mlumaArray[i] * mHistogramHeight)/ (float)mlumaArray[256]) +1, mPaintYellow);
				canvas.drawRect(left, (mHistogramHeight * 2) -1, left + dx, (mHistogramHeight * 2) - (((float)mrArray[i] * mHistogramHeight)/ (float)mrArray[256]) +1, mPaintRed);
				canvas.drawRect(left, (mHistogramHeight * 2) -1, left + dx, (mHistogramHeight * 2) - (((float)mbArray[i] * mHistogramHeight)/ (float)mbArray[256]) +1, mPaintBlue);
				canvas.drawRect(left, (mHistogramHeight * 2) -1, left + dx, (mHistogramHeight * 2) - (((float)mgArray[i] * mHistogramHeight)/ (float)mgArray[256]) +1 , mPaintGreen);
			} else {
				canvas.drawRect(left, mHistogramHeight -1, left + dx, mHistogramHeight - (((float)mlumaArray[i] * mHistogramHeight)/ (float)mlumaArray[256]) +1, mPaintYellow);
				canvas.drawRect(left, (mHistogramHeight * 2) -1, left + dx, (mHistogramHeight * 2) - (((float)mrArray[i] * mHistogramHeight)/ (float)mrArray[256]) +1, mPaintRed);
				canvas.drawRect(left, (mHistogramHeight * 4) -1, left + dx, (mHistogramHeight * 4 ) - (((float)mbArray[i] * mHistogramHeight)/ (float)mbArray[256]) +1, mPaintBlue);
				canvas.drawRect(left, (mHistogramHeight * 3) -1, left + dx, (mHistogramHeight * 3 )- (((float)mgArray[i] * mHistogramHeight)/ (float)mgArray[256]) +1 , mPaintGreen);
				
			}
			
		}
		return hist;
		}
		else
			return null;
	}
    
} 