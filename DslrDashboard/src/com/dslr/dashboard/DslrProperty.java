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

import java.util.ArrayList;

public class DslrProperty {
	private int mPropertyCode;
	private int mPropertyTitle;
	private ArrayList<String> mValues;
	private ArrayList<PtpPropertyListItem> mPropertyValues;
	
	public DslrProperty(int ptpPropertyCode){
		mPropertyCode = ptpPropertyCode;
		mPropertyValues = new ArrayList<PtpPropertyListItem>();
		mValues = new ArrayList<String>();
	}
	
	public int propertyCode(){
		return mPropertyCode;
	}
	public int propertyTitle() {
		return mPropertyTitle;
	}
	public void setPropertyTitle(int value) {
		mPropertyTitle = value;
	}
	public ArrayList<PtpPropertyListItem> valueNames(){
		return mPropertyValues;
	}
	public ArrayList<String> values(){
		return mValues;
	}
	public int indexOfValue(Object value){
		return mValues.indexOf(value.toString());
	}
	public int getImgResourceId(Object value){
		PtpPropertyListItem prop = getPropertyByValue(value);
		if (prop == null)
			return 0;
		return prop.getImage();
	}
	public int getnameResourceId(Object value){
		PtpPropertyListItem prop = getPropertyByValue(value);
		if (prop == null)
			return 0;
		return prop.getNameId();
	}
	public PtpPropertyListItem getPropertyByValue(Object value){
		int index = indexOfValue(value);
		if (index == -1)
			return null;
		return mPropertyValues.get(index);
	}
	public PtpPropertyListItem addPropertyValue(Object value, int nameId, int imgId){
		PtpPropertyListItem item = new PtpPropertyListItem(value, nameId, imgId);
		mPropertyValues.add(item);
		mValues.add(value.toString());
		return item;
	}
} 