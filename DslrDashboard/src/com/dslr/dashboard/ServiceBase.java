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
