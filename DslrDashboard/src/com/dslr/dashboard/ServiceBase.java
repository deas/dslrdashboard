package com.dslr.dashboard;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public abstract class ServiceBase extends Service {

	private final static String TAG = ServiceBase.class.getSimpleName();
	
	protected boolean mIsBind = false;
	
	public class MyBinder extends Binder {
		public ServiceBase getService() {
			Log.d(TAG, ServiceBase.this.getClass().getSimpleName());
			return ServiceBase.this;
		}
	}
	
	private final IBinder mBinder = new MyBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		mIsBind = true;
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "onUnbind");
		mIsBind = false;
		return super.onUnbind(intent);
	}
	
	public void stopService() {
		stopSelf();
	}
	
}
