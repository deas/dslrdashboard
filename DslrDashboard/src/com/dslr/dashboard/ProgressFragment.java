package com.dslr.dashboard;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressFragment extends DslrFragmentBase {

	private static final String TAG = "ProgressFragment";
	
	private TextView mProgressStatusMessage, mProgressFilename, mProgressBracketing, mProgressTimelapse, mProgressFocusStacking;
	private ProgressBar mProgressDownload;
	private Button mProgressStopTimelapse, mProgressStopFocusStacking;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_progress, container, false);
		mProgressStatusMessage = (TextView)view.findViewById(R.id.progress_status_msg);
		mProgressFilename = (TextView)view.findViewById(R.id.progress_filename);
		mProgressDownload = (ProgressBar)view.findViewById(R.id.progress_download);
		mProgressBracketing = (TextView)view.findViewById(R.id.progress_custombracketing);
		mProgressTimelapse = (TextView)view.findViewById(R.id.progress_timelapse);
		mProgressStopTimelapse = (Button)view.findViewById(R.id.progress_stop_timelapse);
		mProgressFocusStacking = (TextView)view.findViewById(R.id.progress_focusstacking);
		mProgressStopFocusStacking = (Button)view.findViewById(R.id.progress_stop_focusstacking);
		
		mProgressStopTimelapse.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				getPtpDevice().stopTimelapse();
				mProgressStopTimelapse.setVisibility(View.GONE);
			}
		});
		mProgressStopFocusStacking.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				getPtpDevice().stopFocusStacking();
				mProgressStopFocusStacking.setVisibility(View.GONE);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void internalPtpPropertyChanged(PtpProperty property) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void ptpDeviceEvent(PtpDeviceEvent event, Object data) {
		switch (event) {
		case BusyBegin:
			mProgressStatusMessage.setText("Camera operation start");
			break;
		case BusyEnd:
			mProgressBracketing.setVisibility(View.GONE);
			mProgressTimelapse.setVisibility(View.GONE);
			mProgressStopTimelapse.setVisibility(View.GONE);
			mProgressFocusStacking.setVisibility(View.GONE);
			mProgressStopFocusStacking.setVisibility(View.GONE);
			mProgressDownload.setVisibility(View.GONE);
			break;
		case CaptureStart:
		case CaptureInitiated:
			mProgressStatusMessage.setText("Capture start");
			
			if (getPtpDevice().getNeedBracketing()) {
				mProgressBracketing.setVisibility(View.VISIBLE);
				mProgressBracketing.setText(String.format("Custom bracketing image %d of %d", getPtpDevice().getCurrentBracketing() + 1, getPtpDevice().getBracketingCount()));
			}
			else
				mProgressBracketing.setVisibility(View.GONE);
			
			if (getPtpDevice().getIsTimelapseRunning()) {
				mProgressTimelapse.setVisibility(View.VISIBLE);
				mProgressTimelapse.setText(String.format("Timelapse image %d of %d", getPtpDevice().getTimelapseIterations() - getPtpDevice().getTimelapseRemainingIterations() , getPtpDevice().getTimelapseIterations()));
				mProgressStopTimelapse.setVisibility(View.VISIBLE);
			}
			else {
				mProgressTimelapse.setVisibility(View.GONE);
				mProgressStopTimelapse.setVisibility(View.GONE);
			}
			if (getPtpDevice().getIsInFocusStacking()) {
				mProgressFocusStacking.setVisibility(View.VISIBLE);
				mProgressFocusStacking.setText(String.format("Focus stacking image %d of %d", getPtpDevice().getCurrentFocusStackingImage(), getPtpDevice().getFocusImages()));
				if (!getPtpDevice().getStopFocusStacking())
					mProgressStopFocusStacking.setVisibility(View.VISIBLE);
			}
			else {
				mProgressFocusStacking.setVisibility(View.GONE);
				mProgressStopFocusStacking.setVisibility(View.GONE);
			}
//			mProgressMessage1.setText("Image capture started");
			break;
		case CaptureComplete:
			mProgressStatusMessage.setText("Capture complete");
//			mProgressMessage1.setText("Image capture finished");
			break;
		case ObjectAdded:
			mProgressStatusMessage.setText("Object added");
			mProgressDownload.setVisibility(View.GONE);
			if (data != null) {
				PtpObjectInfo obj = (PtpObjectInfo)data;
				mProgressFilename.setText(obj.filename);
			}
			break;
		case GetObjectFromSdramInfo:
			mProgressStatusMessage.setText("Got image info");
			if (data != null) {
				ImageObjectHelper obj = (ImageObjectHelper)data;
				if (obj.galleryItemType == ImageObjectHelper.PHONE_PICTURE) {
					mProgressFilename.setText(obj.file.getName());
					mProgressDownload.setVisibility(View.VISIBLE);
					mProgressDownload.setProgress(0);
					mProgressDownload.setMax(obj.objectInfo.objectCompressedSize);
				}
				else {
					mProgressFilename.setText(obj.objectInfo.filename);
				}
			}
			break;
		case GetObjectFromSdramThumb:
			mProgressStatusMessage.setText("Got image thumb");
//			mProgressMessage1.setText("Got image thumb");
			break;
		case GetObjectFromSdramProgress:
			mProgressStatusMessage.setText("Downloading image");
			if (data != null) {
				ImageObjectHelper obj = (ImageObjectHelper)data;
				mProgressDownload.setProgress(obj.progress);
			}
			break;
		case GetObjectFromSdramFinished:
			mProgressStatusMessage.setText("Image transfered from Camera SDRAM");
			mProgressDownload.setVisibility(View.GONE);
			break;
		}
	}

	@Override
	protected void internalSharedPrefsChanged(SharedPreferences prefs,
			String key) {
		// TODO Auto-generated method stub
		
	}

}
