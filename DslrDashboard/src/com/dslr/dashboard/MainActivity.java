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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class MainActivity extends ActivityBase implements IDslrActivity {

    private static final String TAG = "MainActivity"; 
    
    private boolean mCheckForUsb = false; 
    private List<WeakReference<Fragment>> mFragments = new ArrayList<WeakReference<Fragment>>();
    private MenuItem mMainMenuItem;
    private MainMenuProvider mMainMenuProvider;
    
    private BottomFragment mBottomFragment;
    private RightFragment mRightFragment;
    private LeftFragment mLeftFragment;
    private LiveViewFragment mLiveViewFragment;
    private RightFragmentLv mRightFragmentLv;
    private AboutFragment mAboutFragment;
    private ProgressFragment mProgressFragment;

    private DslrFragmentBase mLeft, mRight, mBottom, mCenter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate"); 
        
        setContentView(R.layout.activity_main);
        
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (mPrefs.getBoolean(PtpDevice.PREF_KEY_GENERAL_SCREEN_ON, true))
        	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
        	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        mMainMenuProvider = new MainMenuProvider(this);

        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayUseLogoEnabled(true);
        
        mBottomFragment = new BottomFragment();
        mRightFragment = new RightFragment();
        mLeftFragment = new LeftFragment();
        mLiveViewFragment = new LiveViewFragment();
        mRightFragmentLv = new RightFragmentLv();
        mAboutFragment = new AboutFragment();
        mProgressFragment = new ProgressFragment();
        
        mBottom = mBottomFragment;
        mLeft = mLeftFragment;
        mRight = mRightFragment;
        mCenter = mAboutFragment;
        
        getFragmentManager().beginTransaction()
        	.replace(R.id.center_layout, mCenter)
        	.commit();
        
    }

    private void toggleKeepScreenOn(){
    	if (getPtpDevice().getGeneralScreenOn())
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    	else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.activity_main, menu);
        mMainMenuItem = menu.findItem(R.id.menu_action_test);
        mMainMenuItem.setActionProvider(mMainMenuProvider);
        if (mPtpDevice != null && mPtpDevice.getIsPtpDeviceInitialized()) 
        	mMainMenuItem.setVisible(true);
         else
        	mMainMenuItem.setVisible(false);
        return true;
    }

    @Override
    protected void onRestart() {
    	Log.d(TAG, "onRestart");
    	super.onRestart();
    }
    
    @Override
    protected void onStart() {
    	Log.d(TAG, "onStart");
    	super.onStart();
    }
    
    @Override
    protected void onResume() {
    	Log.d(TAG, "onResume");
    	checkUsbIntent(getIntent());
    	doBindService(PtpService.class);
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	Log.d(TAG, "onPause");
    	unbindPtpService(true, true);
    	setPtpDevice(null);
    	super.onPause();
    }
    
    @Override
    protected void onStop() {
    	Log.d(TAG, "onStop");
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
    	Log.d(TAG, "onDestroy");
    	super.onDestroy();
    }

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent");
		checkUsbIntent(intent);
		super.onNewIntent(intent);
	}
    
	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		Log.d(TAG, "onAttachFragment");
		try {
			if (fragment instanceof DslrFragmentBase)
				Log.d(TAG, "This is DslrFramentBase");
			DslrFragmentBase dslrFragment = (DslrFragmentBase)fragment;
			if (dslrFragment != null) {
				if (!mFragments.contains(fragment)) {
					Log.d(TAG, "attach new fragment");
					mFragments.add(new WeakReference<Fragment>(fragment));
				}
			}
		} catch (ClassCastException e) {
			Log.d(TAG, "Not DSLR fragment");
		}
	}
	
    private void checkUsbIntent(Intent intent) {
    	if (intent != null ) {
    		Bundle extras = intent.getExtras();
    		if (extras != null) {
    			if (extras.containsKey("UsbAttached"))
    				mCheckForUsb = true;
    		}
    	}
    }
	
    PtpDevice.OnPtpDeviceEventListener mPtpDeviceEventListener = new PtpDevice.OnPtpDeviceEventListener() {
		
		public void sendEvent(final PtpDeviceEvent event, final Object data) {
			Log.d(TAG, "OnPtpDeviceEventListener");
			runOnUiThread(new Runnable() {
				
				public void run() {
					switch(event) {
					case  PtpDeviceInitialized:
						initDisplay();
						break;
					case PtpDeviceStoped:
						removeDisplay();
						break;
					case PrefsLoaded:
						toggleKeepScreenOn();
						break;
					case LiveviewStart:
						//if (!mLiveViewFragment.getIsAttached())
							getFragmentManager().beginTransaction()
								//.remove(mCenter)
								.replace(R.id.center_layout, mLiveViewFragment)
								.commit();
						mCenter = mLiveViewFragment;
						break;
					case LiveviewStop:
						if (!mProgressFragment.getIsAttached()) {
							getFragmentManager().beginTransaction()
								//.remove(mCenter)
								.replace(R.id.center_layout, mAboutFragment)
								.commit();
							mCenter = mAboutFragment;
						}
						toggleFullscreen(false);
						toggleLvLayout(false);
						break;
					case SdCardInfoUpdated:
					case SdCardInserted:
					case SdCardRemoved:
						break;
					case CaptureStart:
						break;
					case CaptureComplete:
						break;
					case BusyBegin:
						if (!mProgressFragment.getIsAttached()) {
						getFragmentManager()
							.beginTransaction()
							.replace(R.id.center_layout, mProgressFragment)
							.commit();
							mCenter = mProgressFragment;
						}
						break;
					case BusyEnd:
						getFragmentManager()
							.beginTransaction()
							.replace(R.id.center_layout, mAboutFragment)
							.commit();
							mCenter = mAboutFragment;
						break;
					case ObjectAdded:
						break;
					case GetObjectFromSdramInfo:
						break;
					case GetObjectFromSdramThumb:
						break;
					case GetObjectFromSdramProgress:
						break;
					case GetObjectFromCameraProgress:
						break;
					case GetObjectFromSdramFinished:
						break;
					case GetObjectFromCameraFinished:
						break;
					}
					ptpDeviceEvent(event, data);
								
					
				}
			});
		}
	};
	
	PtpDevice.OnPtpPropertyChangedListener mPtpPropertyChangedListener = new PtpDevice.OnPtpPropertyChangedListener() {
		
		public void sendPtpPropertyChanged(final PtpProperty property) {
			runOnUiThread(new Runnable() {
				
				public void run() {
					ptpPropertyChanged(property);
				}
			});
		}
	};
	
	SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		
		public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences,
				final String key) {
			runOnUiThread(new Runnable() {
				
				public void run() {
					ptpDeviceSharedPrefs(sharedPreferences, key);
					if (key.equals(PtpDevice.PREF_KEY_GENERAL_SCREEN_ON))
						toggleKeepScreenOn();
				}
			});
		}
	};
	
	private void removeDisplay() {
		runOnUiThread(new Runnable() {
			
			public void run() {
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				if (mBottom != null)
					ft.remove(mBottom);
				if (mRight != null)
					ft.remove(mRight);
				if (mLeft != null)
					ft.remove(mLeft);
				if (mCenter != null && mCenter != mAboutFragment)
					ft.remove(mCenter);
				ft.commit();

				
				mMainMenuItem.setVisible(false);
			}
		});
	}
	
	private void initDisplay() {
		Log.d(TAG, "initDisplay");
		
		DslrHelper.getInstance().loadDslrProperties(this, mPtpDevice, mPtpDevice.getVendorId() , mPtpDevice.getProductId());
		

		if (mPtpDevice.getIsLiveViewEnabled())
			mCenter = mLiveViewFragment;
		else
			mCenter = mAboutFragment;
		
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		if (mBottom != null && !mBottom.getIsAttached()) //!mBottom.isAdded())
			ft.replace(R.id.bottom_layout, mBottom);
		
		if (!mIsFullScreen) {
			if (mRight != null && !mRight.getIsAttached()) // !mRight.isAdded())
				ft.replace(R.id.right_layout, mRight);
			if (mLeft != null && !mLeft.isAdded())
				ft.replace(R.id.left_layout, mLeft);
		}
		
		if (mCenter != null && !mCenter.getIsAttached()) // !mCenter.isAdded())
			ft.replace(R.id.center_layout, mCenter);
		
		ft.commit();
		
		
		runOnUiThread(new Runnable() {
			public void run() {
			
				if (mMainMenuItem != null) {
					mMainMenuItem.setVisible(true);
					mMainMenuProvider.initDisplay();
				}
			}
		});
	}
	
	private void ptpDeviceEvent(PtpDeviceEvent event, Object data) {
		for (WeakReference<Fragment> wr : mFragments) {
			Fragment fragment = wr.get();
			if (fragment != null) {
				try{
					DslrFragmentBase fr = (DslrFragmentBase)fragment;
					if (fr.getIsAttached())
						fr.ptpDeviceEvent(event, data);
				}
				catch (ClassCastException e) {
					
				}
			}
		}
		mMainMenuProvider.ptpDeviceEvent(event, data);
	}
	
	private void ptpPropertyChanged(PtpProperty property) {
		for (WeakReference<Fragment> wr : mFragments) {
			Fragment fragment = wr.get();
			if (fragment != null) {
				try{
					DslrFragmentBase fr = (DslrFragmentBase)fragment;
					if (fr.getIsAttached())
						fr.ptpPropertyChanged(property);
				}
				catch (ClassCastException e) {
					
				}
			}
		}
		mMainMenuProvider.ptpPropertyChanged(property);
	}
	
	private void ptpDeviceSharedPrefs(SharedPreferences prefs, String key) {
		for (WeakReference<Fragment> wr : mFragments) {
			Fragment fragment = wr.get();
			if (fragment != null) {
				try{
					DslrFragmentBase fr = (DslrFragmentBase)fragment;
					if (fr.getIsAttached())
						fr.sharedPrefsChanged(prefs, key);
				}
				catch (ClassCastException e) {
					
				}
			}
		}
	}
	
    private void setPtpDevice(PtpDevice ptpDevice) {
    	Log.d(TAG, "setPtpDevice");
    	// remove listener of the old PtpDevice
    	if (mPtpDevice != null) {
    		mPtpDevice.removeOnPtpDeviceEventListener();
    		mPtpDevice.removePtpPropertyChangedListener();
    		mPtpDevice.removeSharedPreferenceChangeListener();
//    		mPtpDevice.removeLiveViewObjectListener();
    	}
    	
    	mPtpDevice = ptpDevice;
    	
    	if (mPtpDevice != null) {
//    		if (mPtpDevice.getIsPtpDeviceInitialized()) {
//    			initDisplay();
//    		}
    		mPtpDevice.setOnPtpDeviceEventListener(mPtpDeviceEventListener);
    		mPtpDevice.setOnPtpPropertyChangedListener(mPtpPropertyChangedListener);
    		mPtpDevice.setOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
//    		mPtpDevice.setLiveviewObjectListener(mLiveViewListener);
    	}
    	
    }
    
//    private boolean mIsBound = false;
    
    private PtpService mPtpService = null;
    private PtpDevice mPtpDevice = null;

	public PtpDevice getPtpDevice() {
		return mPtpDevice;
	}     
    private void unbindPtpService(boolean stopService, boolean keepAliveIfUsbConnected) {
    	if (stopService && mPtpService != null)
    		mPtpService.stopPtpService(keepAliveIfUsbConnected);
    	doUnbindService(PtpService.class);
    	
    }

	@Override
	protected void serviceConnected(Class<?> serviceClass, ComponentName name, ServiceBase service) {
		Log.d(TAG, "onServiceConnected");
		
		if (serviceClass.equals(PtpService.class)) {
			Log.d(TAG, "PtpService connected");
			mPtpService = (PtpService) service;
		
			setPtpDevice(mPtpService.getPtpDevice());
		
			startService(new Intent(MainActivity.this, PtpService.class));

			if (mCheckForUsb)
				mPtpService.searchForUsbCamera();
		
			if (mPtpService.getIsUsbDeviceInitialized() && mPtpDevice.getIsPtpDeviceInitialized())
				initDisplay();
		}
	}

	@Override
	protected void serviceDisconnected(Class<?> serviceClass, ComponentName name) {
		Log.d(TAG, "onServiceDisconnected " + name.getClassName());
		
		if (serviceClass.equals(PtpService.class)) {
			Log.d(TAG, "PtpService disconnected");
			mPtpService = null;
		}
	}
	
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()) {
			case R.id.menu_settings:
				getFragmentManager().beginTransaction()
					.replace(R.id.center_layout, new SettingsFragment())
					.addToBackStack("prefs")
					.commit();
				return true;
			case R.id.menu_search_usb:
				if (!mPtpService.getIsUsbDeviceInitialized())
					mPtpService.searchForUsbCamera();
				return true;
			case R.id.menu_close_usb:
				if (mPtpService.getIsUsbDeviceInitialized())
					mPtpService.closeUsbConnection();
				return true;
			case R.id.menu_action_image_browse:

				Intent ipIntent = new Intent(this, DslrImageBrowserActivity.class);
				ipIntent.setAction(Intent.ACTION_VIEW);
				ipIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(ipIntent);
				
				return true;
			default:
				return true;
		}
	}

	private boolean mIsFullScreen = false;
	
	public void toggleFullscreen(boolean enable) {
		if (enable != mIsFullScreen) {
			if (mPtpDevice != null && mPtpDevice.getIsPtpDeviceInitialized()) {
			
				if (!enable) {
					getActionBar().show();
					getFragmentManager().beginTransaction()
						.replace(R.id.right_layout, mRight)
						.replace(R.id.left_layout, mLeft)
						.commit();
				
				} else {
					getActionBar().hide();
					getFragmentManager().beginTransaction()
						.remove(mRight)
						.remove(mLeft)
						.commit();
				}
				mIsFullScreen = enable;
			}
		}
	}
	public void toggleFullScreen() {
		toggleFullscreen(!mIsFullScreen);
	}

	public boolean getIsFullScreen() {
		return mIsFullScreen;
	}
	
	private boolean mIsLvLayoutEnabled = false;

	public boolean getIsLvLayoutEnabled() {
		return mIsLvLayoutEnabled;
	}
	public void toggleLvLayout() {
		toggleLvLayout(!mIsLvLayoutEnabled);
	}
	public void toggleLvLayout(boolean showLvLayout) {
		if (showLvLayout) {
			if (mRight != mRightFragmentLv) {
				mRight = mRightFragmentLv;
				getFragmentManager().beginTransaction()
					.replace(R.id.right_layout, mRight)
					.commit();
			}
			mIsLvLayoutEnabled = true;
		} else {
			if (mRight != mRightFragment) {
				mRight = mRightFragment;
				getFragmentManager().beginTransaction()
				.replace(R.id.right_layout, mRight)
					.commit();
			}
			mIsLvLayoutEnabled = false;
		}
	}
	public void zoomLiveView(boolean up){
		if (mPtpDevice != null && mPtpDevice.getIsPtpDeviceInitialized()){
			// check if liveview is enabled
			PtpProperty lvStatus = mPtpDevice.getPtpProperty(PtpProperty.LiveViewStatus);
		
			if (lvStatus != null && (Integer)lvStatus.getValue() == 1){
				// get the zoom factor
				PtpProperty zoom = mPtpDevice.getPtpProperty(PtpProperty.LiveViewImageZoomRatio);
				if (zoom != null){
					int zValue = (Integer)zoom.getValue();
					if (up){
						if (zValue < 5)
							mPtpDevice.setDevicePropValueCmd(PtpProperty.LiveViewImageZoomRatio, zValue + 1);
					}
					else {
						if (zValue > 0)
							mPtpDevice.setDevicePropValueCmd(PtpProperty.LiveViewImageZoomRatio, zValue - 1);
					}
				}
			}
		}
		
	}

	@Override
	protected void processArduinoButtons(EnumSet<ArduinoButtonEnum> pressedButtons, EnumSet<ArduinoButtonEnum> releasedButtons) {
		for (ArduinoButtonEnum button : releasedButtons) {
			switch (button) {
			case Button0:
				if (!button.getIsLongPress())
					mPtpDevice.initiateCaptureCmd();
				else
					mPtpDevice.setCaptureToSdram(!mPtpDevice.getCaptureToSdram());
				break;
			case Button4:
				generateKeyEvent(new KeyEvent(button.getLongPressStart(), button.getLongPressEnd(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK,0));
				break;
			case Button5:
				generateKeyEvent(new KeyEvent(button.getLongPressStart(), button.getLongPressEnd(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER,0));
				break;
			case Button6:
				generateKeyEvent(new KeyEvent(button.getLongPressStart(), button.getLongPressEnd(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT,0));
				break;
			case Button7:
				generateKeyEvent(new KeyEvent(button.getLongPressStart(), button.getLongPressEnd(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT,0));
				break;
			case Button8:
				generateKeyEvent(new KeyEvent(button.getLongPressStart(), button.getLongPressEnd(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP,0));
				break;
			case Button9:
				generateKeyEvent(new KeyEvent(button.getLongPressStart(), button.getLongPressEnd(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN,0));
				break;
			}
			Log.d(TAG, "Released button: " + button.toString() + " is long press: " + button.getIsLongPress());
		}
		for (ArduinoButtonEnum button : pressedButtons) {
			switch(button){
			case Button4:
				generateKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
				break;
			case Button5:
				generateKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
				break;
			case Button6:
				generateKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
				break;
			case Button7:
				generateKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
				break;
			case Button8:
				generateKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
				break;
			case Button9:
				generateKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
				break;
			}
			Log.d(TAG, "Pressed button: " + button.toString());
		}
	}
	
}

