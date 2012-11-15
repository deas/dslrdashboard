/*
	<DslrDashboard - controling DSLR camera with Android phone/tablet>
    Copyright (C) <2012>  <Zoltan Hubai>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
 */

package com.dslr.dashboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class LvSurfaceBase extends SurfaceView implements
		SurfaceHolder.Callback, Runnable {

	private static final String TAG = "LvSurfaceBase";

	private SurfaceHolder mHolder;
	private int mFrameWidth;
	private int mFrameHeight;
	private LvSurfaceHelper mHelper = null;
	private Paint mPaintBlack, mPaintRed, mPaintGreen, mPaintBlue, mPaintYellow;
	private Thread mDrawThread;

	private Context mContext;

	private boolean mThreadRun;
	private final Object mSyncRoot = new Object();

	public LvSurfaceBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mHolder = getHolder();
		mHolder.addCallback(this);

        mPaintBlack = new Paint();
        mPaintBlack.setStyle(Paint.Style.FILL);
        mPaintBlack.setColor(Color.BLACK);
        
        mPaintYellow = new Paint();
        mPaintYellow.setStyle(Paint.Style.FILL);
        mPaintYellow.setColor(Color.YELLOW);
        
        mPaintRed = new Paint();
        mPaintRed.setStyle(Paint.Style.FILL);
        mPaintRed.setColor(Color.RED);
        
        mPaintGreen = new Paint();
        mPaintGreen.setStyle(Paint.Style.FILL);
        mPaintGreen.setColor(Color.GREEN);
        
        mPaintBlue = new Paint();
        mPaintBlue.setStyle(Paint.Style.FILL);
        mPaintBlue.setColor(Color.BLUE);
		
		
//		mPaint = new Paint();
//		mPaint.setColor(Color.GREEN);
//		mPaint.setAntiAlias(true);
//		mPaint.setStyle(Style.STROKE);
//		mPaint.setStrokeWidth(2);
		
		Log.d(TAG, "Created new " + this.getClass());
	}

	public int getFrameWidth() {
		return mFrameWidth;
	}

	public int getFrameHeight() {
		return mFrameHeight;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "Surface changed");

		mFrameWidth = width;
		mFrameHeight = height;

	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "Surface created");
		mDrawThread = new Thread(this);
		mDrawThread.start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "Surface destroyed");
		mDrawThread.interrupt();
		mThreadRun = false;
	}

	public void processLvObject(LvSurfaceHelper helper) {
		synchronized (mSyncRoot) {
			mHelper = helper;
			mSyncRoot.notify();
		}
	}

	long mTime = 0;
	
	public void setDefaultImage() {
		synchronized (mSyncRoot) {
			mHelper = null;
			// mLvo = null;
			mSyncRoot.notify();
		}
	}

	private void doDraw(Canvas canvas) {
		long time = System.currentTimeMillis();
		double fps = 0;
		if (mTime != 0)
			fps = 1000 / (time - mTime);
		mTime = time;
		
		Bitmap mBitmap = null;
		if (canvas != null) {
			if (mHelper == null || mHelper.mLvo == null) {
				canvas.drawColor(Color.BLACK);
				return;
			}
			mBitmap = BitmapFactory.decodeByteArray(mHelper.mLvo.data,
					mHelper.mLvo.imgPos, mHelper.mLvo.imgLen);// processLvImage(mLvo);
			if (mBitmap != null) {

				canvas.drawBitmap(mBitmap, new Rect(0, 0,
						mHelper.mLvo.jpegImageSize.horizontal,
						mHelper.mLvo.jpegImageSize.vertical), new RectF(0, 0,
						mFrameWidth, mFrameHeight), null);


				mPaintGreen.setStyle(Paint.Style.STROKE);
				mPaintGreen.clearShadowLayer();
				mPaintYellow.setStyle(Paint.Style.STROKE);
				mPaintYellow.clearShadowLayer();
				mPaintRed.setStyle(Paint.Style.STROKE);
				mPaintRed.clearShadowLayer();
				
				// draw focus rect
				canvas.drawRect(mHelper.mLvo.afRects[0], mPaintGreen);

				// draw rects for face detection
				if (mHelper.mLvo.faceDetectionPersonNo > 0) {
				
					for(int i = 1; i <= mHelper.mLvo.faceDetectionPersonNo; i++){
						canvas.drawRect(mHelper.mLvo.afRects[i], mPaintYellow);
					}
				}

//				 mPaint.setTextSize(20);
//				 mPaint.setShadowLayer(3, 1, 1, Color.BLACK);
//				 canvas.drawText(String.format("%.1f", fps), 100, 20, mPaint);
				
				float fontSize = (mFrameWidth * 32) / 1280;
				mPaintBlack.setTextSize(fontSize);
				mPaintGreen.setTextSize(fontSize);
				mPaintRed.setTextSize(fontSize);
				mPaintYellow.setTextSize(fontSize);
				
				String tmpStr;
				 if ((mHelper.mOsdDisplay & 1) == 1) {
					 //mPaintGreen.setStrokeWidth(1);
					 float thirdx = mFrameWidth / 3;
					 float thirdy = mFrameHeight / 3;
				
					 if (mHelper.mLvZoom != null && (Integer)mHelper.mLvZoom.getValue() == 0) {
						 canvas.drawLine(thirdx, 0, thirdx, mFrameHeight, mPaintGreen);
						 canvas.drawLine(2 * thirdx, 0, 2 * thirdx, mFrameHeight, mPaintGreen);
						 canvas.drawLine(0, thirdy, mFrameWidth, thirdy, mPaintGreen);
						 canvas.drawLine(0, thirdy * 2, mFrameWidth, thirdy * 2, mPaintGreen);
					 }
				 }
				 
				 mPaintGreen.setShadowLayer(3, 1, 1, Color.BLACK);
				 mPaintRed.setShadowLayer(3, 1, 1, Color.BLACK);
				 mPaintYellow.setShadowLayer(3, 1, 1, Color.BLACK);
				 
				 if ((mHelper.mOsdDisplay & 2) == 2) {
					 //mPaintGreen.setTextSize(20);
					 
					 canvas.drawText(String.format("LV remain %d s", mHelper.mLvo.countDownTime), 20, 25, mPaintGreen);
					 if ((mHelper.mLvo.shutterSpeedUpper / mHelper.mLvo.shutterSpeedLower) >= 1)
						 tmpStr = String.format("f %.1f     %.1f \"", (double)mHelper.mLvo.apertureValue / 100, (double)(mHelper.mLvo.shutterSpeedUpper / mHelper.mLvo.shutterSpeedLower));
					 else
						 tmpStr = String.format("f %.1f     %d / %d", (double)mHelper.mLvo.apertureValue / 100, mHelper.mLvo.shutterSpeedUpper, mHelper.mLvo.shutterSpeedLower);
					 
					 if (mHelper.mLvo.hasApertureAndShutter) {
						 float tWidth = mPaintGreen.measureText(tmpStr);
						 canvas.drawText(tmpStr, (mFrameWidth / 2) - (tWidth / 2) , 25, mPaintGreen);
					 }
				
					 if (mHelper.mLvo.focusDrivingStatus == 1)
						 canvas.drawText("AF", 70, 60, mPaintGreen);
				
//					 canvas.drawText(String.format("Focus %d / %d", mHelper.mFocusCurrent, mHelper.mFocusMax ), 80, 75, mPaintGreen);
				
//					 switch(mHelper.mTouchMode){
//					 	case LVFOCUS:
//					 		canvas.drawText("Manual focus", 80, 105, mPaintGreen);
//					 		canvas.drawText(String.format("Focus step: %d", mMfDriveStep), 50, 370, mPaintGreen);
//					 		break;
//					 	case LVZOOM:
//					 		canvas.drawText("Zoom", 80, 105, mPaintGreen);
//					 		break;
//					 }

					 
				 if (mHelper.mLvo.hasRolling) {
					 tmpStr = String.format("Rolling    %.2f",  mHelper.mLvo.rolling);
					 canvas.drawText(tmpStr, mFrameWidth - mPaintGreen.measureText(tmpStr) - 20, 105, mPaintGreen);
				 }
				 if (mHelper.mLvo.hasPitching) {
					 tmpStr = String.format("Pitching  %.2f", mHelper.mLvo.pitching);
					 canvas.drawText(tmpStr, mFrameWidth - mPaintGreen.measureText(tmpStr) - 20, 135, mPaintGreen);
				 }
				 if (mHelper.mLvo.hasYawing) {
					 tmpStr = String.format("Yawing   %.2f",	 mHelper.mLvo.yawing);
					 canvas.drawText(tmpStr, mFrameWidth - mPaintGreen.measureText(tmpStr) - 20, 165, mPaintGreen);
				 }
				 if (mHelper.mLvo.movieRecording) {
					 tmpStr = String.format("REC remaining %d s", mHelper.mLvo.movieRecordingTime / 1000);
					 canvas.drawText(tmpStr, mFrameWidth - mPaintGreen.measureText(tmpStr) - 20, 75, mPaintRed);
				 }

				 mPaintGreen.setStyle(Paint.Style.FILL);
				 mPaintYellow.setStyle(Paint.Style.FILL);
				 mPaintRed.setStyle(Paint.Style.FILL);
				 
				 switch(mHelper.mLvo.focusingJudgementResult){
				 case 0:
				 	 canvas.drawCircle(50, 50, 10, mPaintYellow);
					 break;
				 case 1:
					 canvas.drawCircle(50, 50, 10, mPaintRed);
					 break;
				 case 2:
				 	 canvas.drawCircle(50, 50, 10, mPaintGreen);
					 break;
				 }
				 // focus position drawin
//				 	paint.setColor(Color.GREEN);
//				 	paint.setStyle(Style.FILL_AND_STROKE);
//				
//				 	float fx = (mFrameWidth * mHelper.mFocusCurrent) /	mHelper.mFocusMax;
//				 	canvas.drawCircle(fx, mFrameHeight - 20, 10, paint);
				 }
				
				if (mHelper.mHistogramEnabled) {
					
					Bitmap histBmp = NativeMethods.getInstance().createHistogramBitmap(mBitmap,mHelper.mHistogramSeparate);
					int hWidth = histBmp.getWidth();
					int hHeight = histBmp.getHeight();
					
					if (hWidth > (int)(mFrameWidth / 3)) {
						//hHeight = (int)(hHeight * (hWidth / (mFrameWidth / 3)) );
						hWidth = (int)(mFrameWidth / 3);
					}
					
					if (hHeight > mFrameHeight) {
						//hWidth = (int)(hWidth * (hHeight / mFrameHeight));
						hHeight = mFrameHeight;
					}
					canvas.drawBitmap(histBmp, new Rect(0, 0,
						histBmp.getWidth(), histBmp.getHeight()),
						new RectF(0, 0,	hWidth, hHeight), null);
					histBmp.recycle();
				}

				mBitmap.recycle();
				

				
			} else {
				canvas.drawColor(Color.BLACK);
			}
		}
	}

	public void run() {
		mThreadRun = true;
		Log.d(TAG, "Starting LV image processing thread");
		while (mThreadRun) {
			synchronized (mSyncRoot) {
				try {
					mSyncRoot.wait();
				} catch (InterruptedException e) {
					Log.d(TAG, "Draw thread interrupted");
				}
			}
			try {
				Canvas canvas = mHolder.lockCanvas();

				doDraw(canvas);

				mHolder.unlockCanvasAndPost(canvas);
			} catch (Exception e) {
				Log.e(TAG, "LVSurface draw exception: " + e.getMessage());
			}

		}
	}

}