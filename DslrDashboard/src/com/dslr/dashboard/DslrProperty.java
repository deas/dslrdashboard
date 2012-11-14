package com.dslr.dashboard;

import java.util.ArrayList;

public class DslrProperty {
	private int mPropertyCode;
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