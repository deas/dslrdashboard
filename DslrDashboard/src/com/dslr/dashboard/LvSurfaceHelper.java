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
