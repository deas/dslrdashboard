package com.dslr.dashboard;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class LeftFragment extends DslrFragmentBase {

	private static final String TAG = "LeftFragment";
	
	private LinearLayout mLeftLayout, mLeftScrollLayout;
	private CheckableImageView mInitiateCapture, mAutoFocus, mMovieRec;
	private PorterDuffColorFilter mColorFilterRed;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_left, container, false);

        mColorFilterRed = new PorterDuffColorFilter(getResources().getColor(R.color.Red), android.graphics.PorterDuff.Mode.SRC_ATOP);
        
        mLeftLayout = (LinearLayout)view.findViewById(R.id.left_layout);
        mLeftScrollLayout = (LinearLayout)view.findViewById(R.id.left_scroll_layout);
        
		mInitiateCapture = (CheckableImageView)view.findViewById(R.id.initiatecapture);
		mAutoFocus = (CheckableImageView)view.findViewById(R.id.autofocus);
		mMovieRec = (CheckableImageView)view.findViewById(R.id.movierec);
		
		
		mInitiateCapture.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				getPtpDevice().initiateCaptureCmd(true);
			}
		});
		mInitiateCapture.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				getPtpDevice().setCaptureToSdram(!getPtpDevice().getCaptureToSdram());
				//mInitiateCapture.setColorFilter(getPtpDevice().getCaptureToSdram() ? mColorFilterRed : null);
				return true;
			}
		});
		
		mAutoFocus.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				getPtpDevice().startAfDriveCmd();
			}
		});
		mAutoFocus.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				getPtpDevice().setAFBeforeCapture(!getPtpDevice().getAFBeforeCapture());
				setAfColor();
				return true;
			}
		});

		mMovieRec.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (!getPtpDevice().getIsMovieRecordingStarted()) 
					getPtpDevice().startMovieRecCmd();
				else
					getPtpDevice().stopMovieRecCmd();
						
			}
		});
		
		return view;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, "onAttach");
	}
	
	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
	}
	
	@Override
	public void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
	}
	
	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
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
	public void onDetach() {
		Log.d(TAG, "onDetach");
		super.onDetach();
	}
	
	@Override
	protected void internalInitFragment() {
		if (getIsPtpDeviceInitialized()) {
			if (getPtpDevice().getIsLiveViewEnabled()){
				// toggle movie recording button;
				toggleMovieRecVisibility();
			}
			else
				mMovieRec.setVisibility(View.GONE);

			if (getPtpDevice().getIsCommandSupported(PtpCommand.AfDrive)) {
				mAutoFocus.setVisibility(View.VISIBLE);
				setAfColor();
			} 
			else
				mAutoFocus.setVisibility(View.GONE);
			
			if (getPtpDevice().getCaptureToSdram())
				mInitiateCapture.setColorFilter(mColorFilterRed);
			else
				mInitiateCapture.setColorFilter(null);
		}
	}

	private void toggleMovieRecVisibility(){
		if (getPtpDevice().getIsCommandSupported(PtpCommand.StartMovieRecInCard))
		{
			mMovieRec.setVisibility(View.VISIBLE);
			if (getPtpDevice().getIsMovieRecordingStarted())
				mMovieRec.setColorFilter(mColorFilterRed);
			else
				mMovieRec.setColorFilter(null);
		}
		else
			mMovieRec.setVisibility(View.GONE);
	}
	@Override
	protected void internalPtpPropertyChanged(PtpProperty property) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void ptpDeviceEvent(PtpDeviceEvent event, Object data) {
		switch (event) {
		case LiveviewStart:
			toggleMovieRecVisibility();
			break;
		case LiveviewStop:
			mMovieRec.setVisibility(View.GONE);
			break;
		case MovieRecordingStart:
			mMovieRec.setColorFilter(mColorFilterRed);
			break;
		case MovieRecordingEnd:
			mMovieRec.setColorFilter(null);
			break;
		case BusyBegin:
			Log.d(TAG, "Busy begin");
			DslrHelper.getInstance().enableDisableControls(mLeftLayout, false);
			DslrHelper.getInstance().enableDisableControls(mLeftScrollLayout, false);
			break;
		case BusyEnd:
			Log.d(TAG, "Busy end");
			DslrHelper.getInstance().enableDisableControls(mLeftLayout, true);
			DslrHelper.getInstance().enableDisableControls(mLeftScrollLayout, true);
			break;
		case RecordingDestinationChanged:
				mInitiateCapture.setColorFilter(getPtpDevice().getCaptureToSdram() ? mColorFilterRed : null);
			break;
		}
	}

	private void setAfColor(){
		if (getPtpDevice().getAFBeforeCapture())
			mAutoFocus.setColorFilter(mColorFilterRed);
		else
			mAutoFocus.setColorFilter(null);
	}
	
	@Override
	protected void internalSharedPrefsChanged(SharedPreferences prefs,
			String key) {
		if (key.equals(PtpDevice.PREF_KEY_SHOOTING_AF)) {
			setAfColor();
		}
	}

}
