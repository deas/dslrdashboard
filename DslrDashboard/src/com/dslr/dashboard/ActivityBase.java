package com.dslr.dashboard;

import java.util.EnumSet;
import java.util.HashMap;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;

public abstract class ActivityBase extends Activity {

	private final static String TAG = ActivityBase.class.getSimpleName();
	
	private HashMap<Class<?>, ServiceConnectionHelper> mBindServices = new HashMap<Class<?>, ServiceConnectionHelper>();
	
    private ArduinoButton mArduinoButtons;
    private Instrumentation mInstrumentation;
    private Handler mInstrumentationHandler;
    private Thread mInstrumentationThread;
    protected SharedPreferences mPrefs;
	
	private class ServiceConnectionHelper {
		public boolean isBound = false;
		public Class<?> mServiceClass;
		
		public ServiceConnectionHelper(Class<?> serviceClass) {
			mServiceClass = serviceClass;
		}
	    public ServiceConnection serviceConnection = new ServiceConnection() {
			
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected");
				doServiceDisconnected(mServiceClass, name);
			}
			
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onServiceConnected");
				
				ServiceBase serviceBase = ((ServiceBase.MyBinder)service).getService();
				doServiceConnected(mServiceClass, name, serviceBase);
				
				
			}
		};
		
	}
	protected boolean getIsServiceBind(Class<?> serviceClass) {
		if (mBindServices.containsKey(serviceClass))
			return mBindServices.get(serviceClass).isBound;
		else
			return false;
			
	}
	protected void doBindService(Class<?> serviceClass ) {
	    // Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process (and thus won't be
	    // supporting component replacement by other applications).

		Log.d(TAG, "doBindService " + serviceClass.getSimpleName());

		ServiceConnectionHelper helper;
		if (!mBindServices.containsKey(serviceClass)) 
			helper = new ServiceConnectionHelper(serviceClass);
		else
			helper = mBindServices.get(serviceClass);
		if (!helper.isBound) {
			bindService(new Intent(this, serviceClass), helper.serviceConnection, Context.BIND_AUTO_CREATE);// new Intent(MainActivity.this, PtpService.class), serviceConnection, Context.BIND_AUTO_CREATE);
			helper.isBound = true;
			mBindServices.put(serviceClass, helper);
		}
	}

	protected void doUnbindService(Class<?> serviceClass) {
		Log.d(TAG, "doUnbindService: " + serviceClass.getSimpleName());
		if (mBindServices.containsKey(serviceClass)) {
			ServiceConnectionHelper helper = mBindServices.get(serviceClass);
			unbindService(helper.serviceConnection);
			helper.isBound = false;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		super.onCreate(savedInstanceState);
	}
	
	private void startButtonThread() {
        mInstrumentation = new Instrumentation();
        mInstrumentationThread = new Thread() {
            public void run() {
                Log.d( TAG,"Creating handler ..." );
                Looper.prepare();   
                mInstrumentationHandler = new Handler();
                Looper.loop();
                Log.d( TAG, "Looper thread ends" );
            }
        };
        mInstrumentationThread.start();
		
        mArduinoButtons = new ArduinoButton();
        mArduinoButtons.setArduinoButtonListener(new ArduinoButton.ArduinoButtonListener() {
			
			public void buttonStateChanged(EnumSet<ArduinoButtonEnum> pressedButtons,
					EnumSet<ArduinoButtonEnum> releasedButtons) {
				processArduinoButtons(pressedButtons, releasedButtons);
			}
		});
	}
	@Override
	protected void onStart() {
		super.onStart();
	}
	@Override
	protected void onResume() {
		startButtonThread();
		doBindService(UsbSerialService.class);
		super.onResume();
	}
	@Override
	protected void onPause() {
		stopUsbSerialService();
		mInstrumentationHandler.getLooper().quit();
		mInstrumentationHandler = null;
		super.onPause();
	}
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	private void stopUsbSerialService() {
		if (mUsbSerialService != null)
			mUsbSerialService.stopSelf();
		doUnbindService(UsbSerialService.class);
	}
	private UsbSerialService mUsbSerialService = null;
	
	private void doServiceConnected(Class<?> serviceClass, ComponentName name, ServiceBase service){
		if (serviceClass.equals(UsbSerialService.class)) {
			mUsbSerialService = (UsbSerialService)service;
			mUsbSerialService.setButtonStateChangeListener(new UsbSerialService.ButtonStateChangeListener() {
				
				public void onButtonStateChanged(int buttons) {
					mArduinoButtons.newButtonState(buttons);
				}
			});
			
			startService(new Intent(this, UsbSerialService.class));
		} else
			serviceConnected(serviceClass, name, service);
	}
	
	private void doServiceDisconnected(Class<?> serviceClass, ComponentName name){
		if (serviceClass.equals(UsbSerialService.class)) {
			mUsbSerialService = null;
		}
		else
			serviceDisconnected(serviceClass, name);
	}

	protected void generateKeyEvent(final KeyEvent key) {
		if (mInstrumentationHandler != null) {
		mInstrumentationHandler.post(new Runnable() {
			
			public void run() {
				try {
					mInstrumentation.sendKeySync(key);
				} catch (Exception e) {
					Log.e(TAG, "Exception: " + e.getMessage());
				}
			}
		});
		}
	};
	protected void generateKeyEvent(final int key) {
		if (mInstrumentationHandler != null) {
		mInstrumentationHandler.post(new Runnable() {
			
			public void run() {
				try {
				mInstrumentation.sendKeyDownUpSync(key);
				} catch (Exception e) {
					Log.e(TAG, "Exception: " + e.getMessage());
				}
			}
		});
		}
	}
	
	protected abstract void serviceConnected(Class<?> serviceClass, ComponentName name, ServiceBase service);
	protected abstract void serviceDisconnected(Class<?> serviceClass, ComponentName name);
	protected abstract void processArduinoButtons(EnumSet<ArduinoButtonEnum> pressedButtons, EnumSet<ArduinoButtonEnum> releasedButtons);
	
}
