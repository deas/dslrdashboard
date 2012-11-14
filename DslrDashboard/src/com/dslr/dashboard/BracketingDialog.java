package com.dslr.dashboard;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.TextView;

public class BracketingDialog{

	private static String TAG = "BracketingDialog";
	
	private Context mContext;
	private DslrHelper mDslrHelper;
	private PtpDevice mPtpDevice;
	private View mView;

	private TextView txtCustomBktStep, txtCustomBktDirection, txtCustomBktCount;
	private CheckBox mBktFocusFirst;
	
	public BracketingDialog(Context context) {
		
		mContext = context;
		mDslrHelper = DslrHelper.getInstance();
		mPtpDevice = mDslrHelper.getPtpDevice();
		
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mView = inflater.inflate(R.layout.bracketingdialog, null);
        
//        addContentView(layout, new LayoutParams(
//                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        
        
        //((TextView) layout.findViewById(R.id.title)).setText("Custom Bracketing settings");
        
		
		txtCustomBktStep = (TextView)mView.findViewById(R.id.txt_custombktstep);
		txtCustomBktDirection = (TextView)mView.findViewById(R.id.txt_custombktdirection);
		txtCustomBktCount = (TextView)mView.findViewById(R.id.txt_custombktcount);
		mBktFocusFirst = (CheckBox)mView.findViewById(R.id.bkt_focus_first);
		
		setCustomValues();
		
		txtCustomBktStep.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				customBktStepDialog();
			}
		});
		txtCustomBktDirection.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				customBktDirektionDialog();
			}
		});
		txtCustomBktCount.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				customBktCountDialog();
			}
		});
		mBktFocusFirst.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mPtpDevice.setCustomBracketingFocusFirst(mBktFocusFirst.isChecked());
			}
		});
		
	}	

	public void show() {
        CustomDialog.Builder customBuilder = new CustomDialog.Builder(mContext);
        customBuilder.setTitle("Custom Bracketing settings")
        	.setContentView(mView);
        customBuilder
        	.create()
        	.show();
	}
	
	private void setCustomValues(){
		if (mPtpDevice != null && mPtpDevice.getIsPtpDeviceInitialized()){
			txtCustomBktDirection.setText(getCustomBktDirection(mPtpDevice.getCustomBracketingDirection()));
			txtCustomBktCount.setText((String.format("%d", mPtpDevice.getCustomBracketingCount())));
			
			PtpProperty property = mPtpDevice.getPtpProperty(PtpProperty.ExposureEvStep);
			if (property != null) {
				if ((Integer)property.getValue() == 0)
					txtCustomBktStep.setText(String.format("%.1f EV", mPtpDevice.getCustomBracketingStep() * (1f / 3f)));
				else
					txtCustomBktStep.setText(String.format("%.1f EV", mPtpDevice.getCustomBracketingStep() * (1f / 2f)));
			}
			mBktFocusFirst.setChecked(mPtpDevice.getCustomBracketingFocusFirst());
			
			//txtCustomBktStep.setText((String.format("%d", _dslrHelper.getPtpService().getBktStep())));
		}
	}
	
    private String getCustomBktDirection(int direktion){
    	switch(direktion){
    	case 0:
    		return "Negative";
    	case 1:
    		return "Positive";
    	case 2:
    		return "Both";
    		default:
    			return "Both";
    	}
    }
    private void customBktCountDialog(){
    	final ArrayList<PtpPropertyListItem> items = new ArrayList<PtpPropertyListItem>();
    	int selectedItem = -1;
    	for(int i = 0; i <= 3; i++){
    		int current = 3 + (i * 2);
    		if (current == mPtpDevice.getCustomBracketingCount())
    			selectedItem = i;
    		PtpPropertyListItem item = new PtpPropertyListItem();
    		item.setImage(-1);
    		item.setValue(current);
    		item.setTitle(String.format("%d images", current));
    		items.add(item);
    		
    	}
    	
        CustomDialog.Builder customBuilder = new CustomDialog.Builder(mContext);
        customBuilder.setTitle("Custom bracketing count")
            .setListItems(items, selectedItem)
            .setListOnClickListener(new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					if (which >= 0 && which < items.size()){
						mPtpDevice.setCustomBracketingCount((Integer) items.get(which).getValue());
						txtCustomBktCount.setText(items.get(which).getTitle());
					}
				}
			});
    CustomDialog dialog = customBuilder.create();
    dialog.show();
    }
    
    private void customBktDirektionDialog(){
    	final ArrayList<PtpPropertyListItem> items = new ArrayList<PtpPropertyListItem>();
    	for(int i = 0; i <= 2; i++){
    		PtpPropertyListItem item = new PtpPropertyListItem();
    		item.setImage(-1);
    		item.setValue(i);
    		item.setTitle(getCustomBktDirection(i));
    		items.add(item);
    	}
        CustomDialog.Builder customBuilder = new CustomDialog.Builder(mContext);
        customBuilder.setTitle("Custom bracketing direktion")
            .setListItems(items, mPtpDevice.getCustomBracketingDirection())
            .setListOnClickListener(new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					if (which >= 0 && which < items.size()){
						mPtpDevice.setCustomBracketingDirection((Integer) items.get(which).getValue()); 
						txtCustomBktDirection.setText(items.get(which).getTitle());
					}
				}
			});
    CustomDialog dialog = customBuilder.create();
    dialog.show();
    }
    private void customBktStepDialog(){
    	PtpProperty prop = mPtpDevice.getPtpProperty(PtpProperty.ExposureEvStep);
    	if (prop != null){
    		int evStep = (Integer) prop.getValue();
    		final ArrayList<PtpPropertyListItem> items = new ArrayList<PtpPropertyListItem>();
    		switch(evStep)
    		{
    		case 0: // 1/3
    			for(int i = 1; i <= 6; i++){
    				PtpPropertyListItem item = new PtpPropertyListItem();
    				item.setImage(-1);
    				item.setTitle(String.format("%.1f EV", i * (1f / 3f)));
    				item.setValue(i);
    				items.add(item);
    			}
    			break;
    		case 1: // 1/2
    			if (mPtpDevice.getCustomBracketingStep() > 4)
    				 mPtpDevice.setCustomBracketingStep(1);
    			for(int i = 1; i <= 4; i++){
    				PtpPropertyListItem item = new PtpPropertyListItem();
    				item.setImage(-1);
    				item.setTitle(String.format("%.1f EV", i * (1f / 2f)));
    				item.setValue(i);
    				items.add(item);
    			}
    			break;
    		}
    		
            CustomDialog.Builder customBuilder = new CustomDialog.Builder(mContext);
            customBuilder.setTitle("Custom bracketing EV step")
                .setListItems(items, mPtpDevice.getCustomBracketingStep() - 1)
                .setListOnClickListener(new DialogInterface.OnClickListener() {
    				
    				public void onClick(DialogInterface dialog, int which) {
    					if (which >= 0 && which < items.size()){
    						mPtpDevice.setCustomBracketingStep((Integer) items.get(which).getValue());
    						txtCustomBktStep.setText(items.get(which).getTitle());
    					}
    				}
    			});
        CustomDialog dialog = customBuilder.create();
        dialog.show();
    		
    	}
    }
	
}
