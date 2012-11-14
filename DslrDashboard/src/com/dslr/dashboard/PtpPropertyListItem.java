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
