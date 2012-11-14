package com.dslr.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class FlashDialog {

	private Context mContext;
	private DslrHelper mDslrHelper;
	private PtpDevice mPtpDevice;
	
	private TextView txtInternalFlashRPTIntense, txtInternalFlashRPTCount, txtInternalFlashRPTInterval;
	private TextView txtInternalFlashCommanderChannel;
	private CheckableImageView btnInternalFlashCommanderSelf, btnInternalFlashCommanderGroupA, btnInternalFlashCommanderGroupB;
	private TextView txtInternalFlashCommanderSelfComp, txtInternalFlashCommanderSelfIntense;
	private TextView txtInternalFlashCommanderGroupAComp, txtInternalFlashCommanderGroupAIntense;
	private TextView txtInternalFlashCommanderGroupBComp, txtInternalFlashCommanderGroupBIntense;
	
	private View mView;
	
	public FlashDialog(Context context) {
		mContext = context;
		mDslrHelper = DslrHelper.getInstance();
		mPtpDevice = mDslrHelper.getPtpDevice();
		
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mView = inflater.inflate(R.layout.flash_dialog, null);
        
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
				mDslrHelper.createDslrDialog(mContext, PtpProperty.InternalFlashManualRPTIntense, "Internal Flash Repeat mode intensity", null);
			}
		});
        txtInternalFlashRPTCount.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mDslrHelper.createDslrDialog(mContext, PtpProperty.InternalFlashManualRPTCount, "Internal Flash Repeat mode count", null);
			}
		});
        txtInternalFlashRPTInterval.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mDslrHelper.createDslrDialog(mContext, PtpProperty.InternalFlashManualRPTInterval, "Internal Flash Repeat mode interval", null);
			}
		});
        // commander channel
        txtInternalFlashCommanderChannel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mDslrHelper.createDslrDialog(mContext, PtpProperty.InternalFlashCommanderChannel, "Internal Flash Commander channel", null);
			}
		});
        // commander self
        btnInternalFlashCommanderSelf.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mDslrHelper.createDslrDialog(mContext, PtpProperty.InternalFlashCommanderSelfMode, "Commander-Self mode", null);
			}
		});
        txtInternalFlashCommanderSelfComp.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mDslrHelper.createDslrDialog(mContext, PtpProperty.InternalFlashCommanderSelfComp, "Commander-Self compensation", null);
			}
		});
        txtInternalFlashCommanderSelfIntense.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mDslrHelper.createDslrDialog(mContext, PtpProperty.InternalFlashCommanderSelfIntense, "Commander-Self intensity", null);
			}
		});
        // commander group A
        btnInternalFlashCommanderGroupA.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mDslrHelper.createDslrDialog(mContext, PtpProperty.InternalFlashCommanderGroupAMode, "Commander-Group A mode", null);
			}
		});
        txtInternalFlashCommanderGroupAComp.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mDslrHelper.createDslrDialog(mContext, PtpProperty.InternalFlashCommanderGroupAComp, "Commander-Group A compensation", null);
			}
		});
        txtInternalFlashCommanderGroupAIntense.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mDslrHelper.createDslrDialog(mContext, PtpProperty.InternalFlashCommanderGroupAIntense, "Commander-Group A intensity", null);
			}
		});
        // commander group B
        btnInternalFlashCommanderGroupB.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mDslrHelper.createDslrDialog(mContext, PtpProperty.InternalFlashCommanderGroupBMode, "Commander-Group B mode", null);
			}
		});
        txtInternalFlashCommanderGroupBComp.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mDslrHelper.createDslrDialog(mContext, PtpProperty.InternalFlashCommanderGroupBComp, "Commander-Group B compensation", null);
			}
		});
        txtInternalFlashCommanderGroupBIntense.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mDslrHelper.createDslrDialog(mContext, PtpProperty.InternalFlashCommanderGroupBIntense, "Commander-Group B intensity", null);
				
			}
		});

        
	}

	public void show() {
		if (mPtpDevice.getIsPtpDeviceInitialized()) {
			// update the properties display
			updatePtpProperty(mPtpDevice.getPtpProperty(PtpProperty.InternalFlashManualRPTIntense));
			updatePtpProperty(mPtpDevice.getPtpProperty(PtpProperty.InternalFlashManualRPTCount));
			updatePtpProperty(mPtpDevice.getPtpProperty(PtpProperty.InternalFlashManualRPTInterval));
			updatePtpProperty(mPtpDevice.getPtpProperty(PtpProperty.InternalFlashCommanderChannel));
			updatePtpProperty(mPtpDevice.getPtpProperty(PtpProperty.InternalFlashCommanderSelfMode));
			updatePtpProperty(mPtpDevice.getPtpProperty(PtpProperty.InternalFlashCommanderSelfComp));
			updatePtpProperty(mPtpDevice.getPtpProperty(PtpProperty.InternalFlashCommanderSelfIntense));
			updatePtpProperty(mPtpDevice.getPtpProperty(PtpProperty.InternalFlashCommanderGroupAMode));
			updatePtpProperty(mPtpDevice.getPtpProperty(PtpProperty.InternalFlashCommanderGroupAComp));
			updatePtpProperty(mPtpDevice.getPtpProperty(PtpProperty.InternalFlashCommanderGroupAIntense));
			updatePtpProperty(mPtpDevice.getPtpProperty(PtpProperty.InternalFlashCommanderGroupBMode));
			updatePtpProperty(mPtpDevice.getPtpProperty(PtpProperty.InternalFlashCommanderGroupBComp));
			updatePtpProperty(mPtpDevice.getPtpProperty(PtpProperty.InternalFlashCommanderGroupBIntense));
			
	        CustomDialog.Builder customBuilder = new CustomDialog.Builder(mContext);
	        customBuilder.setTitle("Custom Bracketing settings")
	        	.setContentView(mView);
	        customBuilder
	        	.create()
	        	.show();
			
		}
	}
	
	private void updatePtpProperty(PtpProperty ptpProperty) {
		if (ptpProperty != null) {
			switch (ptpProperty.getPropertyCode()) {
			case PtpProperty.InternalFlashManualRPTIntense:
				txtInternalFlashRPTIntense.setVisibility(View.VISIBLE);
				mDslrHelper.setDslrTxt(txtInternalFlashRPTIntense, ptpProperty);
				break;
			case PtpProperty.InternalFlashManualRPTCount:
				txtInternalFlashRPTCount.setVisibility(View.VISIBLE);
				mDslrHelper.setDslrTxt(txtInternalFlashRPTCount, ptpProperty);
				break;
			case PtpProperty.InternalFlashManualRPTInterval:
				txtInternalFlashRPTInterval.setVisibility(View.VISIBLE);
				mDslrHelper.setDslrTxt(txtInternalFlashRPTInterval, ptpProperty);
				break;
			case PtpProperty.InternalFlashCommanderChannel:
				txtInternalFlashCommanderChannel.setVisibility(View.VISIBLE);
				mDslrHelper.setDslrTxt(txtInternalFlashCommanderChannel, ptpProperty);
				break;
			case PtpProperty.InternalFlashCommanderSelfMode:
				btnInternalFlashCommanderSelf.setVisibility(View.VISIBLE);
				mDslrHelper.setDslrImg(btnInternalFlashCommanderSelf, ptpProperty);
				break;
			case PtpProperty.InternalFlashCommanderSelfComp:
				txtInternalFlashCommanderSelfComp.setVisibility(View.VISIBLE);
				mDslrHelper.setDslrTxt(txtInternalFlashCommanderSelfComp, ptpProperty);
				break;
			case PtpProperty.InternalFlashCommanderSelfIntense:
				txtInternalFlashCommanderSelfIntense.setVisibility(View.VISIBLE);
				mDslrHelper.setDslrTxt(txtInternalFlashCommanderSelfIntense, ptpProperty);
				break;
			case PtpProperty.InternalFlashCommanderGroupAMode:
				btnInternalFlashCommanderGroupA.setVisibility(View.VISIBLE);
				mDslrHelper.setDslrImg(btnInternalFlashCommanderGroupA, ptpProperty);
				break;
			case PtpProperty.InternalFlashCommanderGroupAComp:
				txtInternalFlashCommanderGroupAComp.setVisibility(View.VISIBLE);
				mDslrHelper.setDslrTxt(txtInternalFlashCommanderGroupAComp, ptpProperty);
				break;
			case PtpProperty.InternalFlashCommanderGroupAIntense:
				txtInternalFlashCommanderGroupAIntense.setVisibility(View.VISIBLE);
				mDslrHelper.setDslrTxt(txtInternalFlashCommanderGroupAIntense, ptpProperty);
				break;
			case PtpProperty.InternalFlashCommanderGroupBMode:
				btnInternalFlashCommanderGroupB.setVisibility(View.VISIBLE);
				mDslrHelper.setDslrImg(btnInternalFlashCommanderGroupB, ptpProperty);
				break;
			case PtpProperty.InternalFlashCommanderGroupBComp:
				txtInternalFlashCommanderGroupBComp.setVisibility(View.VISIBLE);
				mDslrHelper.setDslrTxt(txtInternalFlashCommanderGroupBComp, ptpProperty);
				break;
			case PtpProperty.InternalFlashCommanderGroupBIntense:
				txtInternalFlashCommanderGroupBIntense.setVisibility(View.VISIBLE);
				mDslrHelper.setDslrTxt(txtInternalFlashCommanderGroupBIntense, ptpProperty);
				break;
			}
		}
	}
 	
}
