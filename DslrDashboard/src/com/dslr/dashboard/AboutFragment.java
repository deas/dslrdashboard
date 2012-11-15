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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends DslrFragmentBase {

	private final static String TAG = "AboutFragment";
	
	private TextView mAbout, mNoCamera;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about, container, false);
		
		mAbout = (TextView)view.findViewById(R.id.txt_about);
		mNoCamera = (TextView)view.findViewById(R.id.txt_nocamera);
		
		mAbout.setText(Html.fromHtml(getActivity().getString(R.string.about)));
		mNoCamera.setText(Html.fromHtml(getActivity().getString(R.string.nocamera)));
		
		return view;
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
		case PtpDeviceInitialized:
			mNoCamera.setVisibility(View.GONE);
			break;
		case PtpDeviceStoped:
			mNoCamera.setVisibility(View.VISIBLE);
			break;
		}
		
	}

	@Override
	protected void internalSharedPrefsChanged(SharedPreferences prefs,
			String key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
	}
	
	@Override
	public void onResume() {
		Log.d(TAG, "onResume");
		mNoCamera.setVisibility(View.VISIBLE);
		if (getPtpDevice() != null && getPtpDevice().getIsPtpDeviceInitialized())
			mNoCamera.setVisibility(View.GONE);
			
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
	
}
