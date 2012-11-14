package com.dslr.dashboard;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class TimelapseDialog {

	private final static String TAG = "TimelapseDialog";
	
	private Context mContext;
	private DslrHelper mDslrHelper;
	private PtpDevice mPtpDevice;
	private View mView;

	private TextView txtCaptureDuration;
	private TextView txtMovieLength;
	private TextView txtInterval;
	private TextView txtFrameRate;
	private TextView txtFrameCount;
	private Button mStartTimelapse;
	private SeekBar mSeekBar;

	private CustomDialog mDialog;
	
	private TimeSpan mCaptureDuration;
	private TimeSpan mMovieLength;
	private TimeSpan mInterval;
	
	private int mFrameRate = 25;
	private int mFrameCount= 0; 
	
	public TimelapseDialog(Context context) {
		mContext = context;
		mDslrHelper = DslrHelper.getInstance();
		mPtpDevice = mDslrHelper.getPtpDevice();
		
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mView = inflater.inflate(R.layout.timelapsedialog, null);
	
		mCaptureDuration = new TimeSpan(0, 0, 0);
		mMovieLength = new TimeSpan(0, 0, 0);
		mInterval = new TimeSpan(0, 0, 0);

		txtCaptureDuration = (TextView)mView.findViewById(R.id.txtcaptureduration);
		txtMovieLength = (TextView)mView.findViewById(R.id.txtmovielength);
		txtInterval = (TextView)mView.findViewById(R.id.txtinterval);
		txtFrameRate = (TextView)mView.findViewById(R.id.txtframerate);
		txtFrameCount = (TextView)mView.findViewById(R.id.txtframecount);
		mStartTimelapse = (Button)mView.findViewById(R.id.timelapse_start);
		mSeekBar = (SeekBar)mView.findViewById(R.id.timelapse_framerate);
		
		mSeekBar.setProgress(mFrameRate);
		
		if (mPtpDevice != null) {
			Log.d(TAG, "Interval: " + mPtpDevice.getTimelapseInterval());
			Log.d(TAG, "Iterations: " + mPtpDevice.getTimelapseIterations());
			
			
			mInterval = TimeSpan.FromMilliseconds(mPtpDevice.getTimelapseInterval());
			mFrameCount = mPtpDevice.getTimelapseIterations();
			mMovieLength = TimeSpan.FromSeconds(mFrameCount / mFrameRate);
			mCaptureDuration = TimeSpan.FromSeconds(mInterval.TotalSeconds() * mFrameCount);
		}
//		calculateRecordingLength();
		updateDisplay();
		
		txtCaptureDuration.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				TimePickerDialog tpd = new TimePickerDialog(mContext, "", mCaptureDuration, new TimePickerDialog.OnTimeSetListener() {
					
					public void onTimeSet(int hourOfDay, int minute, int second) {
						
						mCaptureDuration = new TimeSpan(hourOfDay, minute, second);
						mFrameCount = (int)(mCaptureDuration.TotalMilliseconds() / mInterval.TotalMilliseconds());
						mMovieLength = TimeSpan.FromSeconds(mFrameCount * mFrameRate);
						updateDisplay();
						
						//calculateInterval();
					}
				});
				tpd.show();
			}
		});
		txtMovieLength.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				new TimePickerDialog(mContext, "", mMovieLength, new TimePickerDialog.OnTimeSetListener() {
					
					public void onTimeSet(int hourOfDay, int minute, int second) {
						mMovieLength = new TimeSpan(hourOfDay, minute, second);
						mFrameCount = (int)(mMovieLength.TotalSeconds() * mFrameRate);
						mCaptureDuration = TimeSpan.FromMilliseconds(mInterval.TotalMilliseconds() * mFrameCount);
						updateDisplay();
						//calculateFrameCount();
						//calculateInterval();
					}
				}).show();
			}
		});
		txtInterval.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				new TimePickerDialog(mContext, "", mInterval, new TimePickerDialog.OnTimeSetListener() {
					
					public void onTimeSet(int hourOfDay, int minute, int second) {
						mInterval = new TimeSpan(hourOfDay, minute, second);
						mCaptureDuration = TimeSpan.FromMilliseconds(mInterval.TotalMilliseconds() * mFrameCount);
						updateDisplay();
						//calculateRecordingLength();
					}
				}).show();
			}
		});
		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					if (progress > 0) {
					mFrameRate = progress;
					mFrameCount = (int)(mMovieLength.TotalSeconds() * mFrameRate);
					mCaptureDuration = TimeSpan.FromMilliseconds(mInterval.TotalMilliseconds() * mFrameCount);
					updateDisplay();
					}
				}
				
			}
		});
//		txtFrameRate.addTextChangedListener(new TextWatcher() {
//			
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			public void beforeTextChanged(CharSequence s, int start, int count,
//					int after) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			public void afterTextChanged(Editable s) {
//				try {
//					mFrameRate = Integer.parseInt(s.toString());
//					
//					//calculateFrameCount();
//					//calculateInterval();
//				} catch (NumberFormatException e) {
//					
//				}
//			}
//		});
		
		mStartTimelapse.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Log.d(TAG, "Interval: " + mInterval.TotalMilliseconds());
				Log.d(TAG, "Iterations: " + mFrameCount);
				mPtpDevice.startTimelapse((long)mInterval.TotalMilliseconds(), mFrameCount);
				mDialog.dismiss();
			}
		});
	}
	
	private void calculateRecordingLength(){
		double recordingLength = mFrameRate * mMovieLength.TotalSeconds() * mInterval.TotalSeconds();
		mCaptureDuration = TimeSpan.FromSeconds(recordingLength);
		txtCaptureDuration.setText(mCaptureDuration.toString());
		Log.d(TAG, "Recording length " + recordingLength / 3600);
	}
	private void calculateInterval() {
		double frameCount = mFrameRate * mMovieLength.TotalSeconds();
		double interval = 0;
		if (frameCount > 0)
			interval = (mCaptureDuration.TotalHours() * 3600) / frameCount;
		mInterval = TimeSpan.FromSeconds(interval);
		txtInterval.setText(mInterval.toString());
		
		Log.d(TAG, "interval " + interval);
	}
	private void calculateFrameCount(){
		mFrameCount = (int)(mMovieLength.TotalSeconds() * mFrameRate);
		txtFrameCount.setText(String.format("%d", mFrameCount));
	}
	
	private void updateDisplay(){
		txtFrameRate.setText(String.format("%d", mFrameRate));
		txtCaptureDuration.setText(mCaptureDuration.toString());
		txtMovieLength.setText(mMovieLength.toString());
		txtInterval.setText(mInterval.toString());
		txtFrameCount.setText(String.format("%d", mFrameCount));
	} 
	
	public void show() {
        CustomDialog.Builder customBuilder = new CustomDialog.Builder(mContext);
        customBuilder.setTitle("Timelapse settings")
        	.setContentView(mView);
        
        mDialog = customBuilder
        	.create();
        mDialog.show();
	}
	
	
}
