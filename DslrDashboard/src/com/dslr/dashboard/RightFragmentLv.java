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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RightFragmentLv extends DslrFragmentBase {

	private static final String TAG = "RightFragmentLv";
	
	private LinearLayout mRightLvLayout;
	private CheckableImageView mZoomIn, mZoomOut, mLvFocusMode, mMovieRecordWithVoice, mMovieRecordMicrophoneLevel, mMovieRecordSize;
	private TextView mVideoMode, mAfAtLiveView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_right_lv, container, false);
		
		mRightLvLayout = (LinearLayout)view.findViewById(R.id.right_lv_layout);
		
		mZoomIn = (CheckableImageView)view.findViewById(R.id.lvzoomin);
		mZoomOut = (CheckableImageView)view.findViewById(R.id.lvzoomout);
		mLvFocusMode = (CheckableImageView)view.findViewById(R.id.lvfocusmode);
		mAfAtLiveView = (TextView)view.findViewById(R.id.afatliveview);
		mVideoMode = (TextView)view.findViewById(R.id.videomode);
		mMovieRecordSize = (CheckableImageView)view.findViewById(R.id.movierecordsize);
		mMovieRecordWithVoice = (CheckableImageView)view.findViewById(R.id.movierecordwithvoice);
		mMovieRecordMicrophoneLevel = (CheckableImageView)view.findViewById(R.id.movierecordmicrophonelevel);
		
		mZoomIn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (getDslrActivity() != null)
					getDslrActivity().zoomLiveView(true);
			}
		});
		mZoomOut.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (getDslrActivity() != null)
					getDslrActivity().zoomLiveView(false);
			}
		});
		mLvFocusMode.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.AfAtLiveView, "Select LV Focus Mode", null);
			}
		});
		mVideoMode.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.VideoMode, "Select Video mode", null);
			}
		});
		mMovieRecordSize.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.MovieRecordScreenSize, getMovieRecordSizeOffset(), "Select Movie recording screen size", null);
			}
		});
		mMovieRecordWithVoice.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.MovieRecordWithVoice, "Select Movie voice recording mode", null);
			}
		});
		mMovieRecordMicrophoneLevel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.MovieRecordMicrophoneLevel, "Select Movie recording Microphone level", null);
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

	private int getMovieRecordSizeOffset() {
		Integer offset = 0;
		switch(getPtpDevice().getProductId()) {
			case 0x0428:
			case 0x0429:
				PtpProperty vMode = getPtpDevice().getPtpProperty(PtpProperty.VideoMode);
				if (vMode != null) {
					if ((Integer)vMode.getValue() == 1)
						offset = 16;
				}
			break;
		default:
			break;
		}
		return offset;
	}
	@Override
	protected void internalInitFragment() {
		if (getIsPtpDeviceInitialized()) {
			initializePtpPropertyView(mLvFocusMode, getPtpDevice().getPtpProperty(PtpProperty.AfAtLiveView));
			initializePtpPropertyView(mAfAtLiveView, getPtpDevice().getPtpProperty(PtpProperty.AfModeAtLiveView));
			initializePtpPropertyView(mVideoMode, getPtpDevice().getPtpProperty(PtpProperty.VideoMode));
			initializePtpPropertyView(mMovieRecordSize, getPtpDevice().getPtpProperty(PtpProperty.MovieRecordScreenSize));
			initializePtpPropertyView(mMovieRecordWithVoice, getPtpDevice().getPtpProperty(PtpProperty.MovieRecordWithVoice));
			initializePtpPropertyView(mMovieRecordMicrophoneLevel, getPtpDevice().getPtpProperty(PtpProperty.MovieRecordMicrophoneLevel));
		}
	}

	@Override
	protected void internalPtpPropertyChanged(PtpProperty property) {
		if (property != null) {
			switch (property.getPropertyCode()) {
			case PtpProperty.AfAtLiveView:
				DslrHelper.getInstance().setDslrImg(mLvFocusMode, property);
				break;
			case PtpProperty.AfModeAtLiveView:
				DslrHelper.getInstance().setDslrTxt(mAfAtLiveView, property);
				break;
			case PtpProperty.VideoMode:
				DslrHelper.getInstance().setDslrTxt(mVideoMode, property);
				break;
			case PtpProperty.MovieRecordScreenSize:
				DslrHelper.getInstance().setDslrImg(mMovieRecordSize,getMovieRecordSizeOffset(), property);
				break;
			case PtpProperty.MovieRecordWithVoice:
				DslrHelper.getInstance().setDslrImg(mMovieRecordWithVoice, property);
				break;
			case PtpProperty.MovieRecordMicrophoneLevel:
				DslrHelper.getInstance().setDslrImg(mMovieRecordMicrophoneLevel, property);
				break;
			}
		}
	}

	@Override
	protected void ptpDeviceEvent(PtpDeviceEvent event, Object data) {
		switch(event) {
		case BusyBegin:
			DslrHelper.getInstance().enableDisableControls(mRightLvLayout, false);
			break;
		case BusyEnd:
			DslrHelper.getInstance().enableDisableControls(mRightLvLayout, true);
			break;
		}
	}

	@Override
	protected void internalSharedPrefsChanged(SharedPreferences prefs,
			String key) {
		// TODO Auto-generated method stub
		
	}

}
