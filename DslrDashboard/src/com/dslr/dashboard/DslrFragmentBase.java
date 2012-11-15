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
