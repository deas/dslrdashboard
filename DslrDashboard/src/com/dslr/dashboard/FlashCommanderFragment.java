package com.dslr.dashboard;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

public class FlashCommanderFragment extends DslrFragmentBase {

	private final static String TAG = FlashCommanderFragment.class.getSimpleName();
	
	private TextView txtInternalFlashRPTIntense, txtInternalFlashRPTCount, txtInternalFlashRPTInterval;
	private TextView txtInternalFlashCommanderChannel;
	private CheckableImageView btnInternalFlashCommanderSelf, btnInternalFlashCommanderGroupA, btnInternalFlashCommanderGroupB;
	private TextView txtInternalFlashCommanderSelfComp, txtInternalFlashCommanderSelfIntense;
	private TextView txtInternalFlashCommanderGroupAComp, txtInternalFlashCommanderGroupAIntense;
	private TextView txtInternalFlashCommanderGroupBComp, txtInternalFlashCommanderGroupBIntense;
	private GridLayout mGridLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mView = inflater.inflate(R.layout.flash_commander_fragment, container, false);
		
		getDialog().setTitle("Flash Commander");
		
		mGridLayout = (GridLayout)mView.findViewById(R.id.flash_grid_layout);
		
        // repeat flash properties 
        txtInternalFlashRPTIntense = (TextView)mView.findViewById(R.id.txtflashrptintense);
        txtInternalFlashRPTCount = (TextView)mView.findViewById(R.id.txtflashrptcout);
        txtInternalFlashRPTInterval = (TextView)mView.findViewById(R.id.txtflashrptinterval);

        // commander channel
        txtInternalFlashCommanderChannel = (TextView)mView.findViewById(R.id.txtflashcommanderchannel);
        
        // commander self
        btnInternalFlashCommanderSelf = (CheckableImageView)mView.findViewById(R.id.imginternalflashcommanderself);
        txtInternalFlashCommanderSelfComp = (TextView)mView.findViewById(R.id.txtflashcommanderselfcomp);
        txtInternalFlashCommanderSelfIntense = (TextView)mView.findViewById(R.id.txtflashcommanderselfintense);

        // commander group A
        btnInternalFlashCommanderGroupA = (CheckableImageView)mView.findViewById(R.id.imginternalflashcommandergroupa);
        txtInternalFlashCommanderGroupAComp = (TextView)mView.findViewById(R.id.txtflashcommandergroupacomp);
        txtInternalFlashCommanderGroupAIntense = (TextView)mView.findViewById(R.id.txtflashcommandergroupaintense);

        // commander group B
        btnInternalFlashCommanderGroupB = (CheckableImageView)mView.findViewById(R.id.imginternalflashcommandergroupb);
        txtInternalFlashCommanderGroupBComp = (TextView)mView.findViewById(R.id.txtflashcommandergroupbcomp);
        txtInternalFlashCommanderGroupBIntense = (TextView)mView.findViewById(R.id.txtflashcommandergroupbintense);


        // repeat flash properties
        txtInternalFlashRPTIntense.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.InternalFlashManualRPTIntense, "Internal Flash Repeat mode intensity", null);
			}
		});
        txtInternalFlashRPTCount.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.InternalFlashManualRPTCount, "Internal Flash Repeat mode count", null);
			}
		});
        txtInternalFlashRPTInterval.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.InternalFlashManualRPTInterval, "Internal Flash Repeat mode interval", null);
			}
		});
        // commander channel
        txtInternalFlashCommanderChannel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.InternalFlashCommanderChannel, "Internal Flash Commander channel", null);
			}
		});
        // commander self
        btnInternalFlashCommanderSelf.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.InternalFlashCommanderSelfMode, "Commander-Self mode", null);
			}
		});
        txtInternalFlashCommanderSelfComp.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.InternalFlashCommanderSelfComp, "Commander-Self compensation", null);
			}
		});
        txtInternalFlashCommanderSelfIntense.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.InternalFlashCommanderSelfIntense, "Commander-Self intensity", null);
			}
		});
        // commander group A
        btnInternalFlashCommanderGroupA.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.InternalFlashCommanderGroupAMode, "Commander-Group A mode", null);
			}
		});
        txtInternalFlashCommanderGroupAComp.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.InternalFlashCommanderGroupAComp, "Commander-Group A compensation", null);
			}
		});
        txtInternalFlashCommanderGroupAIntense.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.InternalFlashCommanderGroupAIntense, "Commander-Group A intensity", null);
			}
		});
        // commander group B
        btnInternalFlashCommanderGroupB.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.InternalFlashCommanderGroupBMode, "Commander-Group B mode", null);
			}
		});
        txtInternalFlashCommanderGroupBComp.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.InternalFlashCommanderGroupBComp, "Commander-Group B compensation", null);
			}
		});
        txtInternalFlashCommanderGroupBIntense.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DslrHelper.getInstance().createDslrDialog(getActivity(), PtpProperty.InternalFlashCommanderGroupBIntense, "Commander-Group B intensity", null);
				
			}
		});
        
		return mView;
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
		initializePtpPropertyView(txtInternalFlashRPTIntense, getPtpDevice().getPtpProperty(PtpProperty.InternalFlashManualRPTIntense));
		initializePtpPropertyView(txtInternalFlashRPTCount, getPtpDevice().getPtpProperty(PtpProperty.InternalFlashManualRPTCount));
		initializePtpPropertyView(txtInternalFlashRPTInterval, getPtpDevice().getPtpProperty(PtpProperty.InternalFlashManualRPTInterval));
		initializePtpPropertyView(txtInternalFlashCommanderChannel, getPtpDevice().getPtpProperty(PtpProperty.InternalFlashCommanderChannel));
		initializePtpPropertyView(btnInternalFlashCommanderSelf, getPtpDevice().getPtpProperty(PtpProperty.InternalFlashCommanderSelfMode));
		initializePtpPropertyView(txtInternalFlashCommanderSelfComp, getPtpDevice().getPtpProperty(PtpProperty.InternalFlashCommanderSelfComp));
		initializePtpPropertyView(txtInternalFlashCommanderSelfIntense, getPtpDevice().getPtpProperty(PtpProperty.InternalFlashCommanderSelfIntense));
		initializePtpPropertyView(btnInternalFlashCommanderGroupA, getPtpDevice().getPtpProperty(PtpProperty.InternalFlashCommanderGroupAMode));
		initializePtpPropertyView(txtInternalFlashCommanderGroupAComp, getPtpDevice().getPtpProperty(PtpProperty.InternalFlashCommanderGroupAComp));
		initializePtpPropertyView(txtInternalFlashCommanderGroupAIntense, getPtpDevice().getPtpProperty(PtpProperty.InternalFlashCommanderGroupAIntense));
		initializePtpPropertyView(btnInternalFlashCommanderGroupB, getPtpDevice().getPtpProperty(PtpProperty.InternalFlashCommanderGroupBMode));
		initializePtpPropertyView(txtInternalFlashCommanderGroupBComp, getPtpDevice().getPtpProperty(PtpProperty.InternalFlashCommanderGroupBComp));
		initializePtpPropertyView(txtInternalFlashCommanderGroupBIntense, getPtpDevice().getPtpProperty(PtpProperty.InternalFlashCommanderGroupBIntense));
	}

	@Override
	protected void internalPtpPropertyChanged(PtpProperty property) {
		if (property != null) {
			switch(property.getPropertyCode()) {
				case PtpProperty.InternalFlashManualRPTIntense:
					DslrHelper.getInstance().setDslrTxt(txtInternalFlashRPTIntense, property);
					break;
				case PtpProperty.InternalFlashManualRPTCount:
					DslrHelper.getInstance().setDslrTxt(txtInternalFlashRPTCount, property);
					break;
				case PtpProperty.InternalFlashManualRPTInterval:
					DslrHelper.getInstance().setDslrTxt(txtInternalFlashRPTInterval, property);
					break;
				case PtpProperty.InternalFlashCommanderChannel:
					DslrHelper.getInstance().setDslrTxt(txtInternalFlashCommanderChannel, property);
					break;
				case PtpProperty.InternalFlashCommanderSelfMode:
					DslrHelper.getInstance().setDslrImg(btnInternalFlashCommanderSelf, property);
					break;
				case PtpProperty.InternalFlashCommanderSelfComp:
					DslrHelper.getInstance().setDslrTxt(txtInternalFlashCommanderSelfComp, property);
					break;
				case PtpProperty.InternalFlashCommanderSelfIntense:
					DslrHelper.getInstance().setDslrTxt(txtInternalFlashCommanderSelfIntense, property);
					break;
				case PtpProperty.InternalFlashCommanderGroupAMode:
					DslrHelper.getInstance().setDslrImg(btnInternalFlashCommanderGroupA, property);
					break;
				case PtpProperty.InternalFlashCommanderGroupAComp:
					DslrHelper.getInstance().setDslrTxt(txtInternalFlashCommanderGroupAComp, property);
					break;
				case PtpProperty.InternalFlashCommanderGroupAIntense:
					DslrHelper.getInstance().setDslrTxt(txtInternalFlashCommanderGroupAIntense, property);
					break;
				case PtpProperty.InternalFlashCommanderGroupBMode:
					DslrHelper.getInstance().setDslrImg(btnInternalFlashCommanderGroupB, property);
					break;
				case PtpProperty.InternalFlashCommanderGroupBComp:
					DslrHelper.getInstance().setDslrTxt(txtInternalFlashCommanderGroupBComp, property);
					break;
				case PtpProperty.InternalFlashCommanderGroupBIntense:
					DslrHelper.getInstance().setDslrTxt(txtInternalFlashCommanderGroupBIntense, property);
					break;
			}
		}
	}

	@Override
	protected void ptpDeviceEvent(PtpDeviceEvent event, Object data) {
		switch(event) {
		case BusyBegin:
			Log.d(TAG, "Busy begin");
			DslrHelper.getInstance().enableDisableControls(mGridLayout, false);
			break;
		case BusyEnd:
			Log.d(TAG, "Busy end");
			DslrHelper.getInstance().enableDisableControls(mGridLayout, true);
			break;
		}
	}

	@Override
	protected void internalSharedPrefsChanged(SharedPreferences prefs,
			String key) {
		// TODO Auto-generated method stub
		
	}

}
