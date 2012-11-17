package com.dslr.dashboard;

import java.io.File;
import java.util.LinkedList;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class GpsLocationHelper {

	private final static String TAG = GpsLocationHelper.class.getSimpleName();
	
	private Context mContext = null;
	private LocationManager mLocationManager;
    private Location mLastLocation = null;
    private LinkedList<File> mGpsList;
    private boolean mIsWaitingForGpsUpdate = false;
    private int mGpsSampleInterval = 1;
    public int mGpsSampleCount = 3; 
	private int mGpsUpdateCount = 0;
	private Thread mGpsThread;
	private Handler mGpsHandler = null;
    
	public void setGpsSampleCount(int value) {
		mGpsSampleCount = value;
	}
	public void setGpsSampleInterval(int value) {
		mGpsSampleInterval = value;
	}
	public GpsLocationHelper(Context context) {
		mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mGpsList = new LinkedList<File>();
        
        mGpsThread = new Thread() {
            public void run() {
                Log.d( TAG,"Creating handler ..." );
                Looper.prepare();   
                mGpsHandler = new Handler();
                Looper.loop();
                Log.d( TAG, "Looper thread ends" );
            }
        };
        mGpsThread.start();
    }
	
	public void stopGpsLocator() {
		Log.d(TAG, "stopGpsLocator");
		mGpsHandler.getLooper().quit();
		mGpsHandler = null;
		mLocationManager.removeUpdates(locationListener);
	}
	
	// Define a listener that responds to location updates
	LocationListener locationListener = new LocationListener() {
	    public void onLocationChanged(Location location) {
	    	
	    	Log.i(TAG, "New location latitude: " + location.getLatitude() + " longituted: " + location.getLongitude() + " altitude: " + location.getAltitude() + " count: " + mGpsUpdateCount);
	    	if (isBetterLocation(location, mLastLocation))
	    		mLastLocation = location;
	    	mGpsUpdateCount++;
	    	//Log.i(TAG, "GPS time: " + location.getTime());
	    	//Log.i(TAG, "Time: " + System.currentTimeMillis());
	    	// wait for 3 location updates
	    	if (mGpsUpdateCount == mGpsSampleCount) {
	    		mLocationManager.removeUpdates(locationListener);
	    		//_gpsUpdateCount = 0;
	    		synchronized (mGpsList) {
	    		
	    			while(!mGpsList.isEmpty()){
	    				File file = mGpsList.poll();
	    				setImageGpsLocation(location, file);
	    			}
	    			mIsWaitingForGpsUpdate = false;
	    		}
	    	}
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {
	    	Log.i(TAG, "Status changes: " + status);
	    }

	    public void onProviderEnabled(String provider) {
	    	Log.i(TAG, "Provider enabled: " + provider);
	    }

	    public void onProviderDisabled(String provider) {
	    	Log.i(TAG, "Provider disable: " + provider);
	    }
	  }; 	
	
		/** Determines whether one Location reading is better than the current Location fix
		  * @param location  The new Location that you want to evaluate
		  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
		  */
		protected boolean isBetterLocation(Location location, Location currentBestLocation) {
			
		    if (currentBestLocation == null) {
		        // A new location is always better than no location
		        return true;
		    }

		    // Check whether the new location fix is newer or older
		    long timeDelta = location.getTime() - currentBestLocation.getTime();
		    boolean isSignificantlyNewer = timeDelta > getSampleInterval();
		    boolean isSignificantlyOlder = timeDelta < -getSampleInterval();
		    boolean isNewer = timeDelta > 0;

		    // If it's been more than two minutes since the current location, use the new location
		    // because the user has likely moved
		    if (isSignificantlyNewer) {
		        return true;
		    // If the new location is more than two minutes older, it must be worse
		    } else if (isSignificantlyOlder) {
		        return false;
		    }

		    // Check whether the new location fix is more or less accurate
		    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		    boolean isLessAccurate = accuracyDelta > 0;
		    boolean isMoreAccurate = accuracyDelta < 0;
		    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		    // Check if the old and new location are from the same provider
		    boolean isFromSameProvider = isSameProvider(location.getProvider(),
		            currentBestLocation.getProvider());

		    // Determine location quality using a combination of timeliness and accuracy
		    if (isMoreAccurate) {
		        return true;
		    } else if (isNewer && !isLessAccurate) {
		        return true;
		    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
		        return true;
		    }
		    return false;
		}
		
		private int getSampleInterval(){
			return 1000 * 60 * mGpsSampleInterval;
		} 
		
		/** Checks whether two providers are the same */
		private boolean isSameProvider(String provider1, String provider2) {
		    if (provider1 == null) {
		      return provider2 == null;
		    }
		    return provider1.equals(provider2);
		}
		
		private void setImageGpsLocation(Location location, File file) {
	    	NativeMethods.getInstance().setGPSExifData(file.getAbsolutePath(), location.getLatitude(), location.getLongitude(), location.getAltitude());
	    	runMediaScanner(file);
		}
		
		private void runMediaScanner(File file) {
		    Uri contentUri = Uri.fromFile(file);
		    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		    mediaScanIntent.setData(contentUri);
		    mContext.sendBroadcast(mediaScanIntent);		
		} 
		
		private boolean needLocationUpdate() {
			if (mLastLocation != null) {
				long utcTime = System.currentTimeMillis();
				if ((utcTime - mLastLocation.getTime()) < getSampleInterval())
						return false;
			}
			return true;
		} 
		
		public void updateGpsLocation() {
			if (mGpsHandler != null)
				mGpsHandler.post(new Runnable() {
					
					public void run() {
						Log.d(TAG, "Getting new GPS Location");
						if (!mIsWaitingForGpsUpdate) {
							mIsWaitingForGpsUpdate = true;
							mGpsUpdateCount = 0;
							Log.d(TAG, "Need GPS location update");
							mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
						}
					}
				});
		}
		public void addGpsLocation(File file) {

			synchronized (mGpsList) {
				Log.d(TAG, "Getting exif gps data");
				if (needLocationUpdate()) {
					mGpsList.push(file);
					Log.d(TAG, "Need new GPS Location");
					updateGpsLocation();
				}
				else {
					setImageGpsLocation(mLastLocation, file);
				}
			}
		}
		
}
