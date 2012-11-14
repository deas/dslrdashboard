package com.dslr.dashboard;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RightFragment extends DslrFragmentBase {

	private static final String TAG = "RightFragment";
	
	private LinearLayout mRightLayout;
	
	private CheckableImageView mStillCaptureMode, mCompressionSetting, mImageSize, mWhiteBalance, 
			mExposureMetering, mFocusMetering, mAfModeSelect, mActivePicCtrlItem, mActiveDLightning, mFlashMode,
			mEnableBracketing, mBracketingType;
	private TextView mIso, mBurstNumber, mExposureCompensation, mExposureEvStep, mFlashCompensation,
		    mAeBracketingStep, mWbBracketingStep, mAeBracketingCount;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_right, container, false);
	
		mRightLayout = (LinearLayout)view.findViewById(R.id.right_layout);
		
		mStillCaptureMode = (CheckableImageView)view.findViewById(R.id.stillcapturemode);
		mCompressionSetting = (CheckableImageView)view.findViewById(R.id.compressionsetting);
		mImageSize = (CheckableImageView)view.findViewById(R.id.imagesize);
		mIso = (TextView)view.findViewById(R.id.iso);
		mWhiteBalance = (CheckableImageView)view.findViewById(R.id.whitebalance);
		mExposureMetering = (CheckableImageView)view.findViewById(R.id.exposuremeteringmode);
		mFocusMetering = (CheckableImageView)view.findViewById(R.id.focusmeteringmode);
		mAfModeSelect = (CheckableImageView)view.findViewById(R.id.afmodeselect);
		mBurstNumber = (TextView)view.findViewById(R.id.burstnumber);
		mExposureCompensation = (TextView)view.findViewById(R.id.exposurecompensation);
		mExposureEvStep = (TextView)view.findViewById(R.id.exposureevstep);
		mActivePicCtrlItem = (CheckableImageView)view.findViewById(R.id.activepicctrlitem);
		mActiveDLightning = (CheckableImageView)view.findViewById(R.id.activedlightning);
		mFlashMode = (CheckableImageView)view.findViewById(R.id.flashmode);
		mFlashCompensation = (TextView)view.findViewById(R.id.flashcompensation);
		mEnableBracketing = (CheckableImageView)view.findViewById(R.id.enablebracketing);
		mBracketingType = (CheckableImageView)view.findViewById(R.id.bracketingtype);
		mAeBracketingStep = (TextView)view.findViewById(R.id.aebracketingstep);
		mWbBracketingStep = (TextView)view.findViewById(R.id.wbbracketingstep);
		mAeBracketingCount = (TextView)view.findViewById(R.id.aebracketingcount);
		
		
		mStillCaptureMode.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.StillCaptureMode, "Select still capture mode", null);
			}
		});
		mCompressionSetting.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.CompressionSetting, "Select compression mode", null);
			}
		});
		mImageSize.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.ImageSize, "Select image size", null);
			}
		});
		mIso.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.ExposureIndex, "Select ISO value", null);
			}
		});
		mWhiteBalance.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.WhiteBalance, "Select White Balance value", null);
			}
		});
		mExposureMetering.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.ExposureMeteringMode, "Select Exposure metering mode", null);
			}
		});
		mFocusMetering.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.FocusMeteringMode, "Select Focus metering mode", null);
			}
		});
		mAfModeSelect.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.AfModeSelect, "Select AF mode", null);
			}
		});
		mExposureEvStep.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.ExposureEvStep, "Select EV step", null);
			}
		});
		mExposureCompensation.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().showExposureCompensationDialog(getActivity());
			}
		});
		mActivePicCtrlItem.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.ActivePicCtrlItem, "Select Active picture control", null);
			}
		});
		mActiveDLightning.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.ActiveDLighting, "Select Active D-Lighting", null);
			}
		});
		mBurstNumber.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				PtpProperty property = DslrHelper.getInstance().getPtpDevice().getPtpProperty(PtpProperty.BurstNumber);
				if (property != null){
			        CustomDialog.Builder customBuilder = new CustomDialog.Builder(getActivity());
			        
			        InputFilter[] filterArray = new InputFilter[1];
			        filterArray[0] = new InputFilter.LengthFilter(2);
	
			        final EditText txt = new EditText(getActivity());
			        txt.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			        txt.setText(property.getValue().toString());
			        txt.setInputType(InputType.TYPE_CLASS_NUMBER);
			        txt.setFilters(filterArray);
			        customBuilder.setTitle("Enter burst number")
			        	.setContentView(txt)
			        	.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								String strNum = txt.getText().toString().trim();
								if (!strNum.isEmpty()) {
									int brNum = Integer.parseInt(strNum);
									Toast.makeText(getActivity(), "Burst number: " + strNum + " num: " + brNum, Toast.LENGTH_SHORT).show();
									DslrHelper.getInstance().getPtpDevice().setDevicePropValueCmd(PtpProperty.BurstNumber, brNum);
								}
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
			        CustomDialog dialog = customBuilder.create();
			        dialog.show();
				} 			}
		});
		mFlashMode.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.FlashMode, "Select Flash mode", null);
			}
		});
		mFlashCompensation.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().showInternalFlashCompensationDialog(getActivity());
			}
		});
		mEnableBracketing.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				getPtpDevice().toggleInternalBracketing();
				
//				PtpProperty property = getPtpDevice().getPtpProperty(PtpProperty.EnableBracketing);
//				if (property != null) {
//					Integer val = (Integer)property.getValue();
//					getPtpDevice().setDevicePropValueCmd(PtpProperty.EnableBracketing, val == 0 ? 1 : 0);
//				}
			}
		});
		mBracketingType.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.BracketingType, "Select Bracketing type", null);
			}
		});
		mAeBracketingStep.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.AeBracketingStep, "Select AE-Bracketing step", null);
			}
		});
		mWbBracketingStep.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.WbBracketingStep, "Select WB-Bracketing step", null);
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
			initializePtpPropertyView(mStillCaptureMode, getPtpDevice().getPtpProperty(PtpProperty.StillCaptureMode));
			initializePtpPropertyView(mCompressionSetting, getPtpDevice().getPtpProperty(PtpProperty.CompressionSetting));
			initializePtpPropertyView(mImageSize, getPtpDevice().getPtpProperty(PtpProperty.ImageSize));
			initializePtpPropertyView(mIso, getPtpDevice().getPtpProperty(PtpProperty.ExposureIndex));
			initializePtpPropertyView(mWhiteBalance, getPtpDevice().getPtpProperty(PtpProperty.WhiteBalance));
			initializePtpPropertyView(mExposureMetering, getPtpDevice().getPtpProperty(PtpProperty.ExposureMeteringMode));
			initializePtpPropertyView(mFocusMetering, getPtpDevice().getPtpProperty(PtpProperty.FocusMeteringMode));
			initializePtpPropertyView(mAfModeSelect, getPtpDevice().getPtpProperty(PtpProperty.AfModeSelect));
			initializePtpPropertyView(mBurstNumber, getPtpDevice().getPtpProperty(PtpProperty.BurstNumber));
			initializePtpPropertyView(mExposureEvStep, getPtpDevice().getPtpProperty(PtpProperty.ExposureEvStep));
			initializePtpPropertyView(mExposureCompensation, getPtpDevice().getPtpProperty(PtpProperty.ExposureBiasCompensation));
			initializePtpPropertyView(mActivePicCtrlItem, getPtpDevice().getPtpProperty(PtpProperty.ActivePicCtrlItem));
			initializePtpPropertyView(mActiveDLightning, getPtpDevice().getPtpProperty(PtpProperty.ActiveDLighting));
			initializePtpPropertyView(mFlashMode, getPtpDevice().getPtpProperty(PtpProperty.FlashMode));
			initializePtpPropertyView(mFlashCompensation, getPtpDevice().getPtpProperty(PtpProperty.InternalFlashCompensation));
			initializePtpPropertyView(mEnableBracketing, getPtpDevice().getPtpProperty(PtpProperty.EnableBracketing));
			initializePtpPropertyView(mBracketingType, getPtpDevice().getPtpProperty(PtpProperty.BracketingType));
			// hide these, if they supported will be displayed
			mAeBracketingStep.setVisibility(View.GONE);
			mAeBracketingCount.setVisibility(View.GONE);
			mWbBracketingStep.setVisibility(View.GONE);
			internalPtpPropertyChanged(getPtpDevice().getPtpProperty(PtpProperty.AeBracketingStep));
			internalPtpPropertyChanged(getPtpDevice().getPtpProperty(PtpProperty.WbBracketingStep));
			internalPtpPropertyChanged(getPtpDevice().getPtpProperty(PtpProperty.AeBracketingCount));
		}
	}

	@Override
	protected void internalPtpPropertyChanged(PtpProperty property) {
		if (property != null) {
			switch (property.getPropertyCode()) {
			case PtpProperty.StillCaptureMode:
				DslrHelper.getInstance().setDslrImg(mStillCaptureMode, property);
				break;
			case PtpProperty.CompressionSetting:
				DslrHelper.getInstance().setDslrImg(mCompressionSetting, property);
				break;
			case PtpProperty.ImageSize:
				DslrHelper.getInstance().setDslrImg(mImageSize, property);
				break;
			case PtpProperty.ExposureIndex:
				DslrHelper.getInstance().setDslrTxt(mIso, property);
				break;
			case PtpProperty.WhiteBalance:
				DslrHelper.getInstance().setDslrImg(mWhiteBalance, property);
				break;
			case PtpProperty.ExposureMeteringMode:
				DslrHelper.getInstance().setDslrImg(mExposureMetering, property);
				break;
			case PtpProperty.FocusMeteringMode:
				DslrHelper.getInstance().setDslrImg(mFocusMetering, property);
				break;
			case PtpProperty.AfModeSelect:
				DslrHelper.getInstance().setDslrImg(mAfModeSelect, property);
				break;
			case PtpProperty.BurstNumber:
				mBurstNumber.setText(property.getValue().toString());
				mBurstNumber.setEnabled(property.getIsWritable()); 
			case PtpProperty.ExposureEvStep:
				DslrHelper.getInstance().setDslrTxt(mExposureEvStep, property);
				break;
			case PtpProperty.ExposureBiasCompensation:
				mExposureCompensation.setEnabled(property.getIsWritable());
				int ev = (Integer)property.getValue();
				mExposureCompensation.setText(String.format("%+.1f EV", (double)ev/1000)); 
				break;
			case PtpProperty.ActivePicCtrlItem:
				DslrHelper.getInstance().setDslrImg(mActivePicCtrlItem, property);
				break;
			case PtpProperty.ActiveDLighting:
				DslrHelper.getInstance().setDslrImg(mActiveDLightning, property);
				break;
			case PtpProperty.FlashMode:
				DslrHelper.getInstance().setDslrImg(mFlashMode, property);
				break;
			case PtpProperty.InternalFlashCompensation:
				mFlashCompensation.setEnabled(property.getIsWritable());
				int fev = (Integer)property.getValue();
				mFlashCompensation.setText(String.format("%+.1f EV", (double)fev/6)); 				
				break;
			case PtpProperty.EnableBracketing:
				Integer enableBkt = (Integer)property.getValue();
				mEnableBracketing.setChecked(enableBkt == 1);
				break;
			case PtpProperty.BracketingType:
				
				Integer val = (Integer)property.getValue();
				boolean isAe = val == 1;
				boolean isWB = val == 3;
				boolean isADL = val == 4;
				
				mAeBracketingStep.setVisibility(isAe ? View.VISIBLE : View.GONE);
				mAeBracketingCount.setVisibility(isAe || isADL ? View.VISIBLE : View.GONE);
				mWbBracketingStep.setVisibility(isWB ? View.VISIBLE : View.GONE);
				
				DslrHelper.getInstance().setDslrImg(mBracketingType, property);
				break;
			case PtpProperty.AeBracketingStep:
				DslrHelper.getInstance().setDslrTxt(mAeBracketingStep, property);
				break;
			case PtpProperty.WbBracketingStep:
				DslrHelper.getInstance().setDslrTxt(mWbBracketingStep, property);
				break;
			case PtpProperty.AeBracketingCount:
				mAeBracketingCount.setText(property.getValue().toString());
				break;
			default:
				break;
			}
		}
	}

	@Override
	protected void ptpDeviceEvent(PtpDeviceEvent event, Object data) {
		switch(event) {
		case BusyBegin:
			Log.d(TAG, "Busy begin");
			DslrHelper.getInstance().enableDisableControls(mRightLayout, false);
			break;
		case BusyEnd:
			Log.d(TAG, "Busy end");
			DslrHelper.getInstance().enableDisableControls(mRightLayout, true);
			break;
		}
	}

	@Override
	protected void internalSharedPrefsChanged(SharedPreferences prefs,
			String key) {
		// TODO Auto-generated method stub
		
	}

}
