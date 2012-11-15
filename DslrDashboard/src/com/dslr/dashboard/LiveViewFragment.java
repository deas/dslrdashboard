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

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class LiveViewFragment extends DslrFragmentBase {

	private LvSurfaceBase mLvSurface;
	private boolean mIsAttached = false;
	private final static String TAG = "LiveViewFragment";
	private LiveviewThread mLvThread = null;
	private ImageView mLvHistgoram, mOsdToggle;
	private int mHistogramMode = 0; //0 - no histogram, 1 - separate, 2 - one
	private PorterDuffColorFilter mColorFilterRed;
	
	private ImageView mFocsuToggle;
	private RelativeLayout mFocusLayout;
	private CheckableImageView mFocusMin, mFocusMax;
	private AutoRepeatImageView mFocusLeft, mFocusRight;
	private TextView mFocusStepDisplay;
	private SeekBar mFocusStepSeekBar;
	private int mFocusStep = 10;
	private int mOsdMode = 3;
	
	private boolean mFocusLayoutVisible = false;
	
	@Override
	public void onAttach(Activity activity) {
		mIsAttached = true;
		super.onAttach(activity);
	}
	
	@Override
	public void onDetach() {
		mIsAttached = false;
		super.onDetach();
	}
	
	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
	}
	
	@Override
	public void onResume() {
		Log.d(TAG, "onResume");
		mLvThread = new LiveviewThread();
		mLvThread.start();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
		stopLvThread();
		super.onPause();
	}
	@Override
	public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.liveview_fragment, container, false);
		
        mColorFilterRed = new PorterDuffColorFilter(getResources().getColor(R.color.Red), android.graphics.PorterDuff.Mode.SRC_ATOP);
		
        mLongPressTimeout = ViewConfiguration.getLongPressTimeout(); 		
        mScaledTouchSlop = ViewConfiguration.get(getActivity()).getScaledTouchSlop(); 		
        mVibrator = (Vibrator)getActivity().getSystemService("vibrator");
        mScaledMaximumFlingVelocity = ViewConfiguration.get(getActivity()).getScaledMaximumFlingVelocity();
        
        mOsdToggle = (ImageView)view.findViewById(R.id.lvosdtoggle);
        mFocsuToggle = (ImageView)view.findViewById(R.id.lvfocustoggle);
        mFocusLayout = (RelativeLayout)view.findViewById(R.id.lvfocuslayout);
        mFocusMin = (CheckableImageView)view.findViewById(R.id.lvfocusmin);
        mFocusMax = (CheckableImageView)view.findViewById(R.id.lvfocusmax);
        mFocusLeft = (AutoRepeatImageView)view.findViewById(R.id.lvfocusleft);
        mFocusRight = (AutoRepeatImageView)view.findViewById(R.id.lvfocusright);
        
        mFocusStepDisplay = (TextView)view.findViewById(R.id.lvfocusstep);
        mFocusStepSeekBar = (SeekBar)view.findViewById(R.id.lvfocusstepseekbar);
        
        mFocusLayout.setVisibility(View.GONE);
        
        mFocusStepSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					mFocusStep = progress;
					mFocusStepDisplay.setText(Integer.toString(mFocusStep));
				}
			}
		});
        
        mFocusMin.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				getPtpDevice().seekFocusMin();
			}
		});
        mFocusMax.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				getPtpDevice().seekFocusMax();
			}
		});
        mFocusLeft.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				getPtpDevice().seekFocus(1, mFocusStep);
			}
		});
        mFocusRight.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				getPtpDevice().seekFocus(2, mFocusStep);
			}
		});
        mFocsuToggle.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mFocusLayoutVisible = !mFocusLayoutVisible;
				mFocusLayout.setVisibility(mFocusLayoutVisible ? View.VISIBLE : View.GONE);
				mFocsuToggle.setColorFilter(mFocusLayoutVisible ? mColorFilterRed : null);
			}
		});
        
        mOsdToggle.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (mOsdMode == 0) 
					mOsdMode = 3;
				else
					mOsdMode--;
			}
		});
        
        mLvHistgoram = (ImageView)view.findViewById(R.id.lv_histogram);
        mLvHistgoram.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (mHistogramMode == 2)
					mHistogramMode = 0;
				else
					mHistogramMode++;
				if (mHistogramMode > 0)
					mLvHistgoram.setColorFilter(mColorFilterRed);
				else
					mLvHistgoram.setColorFilter(null);
			}
		});
        
		mLvSurface = (LvSurfaceBase)view.findViewById(R.id.lvsurface);
		
		mLvSurface.setOnTouchListener(new View.OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {

				if (getIsPtpDeviceInitialized()){
		        final int action = event.getAction();
		        final float x = event.getX();
		        final float y = event.getY();

		        if (mVelocityTracker == null) {
		            mVelocityTracker = VelocityTracker.obtain();
		        }
		        mVelocityTracker.addMovement(event);

		        switch (action) {
		            case MotionEvent.ACTION_DOWN:
		                v.postDelayed(mLongPressRunnable, mLongPressTimeout);
		                mDownX = x;
		                mDownY = y;
		                calculateMfDriveStep(v.getHeight(), y);
		                break;

		            case MotionEvent.ACTION_MOVE: {
		                switch(mMode)
		                {
		                case TOUCH:
		                    final float scrollX = mDownX - x;
		                    final float scrollY = mDownY - y;

		                    final float dist = (float)Math.sqrt(scrollX * scrollX + scrollY * scrollY);

		                    if (dist >= mScaledTouchSlop) {
		                        v.removeCallbacks(mLongPressRunnable);
		                        mMode = TouchMode.PAN;
		                    }
		                	break;
		                case PAN:
		                	break;
		                case LVZOOM:

			                int mdy = Math.round(((x - mDownX) / v.getWidth()) * 10);
							if (mdy > mZoomY){
								zoomLiveView(true);
							}
							else if (mdy < mZoomY){
								zoomLiveView(false);
							}
							mZoomY = mdy;
		                	
		                	break;
		                case LVFOCUSSTART:
		                	Log.d(TAG, "Focus start");
		                	getPtpDevice().seekFocusMin();
		                	mMode = TouchMode.LVFOCUS;
		                	break;
		                case LVFOCUSEND:
		                	Log.d(TAG, "Focus end");
		                	getPtpDevice().seekFocusMax();
		                	mMode = TouchMode.LVFOCUS;
		                	break;
		                case LVFOCUS:
//		                	int focusValue = Math.round((_dslrHelper.getPtpService().getPreferences().mFocusMax * x) / v.getWidth());
//		                	_dslrHelper.getPtpService().seekFocus(focusValue);
		                	
		                	break;
		                }

		                break;
		            }

		            case MotionEvent.ACTION_UP:
		            	switch(mMode)
		            	{
		            	case LVZOOM:
		            		break;
		            	case LVFOCUS:
		            		break;
		            	case PAN:
		            		if ((x - mDownX) > 200){
		            			getPtpDevice().initiateCaptureCmd();
		            			Log.d(TAG, "Pan left to right");
		            		}
//		            		else if ((mDownX - x) > 200) {
//		            			_dslrHelper.getPtpService().initiateCaptureRecInSdramCmd();
//		            			Log.d(TAG, "Pan right to lef");
//		            		}
		            		else if ((y - mDownY) > 200) {
		            			if (getPtpDevice().getIsMovieRecordingStarted())
		            				getPtpDevice().stopMovieRecCmd();
		            			else
		            				getPtpDevice().startMovieRecCmd();
		            			Log.d(TAG, "Pan top to bottom");
		            		}
//		            		else if ((mDownY - y) > 200) {
//		            			Log.d(TAG, "Pan bottom to top");
//		            		}
//		            		
//		                    mVelocityTracker.computeCurrentVelocity(1000, mScaledMaximumFlingVelocity);
		            		break;
		            	case TOUCH:
    						lvAfx = x;
    						lvAfy = y;
		            		long currentTime = System.currentTimeMillis();
		            		if ((currentTime - lastTime) < 200) {
		            			lastTime = -1;
		            			v.removeCallbacks(mSingleTapRunnable);
//		            			lvSurfaceDoubleTap();
		            		} else {
		            			lastTime = currentTime;
		            			v.postDelayed(mSingleTapRunnable, 200);
		            		}
		            		
		            		break;
		            	}
		                mVelocityTracker.recycle();
		                mVelocityTracker = null;
		                v.removeCallbacks(mLongPressRunnable);
		                mMode = TouchMode.TOUCH;
		                break;

		            default:
		                mVelocityTracker.recycle();
		                mVelocityTracker = null;
		                v.removeCallbacks(mLongPressRunnable);
		                mMode = TouchMode.TOUCH;
		                break;

		        }
				}
		        return true;
		        	
			}

		}); 		
		return view;
	}
	
    public enum TouchMode {
        TOUCH, PAN, LVZOOM, LVFOCUS, LVFOCUSSTART, LVFOCUSEND
    } 

	private long lastTime = -1; 
	
    private int mfDriveStep = 200;

    /** Time of tactile feedback vibration when entering zoom mode */
    private static final long VIBRATE_TIME = 50;

    /** Current listener mode */
    private TouchMode mMode = TouchMode.TOUCH;

    /** X-coordinate of latest down event */
    private float mDownX;

    /** Y-coordinate of latest down event */
    private float mDownY;

    private int mZoomY;
    private float focusX;
    
    /** Velocity tracker for touch events */
    private VelocityTracker mVelocityTracker;

    /** Distance touch can wander before we think it's scrolling */
    private int mScaledTouchSlop;

    /** Duration in ms before a press turns into a long press */
    private int mLongPressTimeout;

    /** Vibrator for tactile feedback */
    private Vibrator mVibrator;

    /** Maximum velocity for fling */
    private int mScaledMaximumFlingVelocity;
    
    private boolean mLiveViewNeedFocusChanged = false;
	
	private Object _syncRoot = new Object();
	
    private float lvAfx, lvAfy; 
    
    private final Runnable mLongPressRunnable = new Runnable() {
        public void run() {
        	if (mDownY < 100)
        	{
        		mMode = TouchMode.LVZOOM;
        		mZoomY = 0;
                mVibrator.vibrate(VIBRATE_TIME);
        	}
        	else {
        		if (mDownX < 150)
        			mMode = TouchMode.LVFOCUSSTART;
        		else if (mDownX > (mLvSurface.getWidth() - 150))
        			mMode = TouchMode.LVFOCUSEND;
        		else
        			mMode = TouchMode.LVFOCUS;
        		focusX = mDownX;
                mVibrator.vibrate(VIBRATE_TIME);
        	}
        }
    };
    
    private final Runnable mSingleTapRunnable = new Runnable() {
		
		public void run() {
			synchronized (_syncRoot) {
				mLiveViewNeedFocusChanged = true;
			}
		}
	};
	
    private void calculateMfDriveStep(int height, float y){
    	float inv = height - y;
    	float step = inv * 0.205f;
    	int testy = Math.round(inv * step);
    	if (testy < 1)
    		testy = 1;
    	else if (testy > 32767)
    		testy = 32767;
    	mfDriveStep = testy;
    } 
    
	private void zoomLiveView(boolean up){
		IDslrActivity activity = (IDslrActivity)getActivity();
		if (activity != null)
			activity.zoomLiveView(up);
		
	}     
	public synchronized void updateLiveViewImage(PtpLiveViewObject lvo) {
		lvo.parse(mLvSurface.getFrameWidth(), mLvSurface.getFrameHeight());
		
		if (mLiveViewNeedFocusChanged){
			mLiveViewNeedFocusChanged = false;
			float x = ((lvAfx / lvo.sDw) / lvo.ox) + lvo.dLeft;
			float y = ((lvAfy / lvo.sDh) / lvo.oy) + lvo.dTop;
			 getPtpDevice().changeAfAreaCmd((int)x, (int)y);
		}
		
		LvSurfaceHelper helper = new LvSurfaceHelper(lvo, getPtpDevice().getPtpProperty(PtpProperty.LiveViewImageZoomRatio), mOsdMode);
		helper.mHistogramEnabled = mHistogramMode > 0;
		helper.mHistogramSeparate = mHistogramMode == 1;
		
		mLvSurface.processLvObject(helper);
	}

	@Override
	protected void internalInitFragment() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void internalPtpPropertyChanged(PtpProperty property) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void ptpDeviceEvent(PtpDeviceEvent event, Object data) {
		switch(event) {
			case LiveviewStop:
				if (mLvThread != null)
					mLvThread.interrupt();
				break;
		}
	}
	
	private void stopLvThread() {
		Log.d(TAG, "Stop LV thread");
		if (mLvThread != null) {
			mLvThread.setIsEnabled(false);
			mLvThread.interrupt();
		}
	}
	
	private class LiveviewThread extends ThreadBase {

		private Boolean mIsEnabled = true;
		
		public synchronized void setIsEnabled(boolean isEnabled) {
			mIsEnabled = true;
		}
		public LiveviewThread() {
			mSleepTime = 100;
		}
		
		@Override
		public void codeToExecute() {
			try{
				if (!interrupted()) {
					if (mIsEnabled) {
						PtpLiveViewObject lvo = getPtpDevice().getLiveViewImage();
						if (lvo != null)
							updateLiveViewImage(lvo);
						else {
							Log.d(TAG, "null lvo");
							this.interrupt();
						}
					}
				}
			} catch (Exception e) {
				Log.d(TAG, "Live view thread exception " + e.getMessage());
			}
		}
		
	}

	@Override
	protected void internalSharedPrefsChanged(SharedPreferences prefs,
			String key) {
		// TODO Auto-generated method stub
		
	}
	
}
