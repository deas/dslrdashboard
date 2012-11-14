package com.dslr.dashboard;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class FocusStackingDialog {

	private final static String TAG = FocusStackingDialog.class.getSimpleName();
	
	private Context mContext;
	private DslrHelper mDslrHelper;
	private PtpDevice mPtpDevice;
	private View mView;
	private CustomDialog mDialog;

	private EditText mEditImageNumber, mEditFocusStep;
	private RadioButton mFocusDown, mFocusUp;
	private Button mStartFocusStacking;
	private CheckBox mFocusFirst;
	
	private int mFocusImages = 5;
	private int mFocusStep = 10;
	
	private boolean mFocusDirectionDown = true;
	private boolean mFocusFocusFirst = false;
	
	public FocusStackingDialog(Context context) {
		
		mContext = context;
		mDslrHelper = DslrHelper.getInstance();
		mPtpDevice = mDslrHelper.getPtpDevice();
		
		if (mPtpDevice != null) {
			mFocusImages = mPtpDevice.getFocusImages();
			mFocusStep = mPtpDevice.getFocusStep();
			mFocusDirectionDown = mPtpDevice.getFocusDirectionDown();
			mFocusFocusFirst = mPtpDevice.getFocusFocusFirst();
		}
		
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mView = inflater.inflate(R.layout.focusstackingdialog, null);
        
        mEditImageNumber = (EditText)mView.findViewById(R.id.focus_stacking_Number);
        mEditFocusStep = (EditText)mView.findViewById(R.id.focus_stacking_Step);
        mFocusDown = (RadioButton)mView.findViewById(R.id.focus_stacking_RadioDown);
        mFocusUp = (RadioButton)mView.findViewById(R.id.focus_stacking_RadioUp);
        mStartFocusStacking = (Button)mView.findViewById(R.id.focus_stacking_start);
        mFocusFirst = (CheckBox)mView.findViewById(R.id.focus_stacking_focus_first);

        mStartFocusStacking.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mFocusImages = Integer.valueOf(mEditImageNumber.getText().toString());
				mFocusStep = Integer.valueOf(mEditFocusStep.getText().toString());
				mFocusDirectionDown = mFocusDown.isChecked();
				Log.d(TAG, "Focus direction down: " + mFocusDirectionDown);
				Log.d(TAG, "Focus images: " + mFocusImages);
				Log.d(TAG, "Focus step: " + mFocusStep);
				
				mDialog.dismiss();
				mPtpDevice.startFocusStacking(mFocusImages, mFocusStep, mFocusDirectionDown, mFocusFocusFirst);
				
			}
		});
//        mFocusUp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//			
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				Log.d(TAG, "Focus direction down: " + isChecked);
//				mFocusDirectionDown = isChecked;
//			}
//		});
//        mEditImageNumber.addTextChangedListener(new TextWatcher() {
//			
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			public void beforeTextChanged(CharSequence s, int start, int count,
//					int after) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			public void afterTextChanged(Editable s) {
//				mFocusImages = Integer.valueOf(s.toString());
//				Log.d(TAG, "Focus images changed: " + mFocusImages);
//			}
//		});
//        mEditFocusStep.addTextChangedListener(new TextWatcher() {
//			
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			public void beforeTextChanged(CharSequence s, int start, int count,
//					int after) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			public void afterTextChanged(Editable s) {
//				mFocusStep = Integer.valueOf(s.toString());
//				Log.d(TAG, "Focus step changed: " + mFocusStep);
//			}
//		});
	}	

	private void initDialog(){
		mEditImageNumber.setText(Integer.toString(mFocusImages));
		mEditFocusStep.setText(Integer.toString(mFocusStep));
		mFocusDown.setChecked(mFocusDirectionDown);
		mFocusFirst.setChecked(mFocusFocusFirst);
	}
	public void show() {
		initDialog();
        CustomDialog.Builder customBuilder = new CustomDialog.Builder(mContext);
        customBuilder.setTitle("Focus Stacking settings")
        	.setContentView(mView);
        mDialog = customBuilder
        	.create();
        mDialog.show();
	}
	
	
}
