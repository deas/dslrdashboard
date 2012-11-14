package com.dslr.dashboard;

public class LvSurfaceHelper {
	public PtpLiveViewObject mLvo;
	public boolean mHistogramEnabled = true;
	public boolean mHistogramSeparate = true;
	public PtpProperty mLvZoom;
//	public LayoutMain.TouchMode mTouchMode;
//	public int mMfDriveStep;
//	public int mFocusCurrent;
//	public int mFocusMax;
	public int mOsdDisplay;
	
	public LvSurfaceHelper(PtpLiveViewObject lvo, PtpProperty lvZoom, int osdDisplay){
		mLvo = lvo;
		mLvZoom = lvZoom;
		mOsdDisplay = osdDisplay;
	}
//	public LvSurfaceHelper(PtpLiveViewObject lvo, PtpProperty lvZoom, LayoutMain.TouchMode touchMode, int mfDriveStep, int focusCurrent, int focusMax, int osdDisplay) {
//		mLvo = lvo;
//		mLvZoom = lvZoom;
//		mTouchMode = touchMode;
//		mMfDriveStep = mfDriveStep;
//		mFocusCurrent = focusCurrent;
//		mFocusMax = focusMax;
//		mOsdDisplay = osdDisplay;
//	}
} 	
