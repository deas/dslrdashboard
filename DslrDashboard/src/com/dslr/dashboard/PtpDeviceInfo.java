// Copyright 2000 by David Brownell <dbrownell@users.sourceforge.net>
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package com.dslr.dashboard;

public class PtpDeviceInfo {
    private int		mStandardVersion;
    private int		mVendorExtensionId;
    private int		mVendorExtensionVersion;
    private String	mVendorExtensionDesc;

    private int		mFunctionalMode; 		// may change; 
    private int		mOperationsSupported [];		// 10.2
    private int		mEventsSupported [];		// 12.5
    private int		mPropertiesSupported [];		// 13.3.5

    private int		mCaptureFormats [];		// 6
    private int		mImageFormats [];		// 6
    private String	mManufacturer;
    private String	mModel;

    private String	mDeviceVersion;
    private String	mSerialNumber;
    
    private boolean mIsInitialized = false;

    public PtpDeviceInfo ()	{ 
    	mIsInitialized = false;
	}

    public PtpDeviceInfo(PtpBuffer data) {
    	parse(data);
    }
    
    public int getStandardVersion() {
    	return mStandardVersion;
    }
    public int getVendorExtensionId() {
    	return mVendorExtensionId;
    }
    public int getVendorExtensionVersion() {
    	return mVendorExtensionVersion;
    }
    public String getVendorExtensionDesc() {
    	return mVendorExtensionDesc;
    }
    public int getFunctionalMode() {
    	return mFunctionalMode;
    }
    public int[] getOperationsSupported() {
    	return mOperationsSupported;
    }
    public int[] getEventsSupported() {
    	return mEventsSupported;
    }
    public int[] getPropertiesSupported() {
    	return mPropertiesSupported;
    }
    public int[] getCaptureFormats() {
    	return mCaptureFormats;
    }
    public int[] getImageFormats() {
    	return mImageFormats;
    }
    public String getManufacturer(){
    	return mManufacturer;
    }
    public String getModel() {
    	return mModel;
    }
    public String getDeviceVersion(){
    	return mDeviceVersion;
    }
    public String getSerialNumber(){
    	return mSerialNumber;
    }
    public boolean getIsInitialized() {
    	return mIsInitialized;
    }
    
    private boolean supports (int supported [], int code) {
    	for (int i = 0; i < supported.length; i++) {
    		if (code == supported [i])
    			return true;
    	}
    	return false;
    }


    /** Returns true iff the device supports this operation */
    	public boolean supportsOperation (int opCode) {
	return supports (mOperationsSupported, opCode);
    }

    /** Returns true iff the device supports this event */
    	public boolean supportsEvent (int eventCode) {
	return supports (mEventsSupported, eventCode);
    }

    /** Returns true iff the device supports this property */
    public boolean supportsProperty (int propCode) {
    	return supports (mPropertiesSupported, propCode);
    }

    /** Returns true iff the device supports this capture format */
    public boolean supportsCaptureFormat (int formatCode) {
    	return supports (mCaptureFormats, formatCode);
    }

    /** Returns true iff the device supports this image format */
    public boolean supportsImageFormat (int formatCode) {
    	return supports (mImageFormats, formatCode);
    }

    public void parse (PtpBuffer data) {
	 data.parse();

	 mStandardVersion = data.nextU16 ();
	 mVendorExtensionId = /* unsigned */ data.nextS32 ();
	 mVendorExtensionVersion = data.nextU16 ();
	 mVendorExtensionDesc = data.nextString ();

	 mFunctionalMode = data.nextU16 ();
	 mOperationsSupported = data.nextU16Array ();
	 mEventsSupported = data.nextU16Array ();
	 mPropertiesSupported = data.nextU16Array ();

	 mCaptureFormats = data. nextU16Array ();
	 mImageFormats = data.nextU16Array ();
	 mManufacturer = data.nextString ();
	 mModel = data.nextString ();

	 mDeviceVersion = data.nextString ();
	 mSerialNumber = data.nextString ();
	 
	 mIsInitialized = true;
    } 
}
