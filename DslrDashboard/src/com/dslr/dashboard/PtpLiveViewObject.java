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

import android.graphics.RectF;

public class PtpLiveViewObject {

	public static final int FIXED_POINT = 16;
    public static final int ONE = 1 << FIXED_POINT;
    
    private int mVendorId, mProductId;
    
	public LvSizeInfo jpegImageSize;
	LvSizeInfo wholeSize;
	LvSizeInfo displayAreaSize;
	LvSizeInfo displayCenterCoordinates;
	LvSizeInfo afFrameSize;
	LvSizeInfo afFrameCenterCoordinates;
	int noPersons = 0;
	LvSizeInfo[] personAfFrameSize = null;//= new SizeInfo[5];
	LvSizeInfo[] personAfFrameCenterCoordinates = null; // = new SizeInfo[5];
	public RectF[] afRects = null; // = new RectF[6];
	public int selectedFocusArea = 0;
	public int rotationDirection = 0;
	public int focusDrivingStatus = 0;
	public int shutterSpeedUpper = 0;
	public int shutterSpeedLower = 0;
	public int apertureValue = 0;
	public int countDownTime = 0;
	public int focusingJudgementResult = 0;
	public int afDrivingEnabledStatus = 0;
	public int levelAngleInformation = 0;
	public int faceDetectionAfModeStatus = 0;
	public int faceDetectionPersonNo = 0;
	public int afAreaIndex = 0;
	public byte[] data;
	
	// d7000 properties
	public double rolling = 0;
	public boolean hasRolling = false;
	public double pitching = 0;
	public boolean hasPitching = false;
	public double yawing = 0;
	public boolean hasYawing = false;
	
	public int movieRecordingTime = 0;
	public boolean movieRecording = false;
	
	public boolean hasApertureAndShutter = true;
	
	public float ox, oy, dLeft, dTop;
	
	public int imgLen;
	public int imgPos;
	
	public PtpLiveViewObject(int vendorId, int productId){
		mVendorId = vendorId;
		mProductId = productId;
		switch (mProductId){
		case 0x0429: // d5100
		case 0x0428: // d7000
		case 0x042a: // d800
		case 0x042e: // d800e
		case 0x042d: // d600
			noPersons = 35;
			break;
		case 0x0423: // d5000
		case 0x0421: // d90
			noPersons = 5;
			break;
		case 0x041a: // d300
		case 0x041c: // d3
		case 0x0420: // d3x
		case 0x0422: // d700
		case 0x0425: // d300s
		case 0x0426: // d3s
			noPersons = 0;
			break;
		}
		personAfFrameSize = new LvSizeInfo[noPersons];
		personAfFrameCenterCoordinates = new LvSizeInfo[noPersons];
		afRects = new RectF[noPersons + 1];
		for (int i = 0; i < noPersons; i++) {
			personAfFrameSize[i] = new LvSizeInfo();
			personAfFrameCenterCoordinates[i] = new LvSizeInfo();
		}
		jpegImageSize = new LvSizeInfo();
		wholeSize = new LvSizeInfo();
		displayAreaSize = new LvSizeInfo();
		displayCenterCoordinates = new LvSizeInfo();
		afFrameSize = new LvSizeInfo();
		afFrameCenterCoordinates = new LvSizeInfo();
	}

	public float sDw, sDh;
	
	private PtpBuffer buf = null;
	
	public void setBuffer(PtpBuffer buffer){
		buf = buffer;
	}
	
	public void parse(int sWidth, int sHeight){
		if (buf != null){
			buf.parse();
			
			switch (mProductId){
				case 0x042d: // d600
					buf.nextS32(); // display information area size
					buf.nextS32(); // live view image area size
					break;
			}			
			jpegImageSize.setSize(buf.nextU16(true), buf.nextU16(true));
			
			sDw = (float)sWidth / (float)jpegImageSize.horizontal;
			sDh = (float)sHeight / (float)jpegImageSize.vertical;
			
			//Log.d(MainActivity.TAG, "++ Width: " + jpegImageSize.horizontal + " height: " + jpegImageSize.vertical);
			wholeSize.setSize(buf.nextU16(true), buf.nextU16(true));
			displayAreaSize.setSize(buf.nextU16(true), buf.nextU16(true));
			displayCenterCoordinates.setSize(buf.nextU16(true), buf.nextU16(true));
			afFrameSize.setSize(buf.nextU16(true), buf.nextU16(true));
			afFrameCenterCoordinates.setSize(buf.nextU16(true), buf.nextU16(true));
			
			buf.nextS32(); // reserved
			selectedFocusArea = buf.nextU8();
			rotationDirection = buf.nextU8();
			focusDrivingStatus = buf.nextU8();
			buf.nextU8(); // reserved
			shutterSpeedUpper = buf.nextU16(true);
			shutterSpeedLower = buf.nextU16(true);
			apertureValue = buf.nextU16(true);
			countDownTime = buf.nextU16(true);
			focusingJudgementResult = buf.nextU8();
			afDrivingEnabledStatus = buf.nextU8();
			buf.nextU16(); // reserved
	
	        ox = (float)jpegImageSize.horizontal / (float)displayAreaSize.horizontal;
	        oy = (float)jpegImageSize.vertical / (float)displayAreaSize.vertical;
	        dLeft = ((float)displayCenterCoordinates.horizontal - ((float)displayAreaSize.horizontal / 2));
	        dTop = ((float)displayCenterCoordinates.vertical - ((float)displayAreaSize.vertical / 2));
			
			switch (mProductId){
			case 0x0426: // d3s
		        CalcCoord(0, dLeft, dTop, afFrameCenterCoordinates, afFrameSize);
				hasApertureAndShutter = false;
				hasRolling = true;
				rolling = ((double)buf.nextS32(true)) / ONE;
		        imgPos = 128 + 12;
				break;
			case 0x0420: // d3x
		        CalcCoord(0, dLeft, dTop, afFrameCenterCoordinates, afFrameSize);
				hasRolling = true;
				rolling = ((double)buf.nextS32(true)) / ONE;
		        imgPos = 64 + 12;
				break;
			case 0x041a: // d300
			case 0x041c: // d3
			case 0x0422: // d700
			case 0x0425: // d300s
		        CalcCoord(0, dLeft, dTop, afFrameCenterCoordinates, afFrameSize);
		        imgPos = 64 + 12;
				break;
			case 0x0429: // d5100
			case 0x0428: // d7000				break;

			case 0x042a: // d800
			case 0x042e: // d800e
			case 0x042d: // d600
				// d800 don't have the aperture and shutter values
				if (mProductId == 0x042a || mProductId == 0x042e)
					hasApertureAndShutter = false;
				
				hasRolling = true;
				rolling = ((double)buf.nextS32(true)) / ONE;
				hasPitching = true;
				pitching = ((double)buf.nextS32(true)) / ONE;
				hasYawing = true;
				yawing = ((double)buf.nextS32(true)) / ONE;
				
				movieRecordingTime = buf.nextS32();
				movieRecording = buf.nextU8() == 1;
				faceDetectionAfModeStatus = buf.nextU8();
				faceDetectionPersonNo = buf.nextU8();
				afAreaIndex = buf.nextU8();
				
		        CalcCoord(0, dLeft, dTop, afFrameCenterCoordinates, afFrameSize);
	
				for(int i = 0; i < noPersons; i++){
					personAfFrameSize[i].setSize(buf.nextU16(true), buf.nextU16(true));
					personAfFrameCenterCoordinates[i].setSize(buf.nextU16(true), buf.nextU16(true));
					
					if ((i+1) <= faceDetectionPersonNo)
						CalcCoord(i + 1, dLeft, dTop, personAfFrameCenterCoordinates[i], personAfFrameSize[i]);
				}
				
				imgPos = 384 + 12;
		        
				break;
			case 0x0423: // d5000
			case 0x0421: // d90
				levelAngleInformation = buf.nextS32(true);
				faceDetectionAfModeStatus = buf.nextU8();
				buf.nextU8(); // reserved
				faceDetectionPersonNo = buf.nextU8();
				afAreaIndex = buf.nextU8();
	
		        CalcCoord(0, dLeft, dTop, afFrameCenterCoordinates, afFrameSize);
				
				for(int i = 0; i < noPersons; i++){
					personAfFrameSize[i].setSize(buf.nextU16(true), buf.nextU16(true));
					personAfFrameCenterCoordinates[i].setSize(buf.nextU16(true), buf.nextU16(true));
					
					if ((i+1) <= faceDetectionPersonNo)
						CalcCoord(i + 1, dLeft, dTop, personAfFrameCenterCoordinates[i], personAfFrameSize[i]);
				}
				imgPos = 128+12;
				break;
			}
			imgLen = buf.data().length - imgPos;
			data = buf.data();
		}
		//System.arraycopy(buf.data(), imgPos, imgData, 0, imgLen);
		
	}
	
    private void CalcCoord(int coord, float dLeft, float dTop, LvSizeInfo afCenter, LvSizeInfo afSize)
    {
    	float left, top, right, bottom;
        left = (((float)afCenter.horizontal - ((float)afSize.horizontal / 2)) - dLeft) * ox;
        top = (((float)afCenter.vertical - ((float)afSize.vertical / 2)) - dTop) * ox;
        if (left < 0)
            left = 0;
        if (top < 0)
            top = 0;
        right = left + ((float)afSize.horizontal * ox);
        bottom = top + ((float)afSize.vertical * oy);
        if (right > jpegImageSize.horizontal)
        	right = jpegImageSize.horizontal;
        if (bottom > jpegImageSize.vertical)
        	bottom = jpegImageSize.vertical;
        //Log.d(MainActivity.TAG, "++ Left: " + left + " top: " + top +" right: " + right + " bottom: " + bottom);
        afRects[coord] = new RectF(left * sDw, top * sDh, right * sDw, bottom * sDh);
    }

    public class LvSizeInfo {
    	public int horizontal;
    	public int vertical;
    	
    	public LvSizeInfo(){
    		horizontal = 0;
    		vertical = 0;
    	}
    	public LvSizeInfo(int horSize, int vertSize){
    		horizontal = horSize;
    		vertical = vertSize;
    	}
    	
    	public void setSize(int hor, int vert){
    		horizontal = hor;
    		vertical = vert;
    	}
    	
    	public void setSize(LvSizeInfo sizeInfo){
    		horizontal = sizeInfo.horizontal;
    		vertical = sizeInfo.vertical;
    	}
    }     
}