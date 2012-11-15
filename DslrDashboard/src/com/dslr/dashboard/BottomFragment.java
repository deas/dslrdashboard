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

import java.util.Hashtable;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BottomFragment extends DslrFragmentBase {
	
	private static final String TAG = "BottomFragment";
	
	private RelativeLayout mBottomLayout;
	private ImageView mFullScreen, mFlashIndicator, mEvCompensationIndicator, mFlashCompensationIndicator, mLvLayoutSwitch;
	private TextView txtAperture, txtShutter, mSdcard1, mSdcard2, mAeLockStatus;
	private PorterDuffColorFilter mColorFilterGreen, mColorFilterRed;
	private ExposureIndicatorDisplay mExposureIndicator;
	
	private View.OnClickListener mSdcardClickListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			Intent ipIntent = new Intent(getActivity(), DslrImageBrowserActivity.class);
			ipIntent.setAction(Intent.ACTION_VIEW);
			ipIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getActivity().startActivity(ipIntent); 			
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_bottom, container, false);
		
		mBottomLayout = (RelativeLayout)view.findViewById(R.id.bottom_layout);
		
		txtAperture = (TextView)view.findViewById(R.id.txt_aperture);
		txtAperture.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Log.d(TAG, "Aperture click");
				DslrHelper.getInstance().showApertureDialog(getActivity());
				//showApertureDialog();
			}
		});
		
		txtShutter = (TextView)view.findViewById(R.id.txt_shutter);
		txtShutter.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().showExposureTimeDialog(getActivity());
				//showExposureTimeDialog();
			}
		});
//		mFlexibleProgram = (TextView)view.findViewById(R.id.flexibleprogram);
//		mExposureIndicateStatus = (TextView)view.findViewById(R.id.exposureindicatestatus);
		
		mFullScreen = (ImageView)view.findViewById(R.id.fullscreen);
		mLvLayoutSwitch = (ImageView)view.findViewById(R.id.lvlayoutswitch);
		
		mFlashIndicator = (ImageView)view.findViewById(R.id.flashindicator);
		mEvCompensationIndicator = (ImageView)view.findViewById(R.id.evcompensationindicator);
		mFlashCompensationIndicator = (ImageView)view.findViewById(R.id.flashcompensationindicator);
		mExposureIndicator = (ExposureIndicatorDisplay)view.findViewById(R.id.exposureindicator);
		
        mColorFilterGreen = new PorterDuffColorFilter(getResources().getColor(R.color.HoloGreenLight), android.graphics.PorterDuff.Mode.SRC_ATOP);
        mColorFilterRed = new PorterDuffColorFilter(getResources().getColor(R.color.Red), android.graphics.PorterDuff.Mode.SRC_ATOP);
        
        mFullScreen.setColorFilter(mColorFilterGreen);
        mFlashIndicator.setColorFilter(mColorFilterGreen);
        mEvCompensationIndicator.setColorFilter(mColorFilterGreen);
        mFlashCompensationIndicator.setColorFilter(mColorFilterGreen);
        mLvLayoutSwitch.setColorFilter(mColorFilterGreen);
		
		mFullScreen.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				IDslrActivity activity = getDslrActivity();
				if (activity != null) {
					activity.toggleFullScreen();
					if (activity.getIsFullScreen()) {
						mFullScreen.setImageResource(R.drawable.full_screen_return);
						mLvLayoutSwitch.setVisibility(View.INVISIBLE);
					}
					else {
						mFullScreen.setImageResource(R.drawable.full_screen);
						mLvLayoutSwitch.setVisibility(View.VISIBLE);
					}
				}
			}
		});
		mLvLayoutSwitch.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				IDslrActivity activity = getDslrActivity();
				if (activity != null) {
					activity.toggleLvLayout();
					if (activity.getIsLvLayoutEnabled())
						mLvLayoutSwitch.setImageResource(R.drawable.initiate_capture);
					else
						mLvLayoutSwitch.setImageResource(R.drawable.i0xd1a20x0000);
				}
			}
		});
		
		mSdcard1 = (TextView)view.findViewById(R.id.txt_sdcard1);
		mSdcard2 = (TextView)view.findViewById(R.id.txt_sdcard2);
		
		mSdcard1.setOnClickListener(mSdcardClickListener);
		mSdcard2.setOnClickListener(mSdcardClickListener);
		
		mAeLockStatus = (TextView)view.findViewById(R.id.txt_aelockstatus);
		return view;
	}
	
	public void initFragment() {
		Log.d(TAG, "initFragment");
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
		try {
		if (getIsPtpDeviceInitialized()) {
			IDslrActivity activity = getDslrActivity();
			internalPtpPropertyChanged(getPtpDevice().getPtpProperty(PtpProperty.FStop));
			internalPtpPropertyChanged(getPtpDevice().getPtpProperty(PtpProperty.ExposureTime));
			internalPtpPropertyChanged(getPtpDevice().getPtpProperty(PtpProperty.AeLockStatus));
			internalPtpPropertyChanged(getPtpDevice().getPtpProperty(PtpProperty.InternalFlashPopup));
			internalPtpPropertyChanged(getPtpDevice().getPtpProperty(PtpProperty.InternalFlashStatus));
			internalPtpPropertyChanged(getPtpDevice().getPtpProperty(PtpProperty.ExposureBiasCompensation));
			internalPtpPropertyChanged(getPtpDevice().getPtpProperty(PtpProperty.InternalFlashCompensation));
			internalPtpPropertyChanged(getPtpDevice().getPtpProperty(PtpProperty.FlexibleProgram));
			mExposureIndicator.invalidate();
			internalPtpPropertyChanged(getPtpDevice().getPtpProperty(PtpProperty.ExposureIndicateStatus));
			
			Hashtable<Integer, PtpStorageInfo> tmp = getPtpDevice().getPtpStorages(); 
			PtpStorageInfo[] storages = tmp.values().toArray(new PtpStorageInfo[tmp.values().size()]);
			mSdcard1.setText("[E]");
			mSdcard2.setVisibility(View.INVISIBLE);
			if (storages.length > 0) {
				if (storages.length > 1) {
					mSdcard2.setVisibility(View.VISIBLE);
					mSdcard2.setText(String.format("[%d]",  storages[1].freeSpaceInImages));
				}
				mSdcard1.setText(String.format("[%d]",  storages[0].freeSpaceInImages));
			}
			mFullScreen.setVisibility(getPtpDevice().getIsLiveViewEnabled() ? View.VISIBLE : View.INVISIBLE);
			if (activity != null) {
				if (activity.getIsFullScreen())
					mLvLayoutSwitch.setVisibility(View.INVISIBLE);
				else
					mLvLayoutSwitch.setVisibility(mFullScreen.getVisibility());
			}
		}
		} catch (Exception e) {
			Log.e(TAG, "Exception: " + e.getMessage());
		}
	}

	@Override
	protected void internalPtpPropertyChanged(PtpProperty property) {
		if (property != null) {
			switch (property.getPropertyCode()) {
			case PtpProperty.FStop:
				updateAperture(property);
				break;
			case PtpProperty.ExposureTime:
				updateExposureTime(property);
				break;
			case PtpProperty.AeLockStatus:
				if ((Integer)property.getValue() == 1)
					mAeLockStatus.setVisibility(View.VISIBLE);
				else
					mAeLockStatus.setVisibility(View.INVISIBLE);
				break;
			case PtpProperty.InternalFlashPopup:
				if ((Integer)property.getValue() == 1) {
					mFlashIndicator.setVisibility(View.VISIBLE);
				}
				else
					mFlashIndicator.setVisibility(View.INVISIBLE);
				break;
			case PtpProperty.InternalFlashStatus:
				Integer flashStatus = (Integer)property.getValue(); 
				Log.d(TAG, "Flash status " + flashStatus);
				if (flashStatus == 1)
					// flash ready
					mFlashIndicator.setColorFilter(mColorFilterGreen);
				else
					// flash charging
					mFlashIndicator.setColorFilter(mColorFilterRed);
				break;
			case PtpProperty.ExposureBiasCompensation:
				if ((Integer)property.getValue() != 0)
					mEvCompensationIndicator.setVisibility(View.VISIBLE);
				else
					mEvCompensationIndicator.setVisibility(View.INVISIBLE);
				break;
			case PtpProperty.InternalFlashCompensation:
				if ((Integer)property.getValue() != 0)
					mFlashCompensationIndicator.setVisibility(View.VISIBLE);
				else
					mFlashCompensationIndicator.setVisibility(View.INVISIBLE);
				break;
			case PtpProperty.FlexibleProgram:
				Integer val = (Integer)property.getValue();
				//mFlexibleProgram.setText(val.toString());
		    	mExposureIndicator.processValue((float)val / 6);
				break;
			case PtpProperty.ExposureIndicateStatus:
				Integer eval = (Integer)property.getValue();
				//mExposureIndicateStatus.setText(eval.toString());
		    	mExposureIndicator.processValue((float)eval / 6);
		    	
				break;
			default:
				break;
			}
		}
	}
	
	private void updateAperture(PtpProperty property) {
			int fStop = (Integer)property.getValue();
			txtAperture.setVisibility(View.VISIBLE);
			txtAperture.setText("F" + (double)fStop / 100);
			txtAperture.setEnabled(property.getIsWritable()); 			 			
	}
	
	private void updateExposureTime(PtpProperty property) {
			Long nesto = (Long)property.getValue();
			Log.i(TAG, "Exposure " + nesto);
			//double value = 1 / ((double)nesto / 10000);
			if (nesto == 4294967295L)
				txtShutter.setText("Bulb");
			else {
				if (nesto >= 10000)
					txtShutter.setText(String.format("%.1f \"", (double)nesto / 10000));
				else
					txtShutter.setText(String.format("1/%.1f" , 10000 / (double)nesto));
			}
			txtShutter.setEnabled(property.getIsWritable()); 			 			
	}
	
	@Override
	protected void ptpDeviceEvent(PtpDeviceEvent event, Object data) {
		int card;
		TextView txtCard = null;
		switch(event) {
			case LiveviewStart:
				mFullScreen.setVisibility(View.VISIBLE);
				mLvLayoutSwitch.setVisibility(View.VISIBLE);
				break;
			case LiveviewStop:
				mFullScreen.setVisibility(View.INVISIBLE);
				mLvLayoutSwitch.setVisibility(View.INVISIBLE);
				break;
			case SdCardInserted:
			case SdCardRemoved:
				card = (Integer)data;
				txtCard = card == 1 ? mSdcard1 : mSdcard2;
				if (txtCard != null) {
					txtCard.setVisibility(View.VISIBLE);
					if (event == PtpDeviceEvent.SdCardRemoved) {
						txtCard.setText(" [E] ");
					}
				}
			break;
			case SdCardInfoUpdated:
				PtpStorageInfo sInfo = (PtpStorageInfo)data;
				card = sInfo.storageId >> 16;
				txtCard = card == 1 ? mSdcard1 : mSdcard2;
				if (txtCard != null)
					txtCard.setText(String.format("[%d]", sInfo.freeSpaceInImages));
			break;
			case BusyBegin:
				Log.d(TAG, "BusyBegin");
				DslrHelper.getInstance().enableDisableControls(mBottomLayout, false, false);
				break;
			case BusyEnd:
				Log.d(TAG, "BusyEnd");
				DslrHelper.getInstance().enableDisableControls(mBottomLayout, true, false);
				break;
		} 	
	}

	@Override
	protected void internalSharedPrefsChanged(SharedPreferences prefs,
			String key) {
		// TODO Auto-generated method stub
		
	}
}
