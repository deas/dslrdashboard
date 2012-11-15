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
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class MainMenuProvider extends ActionProvider {

	private static final String TAG = "MainMenuProvider";
	
	private Context mContext;
	
	private LinearLayout mMenuLayout;
	private CheckableImageView mShootingMode, mSceneMode, mCameraMode, mLiveView, mCustomBracketing, mTimelapse, mFlashCommander, mFocusBracketing;
	
	public MainMenuProvider(Context context) {
		super(context);
		mContext = context;
	}

	
	@Override
	public View onCreateActionView() {
		Log.d(TAG, "onCreateActionView");
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.main_menu_provider,null);
        
        mMenuLayout = (LinearLayout)view.findViewById(R.id.menu_layout);
        
        mShootingMode = (CheckableImageView)view.findViewById(R.id.shootingmode);
        mSceneMode = (CheckableImageView)view.findViewById(R.id.scenemode);
        mCameraMode = (CheckableImageView)view.findViewById(R.id.cameramode);
        mLiveView = (CheckableImageView)view.findViewById(R.id.liveview);
        mCustomBracketing = (CheckableImageView)view.findViewById(R.id.custom_bracketing);
        mFlashCommander = (CheckableImageView)view.findViewById(R.id.flash_commander);
        mTimelapse = (CheckableImageView)view.findViewById(R.id.timelapse);
        mFocusBracketing = (CheckableImageView)view.findViewById(R.id.focus_bracketing);
        
        mShootingMode.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(mContext, PtpProperty.ExposureProgramMode, "Select exposure program mode", null);
			}
		});
        mSceneMode.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(mContext, PtpProperty.SceneMode, "Select Scene mode", null);
			}
		});
        mCameraMode.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().getPtpDevice().changeCameraMode();
			}
		});
        mLiveView.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (mLiveView.isChecked())
					DslrHelper.getInstance().getPtpDevice().endLiveViewCmd(true);
				else
					DslrHelper.getInstance().getPtpDevice().startLiveViewCmd();
			}
		});
        mCustomBracketing.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				BracketingDialog dialog = new BracketingDialog(mContext);
				dialog.show();
			}
		});
        mCustomBracketing.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				PtpDevice ptpDevice = DslrHelper.getInstance().getPtpDevice();
				ptpDevice.setIsCustomBracketingEnabled(!ptpDevice.getIsCustomBracketingEnabled());
				mCustomBracketing.setChecked(ptpDevice.getIsCustomBracketingEnabled());
				return true;
			}
		});
        
        mTimelapse.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				TimelapseDialog dialog = new TimelapseDialog(mContext);
				dialog.show();
			}
		});
        
        mFlashCommander.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				FlashDialog dialog = new FlashDialog(mContext);
				dialog.show();
			}
		});
        mFocusBracketing.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				FocusStackingDialog dialog = new FocusStackingDialog(mContext);
				dialog.show();
			}
		});
        
        if (DslrHelper.getInstance().getIsInitialized() && DslrHelper.getInstance().getPtpDevice().getIsInitialized())
        	updateDisplay();
        return view;
    }

	public void initDisplay(){
		updateDisplay();
	}
	
	private void updateDisplay() {
		DslrHelper dslrHelper = DslrHelper.getInstance();
		if (dslrHelper != null && dslrHelper.getIsInitialized()) {
			if (dslrHelper.getPtpDevice().getIsCommandSupported(PtpCommand.ChangeCameraMode)) 
				mCameraMode.setVisibility(View.VISIBLE);
			else
				mCameraMode.setVisibility(View.GONE);
			PtpProperty property = dslrHelper.getPtpDevice().getPtpProperty(PtpProperty.ExposureProgramMode);
			if (property != null) {
				dslrHelper.setDslrImg(mShootingMode, property);
				mShootingMode.setEnabled(property.getIsWritable());
				if (mCameraMode.getVisibility() == View.VISIBLE) {
					mCameraMode.setChecked(property.getIsWritable());
					mCameraMode.setImageResource(mCameraMode.isChecked() ? R.drawable.hostmode : R.drawable.cameramode);
				}
			}
			else
				mShootingMode.setVisibility(View.GONE);
			
			property = dslrHelper.getPtpDevice().getPtpProperty(PtpProperty.SceneMode);
			if (property != null) {
				dslrHelper.setDslrImg(mSceneMode, property);
			}
			else 
				mSceneMode.setVisibility(View.GONE);
			property = dslrHelper.getPtpDevice().getPtpProperty(PtpProperty.LiveViewStatus);
			if (property != null) {
				boolean lvEnabled = (Integer)property.getValue() != 0;
				mLiveView.setVisibility(View.VISIBLE);
				mLiveView.setChecked(lvEnabled);
			}
			else
				mLiveView.setVisibility(View.GONE);
			mCustomBracketing.setChecked(dslrHelper.getPtpDevice().getIsCustomBracketingEnabled());
		}
	}
	
	public void ptpPropertyChanged(PtpProperty property) {
		DslrHelper dslrHelper = DslrHelper.getInstance();
		if (property != null && dslrHelper.getIsInitialized() && mCameraMode != null && mShootingMode != null && mLiveView != null) {
			switch(property.getPropertyCode()) {
				case PtpProperty.ExposureProgramMode:
					dslrHelper.setDslrImg(mShootingMode, property);
					mShootingMode.setEnabled(property.getIsWritable());
					if (mCameraMode.getVisibility() == View.VISIBLE) {
						mCameraMode.setChecked(property.getIsWritable());
						mCameraMode.setImageResource(mCameraMode.isChecked() ? R.drawable.hostmode : R.drawable.cameramode);
					}
					break;
				case PtpProperty.SceneMode:
					dslrHelper.setDslrImg(mSceneMode, property);
					break;
				case PtpProperty.LiveViewStatus:
					boolean lvEnabled = (Integer)property.getValue() != 0;
					mLiveView.setVisibility(View.VISIBLE);
					mLiveView.setChecked(lvEnabled);
					break;
			}
		}
	}
	
	public void ptpDeviceEvent(PtpDeviceEvent event, Object eventData) {
		switch(event) {
		case BusyBegin:
			Log.d(TAG, "BusyBegin");
			DslrHelper.getInstance().enableDisableControls(mMenuLayout, false);
			break;
		case BusyEnd:
			Log.d(TAG, "BusyEnd");
			DslrHelper.getInstance().enableDisableControls(mMenuLayout, true);
			break;
		}
		
	}
	
}
