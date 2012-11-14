package com.dslr.dashboard;

import java.util.Hashtable;

import android.util.Log;

public class PtpStorageInfo {
	private static String TAG = "PtpStorageInfo";
	
	public int storageId;
	public int storageType;
	public int filesystemType;
	public int accessCapability;
	public long maxCapacity;
	public long freeSpaceInBytes;
	public int freeSpaceInImages;
	public String storageDescription;
	public String volumeLabel;
	
	public boolean isObjectsLoaded = false;
	
	public Hashtable<Integer, PtpObjectInfo> objects;
	
	public PtpStorageInfo(int id, PtpBuffer buf)
	{
		objects = new Hashtable<Integer, PtpObjectInfo>();
		
		storageId = id;
		updateInfo(buf);
	}
	
	public void updateInfo(PtpBuffer buf){
		buf.parse();
		storageType = buf.nextU16();
		filesystemType = buf.nextU16();
		accessCapability = buf.nextU16();
		maxCapacity = buf.nextS64();
		freeSpaceInBytes = buf.nextS64();
		freeSpaceInImages = buf.nextS32();
		storageDescription = buf.nextString();
		volumeLabel = buf.nextString();
		
		Log.d(TAG, String.format("Storage id: %#04x images: %d", storageId, freeSpaceInImages));
	} 
	
	public void deleteObject(int objectId) {
		if (objects.containsKey(objectId)) {
			objects.remove(objectId);
		}
	}
}
