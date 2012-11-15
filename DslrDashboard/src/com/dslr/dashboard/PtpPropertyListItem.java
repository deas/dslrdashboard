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

public class PtpPropertyListItem {
	private String mTitle;
	private int mImage;
	private Object mValue;
	private int mNameId;

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}
	public int getImage() {
		return mImage;
	}
	public void setImage(int image) {
		this.mImage = image;
	}	
	public Object getValue(){
		return mValue;
	}
	public void setValue(Object value){
		mValue = value;
	}
	public int getNameId(){
		return mNameId;
	} 
	public void setNameId(int value){
		mNameId = value;
	}
	
	
	public PtpPropertyListItem(){
		
	}
	public PtpPropertyListItem(Object propValue, int propNameId, int propImgId){
		mValue = propValue;
		mNameId = propNameId;
		mImage = propImgId;
	}
	
	public PtpPropertyListItem(String title) {
		mTitle = title;
	}
}
