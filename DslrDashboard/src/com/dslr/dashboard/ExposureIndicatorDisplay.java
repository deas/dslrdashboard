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
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ExposureIndicatorDisplay extends SurfaceView implements
		SurfaceHolder.Callback, Runnable {

	private static final String TAG = "ExposureIndicatorDisplay";

	private SurfaceHolder mHolder;
	private int mFrameWidth;
	private int mFrameHeight;
	private float mValue = 0;

	private Context _context;
	private Paint mPaint;

	private boolean mThreadRun;
	private final Object _syncRoot = new Object();

	public ExposureIndicatorDisplay(Context context, AttributeSet attrs) {
		super(context, attrs);
		_context = context;
		mHolder = getHolder();
		mHolder.addCallback(this);

		mPaint = new Paint();
		mPaint.setColor(0xff99cc00);
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.FILL);
		mPaint.setStrokeWidth(2);

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

		processValue(mValue);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "Surface created");
		setWillNotDraw(false);
		(new Thread(this)).start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "Surface destroyed");
		mThreadRun = false;
	}

	public void processValue(float value) {
		mValue = value;
		synchronized (_syncRoot) {
			Log.d(TAG, "Process value");
			_syncRoot.notify();
		}
	}

	public void setDefaultImage() {
		mValue = 0;
		synchronized (_syncRoot) {
			// mLvo = null;
			_syncRoot.notify();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
	}

	@Override
	protected void onAttachedToWindow() {
		setDefaultImage();
		super.onAttachedToWindow();
	}

	private void doDraw(Canvas canvas) {
		canvas.drawColor(Color.BLACK);

		float xCenter = mFrameWidth / 2;
		float yCenter = mFrameHeight / 2;

		canvas.drawRect(xCenter - 3, yCenter - 3, xCenter + 3,
				(2 * yCenter) - 6, mPaint);

		float ds = (xCenter - 20) / 3;

		for (int i = 1; i <= 3; i++) {
			float dsx = i * ds;
			canvas.drawRect(xCenter + dsx - 3, yCenter - 3, xCenter + dsx + 3,
					yCenter + 3, mPaint);
			canvas.drawRect(xCenter - dsx - 3, yCenter - 3, xCenter - dsx + 3,
					yCenter + 3, mPaint);
		}
		mPaint.setTextSize(18);
		String str = String.format("%.1f EV", mValue);
		float tw = mPaint.measureText(str);
		canvas.drawText(str, xCenter - (tw / 2), yCenter - 6, mPaint);
		tw = mPaint.measureText("+");
		canvas.drawText("+", xCenter + (3 * ds) - (tw / 2), yCenter - 6, mPaint);
		tw = mPaint.measureText("-");
		canvas.drawText("-", xCenter - (3 * ds) - (tw / 2), yCenter - 6, mPaint);

		if (mValue != 0) {
			float limit = xCenter + (3 * ds);

			if (Math.abs(mValue) < 3)
				limit = xCenter + (Math.abs(mValue) * ds);

			float multi = Math.signum(mValue);

			float start = xCenter + (5 * multi);
			float counter = xCenter + 5;

			while (counter < limit) {
				canvas.drawRect(start, yCenter + 6, start + (2 * multi),
						(2 * yCenter) - 6, mPaint);
				start += 4 * multi;
				counter += 4;

			}

			if (Math.abs(mValue) >= 3) {
				Path p = new Path();
				p.moveTo(xCenter + (((3 * ds) - 4) * multi), yCenter + 6);
				p.lineTo(xCenter + (((3 * ds) + 2) * multi), yCenter + 6);
				p.lineTo(xCenter + (((3 * ds) + 10) * multi), yCenter + 6
						+ ((yCenter - 12) / 2));
				p.lineTo(xCenter + (((3 * ds) + 2) * multi), (2 * yCenter) - 6);
				p.lineTo(xCenter + (((3 * ds) - 4) * multi), (2 * yCenter) - 6);
				canvas.drawPath(p, mPaint);
			}
		}
	}

	public void run() {
		mThreadRun = true;
		Log.d(TAG, "Starting Exposure indicator processing thread");
		while (mThreadRun) {
			synchronized (_syncRoot) {
				try {
					_syncRoot.wait();
				} catch (InterruptedException e) {
					Log.d(TAG, "interruped");
				}
			}
			//Log.d(TAG, "Drawing Indicator display");
			try {
				Canvas canvas = mHolder.lockCanvas();
				if (canvas != null) {

					doDraw(canvas);
					mHolder.unlockCanvasAndPost(canvas);
				}
			} catch (Exception e) {
				Log.e(TAG, "Exposure indicator drawing exception: " + e.getMessage());
			}

		}
	}

}