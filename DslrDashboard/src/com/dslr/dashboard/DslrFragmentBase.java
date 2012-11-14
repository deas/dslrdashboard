package com.dslr.dashboard;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.view.View;

public abstract class DslrFragmentBase extends Fragment {
	private IDslrActivity mActivity = null;
	private boolean mIsAttached = false;
	private PtpDevice mPtpDevice = null;
	
	protected IDslrActivity getDslrActivity() {
		return mActivity;
	}
	protected PtpDevice getPtpDevice() {
		return mPtpDevice;
	}
	protected boolean getIsAttached() {
		return mIsAttached;
	}
	protected boolean getIsPtpDeviceInitialized() {
		return mPtpDevice != null ? mPtpDevice.getIsPtpDeviceInitialized() : false;
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try{
			mActivity = (IDslrActivity)activity;
			mPtpDevice = mActivity.getPtpDevice();
			mIsAttached = true;
		} catch (ClassCastException e) {
			mActivity = null;
		}
		
	}
	
	
	@Override
	public void onDetach() {
		mIsAttached = false;
		mActivity = null;
		super.onDetach();
	}
	
	public void initFragment(){
		if (mIsAttached)
			internalInitFragment();
	}
	
	public void ptpPropertyChanged(PtpProperty property) {
		internalPtpPropertyChanged(property);
	}
	public void sharedPrefsChanged(SharedPreferences prefs, String key) {
		internalSharedPrefsChanged(prefs, key);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		internalInitFragment();
	}
	
	protected void initializePtpPropertyView(View view, PtpProperty property) {
		if (property == null)
			view.setVisibility(View.GONE);
		else {
			view.setVisibility(View.VISIBLE);
			internalPtpPropertyChanged(property);
		}
	}
	
	protected abstract void internalInitFragment();
	
	protected abstract void internalPtpPropertyChanged(PtpProperty property);
	
	protected abstract void ptpDeviceEvent(PtpDeviceEvent event, Object data);
	
	protected abstract void internalSharedPrefsChanged(SharedPreferences prefs, String key);
}
