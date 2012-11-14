package com.dslr.dashboard;

import java.util.Hashtable;

public class DslrProperties {
	
	private Hashtable<Integer, DslrProperty> mProperties;
	private int mVendorId, mProductId;
	
	public DslrProperties(int vendorId, int productId){
		mVendorId = vendorId;
		mProductId = productId;
		mProperties = new Hashtable<Integer, DslrProperty>();
	}
	
	public Hashtable<Integer, DslrProperty> properties(){
		return mProperties;
	}
	
	public DslrProperty addProperty(int propertyCode){
		DslrProperty property = new DslrProperty(propertyCode);
		mProperties.put(propertyCode, property);
		return property;
	}
	
	public boolean containsProperty(int propertyCode){
		return mProperties.containsKey(propertyCode);
	}
	public DslrProperty getProperty(int propertyCode){
		return mProperties.get(propertyCode);
	}
	
	public int getVendorId(){
		return mVendorId;
	}
	public int getProductId(){
		return mProductId;
	}
} 