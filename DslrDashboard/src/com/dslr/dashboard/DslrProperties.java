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