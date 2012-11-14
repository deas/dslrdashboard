package com.dslr.dashboard;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

public class PtpDevice {
	private static final String TAG = "PtpDevice";
	public final static String PREF_KEY_LV_AT_START = "pref_key_lv_at_start";
	public final static String PREF_KEY_LV_RETURN = "pref_key_lv_return";
	public final static String PREF_KEY_SHOOTING_AF = "pref_key_shooting_af";
	public final static String PREF_KEY_GENERAL_SCREEN_ON = "pref_key_general_screen_on";
	public final static String PREF_KEY_GENERAL_INTERNAL_VIEWER = "pref_key_general_internal_viewer";
	public final static String PREF_KEY_SDRAM_LOCATION = "pref_key_sdram_saving_location";
	public final static String PREF_KEY_SDRAM_PREFIX = "pref_key_sdram_prefix";
	public final static String PREF_KEY_SDRAM_NUMBERING = "pref_key_sdram_numbering";
	
	public final static String PREF_KEY_BKT_ENABLED = "pref_key_bkt_enabled";
	public final static String PREF_KEY_BKT_COUNT = "pref_key_bkt_count";
	public final static String PREF_KEY_BKT_DIRECTION = "pref_key_bkt_direction";
	public final static String PREF_KEY_BKT_STEP = "pref_key_bkt_step";
	public final static String PREF_KEY_BKT_FOCUS_FIRST = "pref_key_bkt_focus_first";

	public final static String PREF_KEY_TIMELAPSE_INTERVAL = "pref_key_timelapse_interval";
	public final static String PREF_KEY_TIMELAPSE_ITERATIONS = "pref_key_timelapse_iterations";

	public final static String PREF_KEY_FOCUS_IMAGES = "pref_key_focus_images";
	public final static String PREF_KEY_FOCUS_STEPS = "pref_key_focus_steps";
	public final static String PREF_KEY_FOCUS_DIRECTION_DOWN = "pref_key_focus_direction_down";
	public final static String PREF_KEY_FOCUS_FOCUS_FIRST = "pref_key_focus_focus_first";
	
	public interface OnPtpDeviceEventListener {
		public void sendEvent(PtpDeviceEvent event, Object data);
	}

	private OnPtpDeviceEventListener mOnPtpDeviceEventListener = null;

	public void setOnPtpDeviceEventListener(OnPtpDeviceEventListener listener) {
		mOnPtpDeviceEventListener = listener;
	}

	public void removeOnPtpDeviceEventListener() {
		mOnPtpDeviceEventListener = null;
	}

	private void sendPtpDeviceEvent(PtpDeviceEvent event) {
		sendPtpDeviceEvent(event, null);
	}

	private void sendPtpDeviceEvent(PtpDeviceEvent event, Object data) {
		if (mOnPtpDeviceEventListener != null)
			mOnPtpDeviceEventListener.sendEvent(event, data);
	}

	public interface OnPtpPropertyChangedListener {
		public void sendPtpPropertyChanged(PtpProperty property);
	}

	private OnPtpPropertyChangedListener mPtpPropertyChangedListener = null;

	public void setOnPtpPropertyChangedListener(
			OnPtpPropertyChangedListener listener) {
		mPtpPropertyChangedListener = listener;
	}

	public void removePtpPropertyChangedListener() {
		mPtpPropertyChangedListener = null;
	}

	private void sendPtpPropertyChanged(PtpProperty property) {
		if (mPtpPropertyChangedListener != null)
			mPtpPropertyChangedListener.sendPtpPropertyChanged(property);
	}

	private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener = null;

	public void setOnSharedPreferenceChangeListener(
			SharedPreferences.OnSharedPreferenceChangeListener listener) {
		mPrefListener = listener;
	}

	public void removeSharedPreferenceChangeListener() {
		mPrefListener = null;
	}

	public boolean getIsCommandSupported(int ptpCommand) {
		if (!mIsPtpDeviceInitialized)
			return false;
		return mDeviceInfo.supportsOperation(ptpCommand);
	}

	public boolean getIsMovieRecordingStarted() {
		return mIsMovieRecordingStarted;
	}

	public boolean getCaptureToSdram() {
		return mCaptureToSdram;
	}

	public void setCaptureToSdram(boolean value) {
		mCaptureToSdram = value;
		sendPtpDeviceEvent(PtpDeviceEvent.RecordingDestinationChanged, mCaptureToSdram);
	}

	private int mVendorId = 0;
	private int mProductId = 0;

	private PtpService mPtpService = null;
	private SharedPreferences mPrefs = null;
	private PtpCommunicatorBase mCommunicator = null;
	private boolean mIsInitialized = false;
	private ExecutorService mExecutor;
	private boolean mIsExecutorRunning = false;
	private boolean mIsPtpDeviceInitialized = false;

	private PtpDeviceInfo mDeviceInfo = null;
	private Hashtable<Integer, PtpProperty> mPtpProperties;
	private Hashtable<Integer, PtpStorageInfo> mPtpStorages;
	private boolean mIsLiveViewEnabled = false;
	private boolean mIsPtpObjectsLoaded = false;
	private ThreadBase mEventThread = null;
	// private LiveviewThread mLiveviewThread = null;
	private boolean mIsMovieRecordingStarted = false;
	private PtpLiveViewObject mLvo = null;
	private boolean mCaptureToSdram = false;

	// internal bracketing backup
	private Object mInternalBracketingShootingMode = null;
	private Object mInternalBracketingBurstNumber = null;
	
	// preferences private
	private boolean mLiveViewAtStart = false;
	private boolean mLiveViewReturn = false;
	private boolean mAFBeforeCapture = true;
	private boolean mGeneralScreenOn = true;
	private boolean mGeneralInternalViewer = true;
	// custom bracketing preferences
	private boolean mIsCustomBracketingEnabled = false;
    private int mBktStep = 1;
    private int mBktDirection = 2;
    private int mBktCount = 3;
    private boolean mBktFocusFirst = true;
    // sdram image saving preferences
	private String mSdramSavingLocation = new File(
			Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
			"DSLR").getAbsolutePath();
	private String mSdramPrefix = "dslr";
	private int mSdramPictureNumbering = 1;

	// time lapse preferences
	private long mTimelapseInterval = 10000; // interval in seconds
	private int mTimelapseIterations = 10;
	
	
	// preferences public
	public boolean getLiveViewAtStart() {
		return mLiveViewAtStart;
	}

	public boolean getLiveViewReturn() {
		return mLiveViewReturn;
	}

	public boolean getAFBeforeCapture() {
		return mAFBeforeCapture;
	}

	public void setAFBeforeCapture(boolean value) {
		if (value != mAFBeforeCapture) {
			mAFBeforeCapture = value;
			Editor editor = mPrefs.edit();
			editor.putBoolean(PREF_KEY_SHOOTING_AF, value);
			editor.commit();
		}
	}

	public boolean getGeneralScreenOn() {
		return mGeneralScreenOn;
	}
	public boolean getGeneralInternalViewer() {
		return mGeneralInternalViewer;
	}
	// custom bracketing public properties
	public boolean getIsCustomBracketingEnabled() {
		return mIsCustomBracketingEnabled;
	}
	public void setIsCustomBracketingEnabled(boolean value) {
		mIsCustomBracketingEnabled = value;
		mPrefs
		.edit()
		.putBoolean(PREF_KEY_BKT_ENABLED, mIsCustomBracketingEnabled)
		.commit();
	}
	public int getCustomBracketingCount() {
		return mBktCount;
	}
	public void setCustomBracketingCount(int value) {
		mBktCount = value;
		mPrefs
		.edit()
		.putString(PREF_KEY_BKT_COUNT, Integer.toString(mBktCount))
		.commit();
	}
	public int getCustomBracketingDirection() {
		return mBktDirection;
	}
	public void setCustomBracketingDirection(int value) {
		mBktDirection = value;
		mPrefs
		.edit()
		.putString(PREF_KEY_BKT_DIRECTION, Integer.toString(mBktDirection))
		.commit();
	}
	public int getCustomBracketingStep() {
		return mBktStep;
	}
	public void setCustomBracketingStep(int value) {
		mBktStep = value;
		mPrefs
			.edit()
			.putString(PREF_KEY_BKT_STEP, Integer.toString(mBktStep))
			.commit();
	}
	public boolean getCustomBracketingFocusFirst(){
		return mBktFocusFirst;
	}
	public void setCustomBracketingFocusFirst(boolean value) {
		mBktFocusFirst = value;
		mPrefs
		.edit()
		.putBoolean(PREF_KEY_BKT_FOCUS_FIRST, mBktFocusFirst)
		.commit();
	}
	// sdram image saving public preferences
	public void setSdramPictureNumbering(int value) {
		mSdramPictureNumbering = value;
		mPrefs
		.edit()
		.putString(PREF_KEY_SDRAM_NUMBERING, Integer.toString(mSdramPictureNumbering))
		.commit();
	}
	// timelapse public preferences
	public long getTimelapseInterval(){
		return mTimelapseInterval;
	}
	public void setTimelapseInterval(long value){
		mTimelapseInterval = value;
		mPrefs
			.edit()
			.putString(PREF_KEY_TIMELAPSE_INTERVAL, Long.toString(mTimelapseInterval))
			.commit();
	}
	public int getTimelapseIterations(){
		return mTimelapseIterations;
	}
	public void setTimelapseIterations(int value){
		mTimelapseIterations = value;
		mPrefs
			.edit()
			.putString(PREF_KEY_TIMELAPSE_ITERATIONS, Integer.toString(mTimelapseIterations))
			.commit();
	} 
	
	
	public PtpDevice(PtpService ptpService) {
		mPtpService = ptpService;
		mPrefs = mPtpService.getPreferences();
		loadPreferences();

		mDeviceInfo = new PtpDeviceInfo();
		mPtpProperties = new Hashtable<Integer, PtpProperty>();
		mPtpStorages = new Hashtable<Integer, PtpStorageInfo>();
	}

	private SharedPreferences.OnSharedPreferenceChangeListener mPrefChangedListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			Log.d(TAG, "Preference changed: " + key);
			loadPreferences();
			if (mPrefListener != null)
				mPrefListener.onSharedPreferenceChanged(sharedPreferences, key);
		}
	};

	private void loadPreferences() {
		Log.d(TAG, "Loading preferences");

		mLiveViewAtStart = mPrefs.getBoolean(PREF_KEY_LV_AT_START, false);
		mLiveViewReturn = mPrefs.getBoolean(PREF_KEY_LV_RETURN, false);
		mAFBeforeCapture = mPrefs.getBoolean(PREF_KEY_SHOOTING_AF, true);

		mGeneralScreenOn = mPrefs.getBoolean(PREF_KEY_GENERAL_SCREEN_ON, true);
		mGeneralInternalViewer = mPrefs.getBoolean(PREF_KEY_GENERAL_INTERNAL_VIEWER, true);

		String tmp = mPrefs.getString(PREF_KEY_SDRAM_LOCATION, "");
		if (tmp.equals("")) {
			Editor editor = mPrefs.edit();
			editor.putString(PREF_KEY_SDRAM_LOCATION, mSdramSavingLocation);
			editor.commit();
		} else
			mSdramSavingLocation = tmp;

		mSdramPrefix = mPrefs.getString(PREF_KEY_SDRAM_PREFIX, "dslr");
		mSdramPictureNumbering = Integer.valueOf(mPrefs.getString(
				PREF_KEY_SDRAM_NUMBERING, "1"));
		
		mIsCustomBracketingEnabled = mPrefs.getBoolean(PREF_KEY_BKT_ENABLED, false);
		mBktCount = Integer.valueOf(mPrefs.getString(PREF_KEY_BKT_COUNT, "3"));
		mBktDirection = Integer.valueOf(mPrefs.getString(PREF_KEY_BKT_DIRECTION, "2"));
		mBktStep = Integer.valueOf(mPrefs.getString(PREF_KEY_BKT_STEP, "1"));
		mBktFocusFirst = mPrefs.getBoolean(PREF_KEY_BKT_FOCUS_FIRST, true);

		mTimelapseInterval = Long.valueOf(mPrefs.getString(PREF_KEY_TIMELAPSE_INTERVAL, "10000"));
		mTimelapseIterations = Integer.valueOf(mPrefs.getString(PREF_KEY_TIMELAPSE_ITERATIONS, "10"));
		
		mFocusImages = Integer.valueOf(mPrefs.getString(PREF_KEY_FOCUS_IMAGES, "5"));
		mFocusStep = Integer.valueOf(mPrefs.getString(PREF_KEY_FOCUS_STEPS, "10"));
		mFocusDirectionDown = mPrefs.getBoolean(PREF_KEY_FOCUS_DIRECTION_DOWN, true);
		mFocusFocusFirst = mPrefs.getBoolean(PREF_KEY_FOCUS_FOCUS_FIRST, false);
		
		sendPtpDeviceEvent(PtpDeviceEvent.PrefsLoaded, null);

	}

	public void initialize(int vendorId, int productId,
			PtpCommunicatorBase communicator) {
		if (!mIsInitialized) {
			mExecutor = Executors.newSingleThreadExecutor();
			mIsExecutorRunning = true;
			mCommunicator = communicator;
			mIsInitialized = true;
			mVendorId = vendorId;
			mProductId = productId;
			mLvo = new PtpLiveViewObject(vendorId, productId);

			mPrefs.registerOnSharedPreferenceChangeListener(mPrefChangedListener);

			initializePtpDevice();
		}
	}

	public void stop(boolean isUsbUnpluged) {
		Log.d(TAG, "stop PTP device");
		if (!isUsbUnpluged)
			normalStop();
		else
			usbUnplugedStop();

		mPrefs.unregisterOnSharedPreferenceChangeListener(mPrefChangedListener);
	}

	private void normalStop() {
		// stop live view if running
		Log.i(TAG, "ending live view");
		endLiveViewDisplay(true);
		// return to camera mode
		Log.i(TAG, "switching to camera mode");
		changeCameraModeCmd(true);
		// return to sdcard mode
		Log.i(TAG, "switching to sdcard mode");
		setDevicePropValue(PtpProperty.RecordingMedia, 0x0000, true);

		Log.d(TAG, "stoping PTPDevice threads");
		usbUnplugedStop();
	}

	private void usbUnplugedStop() {
		stopTimelapseExecutor();
		
		mCommunicator.closeCommunicator();
		if (mIsExecutorRunning) {
			Log.d(TAG, "stoping communicator thread");
			stopExecutor(mExecutor);
			mIsExecutorRunning = false;
		}

		// if (mLiveviewThread != null)
		// mLiveviewThread.interrupt();

		if (mEventThread != null)
			mEventThread.interrupt();

		sendPtpDeviceEvent(PtpDeviceEvent.PtpDeviceStoped);

		mIsInitialized = false;
		mIsPtpDeviceInitialized = false;
		mIsLiveViewEnabled = false;
		mIsPtpObjectsLoaded = false;
		mPtpProperties.clear();
		mPtpStorages.clear();
	}

	public boolean getIsInitialized() {
		return mIsInitialized;
	}

	public boolean getIsPtpDeviceInitialized() {
		return mIsPtpDeviceInitialized;
	}

	public PtpService getPtpService() {
		return mPtpService;
	}

	public int getVendorId() {
		return mVendorId;
	}

	public int getProductId() {
		return mProductId;
	}

	public PtpDeviceInfo getPtpDeviceInfo() {
		return mDeviceInfo;
	}

	public boolean getIsLiveViewEnabled() {
		return mIsLiveViewEnabled;
	}

	public boolean getIsPtpObjectsLoaded() {
		return mIsPtpObjectsLoaded;
	}

	public Hashtable<Integer, PtpStorageInfo> getPtpStorages() {
		return mPtpStorages;
	}

	public synchronized PtpProperty getPtpProperty(final int propertyCode) {
		PtpProperty property = (PtpProperty) mPtpProperties.get(propertyCode);
		return property;
	}

	private void stopExecutor(ExecutorService executor) {
		if (executor != null) {
		Log.d(TAG, "Shuting down executor");
			executor.shutdown();
			try {
				// Wait a while for existing tasks to terminate
				if (!executor.awaitTermination(500, TimeUnit.MICROSECONDS)) {
					executor.shutdownNow(); // Cancel currently executing tasks
					// Wait a while for tasks to respond to being cancelled
					if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS))
						System.err.println("Pool did not terminate");
				}
			} catch (InterruptedException ie) {
				// (Re-)Cancel if current thread also interrupted
				executor.shutdownNow();
				// Preserve interrupt status
				Thread.currentThread().interrupt();
			}
		}
	}

	protected synchronized PtpCommand sendCommand(PtpCommand cmd) {
		try {

			if (cmd.getCommandCode() != PtpCommand.GetDeviceInfo
					&& cmd.getCommandCode() != PtpCommand.OpenSession) {
				if (!mDeviceInfo.supportsOperation(cmd.getCommandCode()))
					return null;
			}

			// Log.d(MainActivity.TAG, "Before get task");
			FutureTask<PtpCommand> fCmd = cmd.getTask(mCommunicator);
			// Log.d(MainActivity.TAG, "Before executing");
			mExecutor.execute(fCmd);
			// Log.d(MainActivity.TAG, "After executing");

			PtpCommand result = fCmd.get();

			return result;
		} catch (Exception e) {
			Log.e(TAG, "SendPtpCommand: ");// + e.getMessage());
			return null;
		}
	}

	private void initializePtpDevice() {
		new Thread(new Runnable() {

			public void run() {
				// get the device info
				PtpCommand cmd = sendCommand(getDeviceInfoCmd());
				if ((cmd == null) || (cmd != null && !cmd.isDataOk()))
					return;
				mDeviceInfo.parse(cmd.incomingData());
				// open the session
				cmd = sendCommand(openSessionCmd());
				if (cmd == null)
					return;
				if (cmd.isResponseOk()
						|| (cmd.hasResponse() && cmd.incomingResponse()
								.getPacketCode() == PtpResponse.SessionAlreadyOpen)) {
					// we have a session
					// load the supported properties
					for (int i = 0; i < mDeviceInfo.getPropertiesSupported().length; i++) {
						updateDevicePropDescCmd(mDeviceInfo
								.getPropertiesSupported()[i]);
					}

					loadVendorProperties();

					getStorageIds();

					mIsPtpDeviceInitialized = true;

					startEventListener();

					sendPtpDeviceEvent(PtpDeviceEvent.PtpDeviceInitialized);

					if (mIsLiveViewEnabled) {
						sendPtpDeviceEvent(PtpDeviceEvent.LiveviewStart);
					} else if (mLiveViewAtStart)
						enterLiveViewAtStart();
				}
			}
		}).start();

	}

	private void loadVendorProperties() {
		Log.i(TAG, "Load vendor properties");
		// try to get the vendor properties
		PtpCommand cmd = sendCommand(getVendorPropCodesCmd());
		// process the vendor properties
		if (cmd != null && cmd.isDataOk()) {
			cmd.incomingData().parse();
			int[] vendorProps = cmd.incomingData().nextU16Array();
			for (int i = 0; i < vendorProps.length; i++) {
				PtpCommand pCmd = sendCommand(getDevicePropDescCmd(vendorProps[i]));
				processLoadedProperty(pCmd);
			}
		}
	}

	private PtpCommand getDeviceInfoCmd() {
		return new PtpCommand(PtpCommand.GetDeviceInfo);
	}

	private PtpCommand openSessionCmd() {
		return new PtpCommand(PtpCommand.OpenSession).addParam(1);
	}

	private PtpCommand getDevicePropDescCmd(int propertyCode) {
		return new PtpCommand(PtpCommand.GetDevicePropDesc)
				.addParam(propertyCode);
	}

	private void updateDevicePropDescCmd(int propertyCode) {
		processLoadedProperty(sendCommand(getDevicePropDescCmd(propertyCode)));
	}

	private void processLoadedProperty(PtpCommand cmd) {
		if (cmd != null && cmd.isDataOk()) {
			cmd.incomingData().parse();

			PtpProperty property;
			int propCode = cmd.incomingData().nextU16();
			if (!mPtpProperties.containsKey(propCode)) {
				// Log.i(TAG, String.format("+++ Creating new property %#04x",
				// propCode));
				property = new PtpProperty();
				mPtpProperties.put(Integer.valueOf(propCode), property);
			} else {
				property = mPtpProperties.get(propCode);
				// Log.i(TAG,
				// String.format("+++ Property already in list %#04x",
				// propCode));
			}
			property.parse(cmd.incomingData());

			sendPtpPropertyChanged(property);

			switch (property.getPropertyCode()) {
			case PtpProperty.LiveViewStatus:
				mIsLiveViewEnabled = (Integer) property.getValue() == 1;
				break;
			}
		}
	}

	private PtpCommand getVendorPropCodesCmd() {
		return new PtpCommand(PtpCommand.GetVendorPropCodes);
	}

	private PtpCommand getEventCmd() {
		return new PtpCommand(PtpCommand.GetEvent);
	}

	private PtpCommand setDevicePropValueCommand(int propertyCode, Object value) {
		// Log.i(TAG, "setDevicePropValueCommand");
		PtpProperty property = mPtpProperties.get(propertyCode);
		if (property != null) {
			PtpBuffer buf = new PtpBuffer(
					new byte[mCommunicator.getWriteEpMaxPacketSize()]);
			PtpPropertyValue.setNewPropertyValue(buf, property.getDataType(),
					value);
			return new PtpCommand(PtpCommand.SetDevicePropValue).addParam(
					propertyCode).setCommandData(buf.getOfssetArray());
		} else
			return null;
	}

	protected void setDevicePropValue(int propertyCode, Object value,
			boolean updateProperty) {

		final PtpCommand cmd = setDevicePropValueCommand(propertyCode, value);
		if (cmd != null) {
			PtpCommand fCmd = sendCommand(cmd);
			if (fCmd != null && fCmd.isResponseOk()) {
				// Log.d(TAG, "setDevicePropValue finished");
				if (updateProperty)
					updateDevicePropDescCmd(propertyCode);
			}
		}
		// else
		// Log.i(TAG, "property not found");
	}

	public void setDevicePropValueCmd(final int propertyCode, final Object value) {
		setDevicePropValueCmd(propertyCode, value, true);
	}

	public void setDevicePropValueCmd(final int propertyCode,
			final Object value, final boolean updateProperty) {
		if (mIsPtpDeviceInitialized) {
			new Thread(new Runnable() {

				public void run() {

					setDevicePropValue(propertyCode, value, updateProperty);
				}
			}).start();
		}

	}

	protected PtpCommand getThumbCmd(int objectId) {
		return new PtpCommand(PtpCommand.GetThumb).addParam(objectId);
	}

	protected PtpCommand getObjectInfoCmd(int objectId) {
		return new PtpCommand(PtpCommand.GetObjectInfo).addParam(objectId);
	}

	private PtpCommand getStorageIdsCommand() {
		return new PtpCommand(PtpCommand.GetStorageIDs);
	}

	private PtpCommand getStorageInfoCommand(int storageId) {
		return new PtpCommand(PtpCommand.GetStorageInfo).addParam(storageId);
	}

	private PtpCommand getObjectHandlesCommand(int storageId) {
		return new PtpCommand(PtpCommand.GetObjectHandles).addParam(storageId);
	}

	private int mSlot2Mode = 0;
	private int mActiveSlot = 1;

	public int getSlot2Mode() {
		return mSlot2Mode;
	}

	public int getActiveSlot() {
		return mActiveSlot;
	}

	private void getStorageIds() {
		PtpCommand cmd = sendCommand(getStorageIdsCommand());
		if ((cmd == null) || (cmd != null && !cmd.isDataOk()))
			return;
		cmd.incomingData().parse();
		int count = cmd.incomingData().nextS32();
		Log.d(TAG, "Storage id count: " + count);
		PtpProperty prop = getPtpProperty(PtpProperty.Slot2ImageSaveMode);
		if (prop != null) {
			mSlot2Mode = (Integer) prop.getValue();
			Log.d(TAG, "Slot2 mode: " + mSlot2Mode);
		}
		prop = getPtpProperty(PtpProperty.ActiveSlot);
		if (prop != null) {
			mActiveSlot = (Integer) prop.getValue();
			Log.d(TAG, "Active slot: " + mActiveSlot);
		}

		for (int i = 1; i <= count; i++) {
			int storeId = cmd.incomingData().nextS32();
			int storeNo = storeId >> 16;
			Log.d(TAG, "StoreID: " + storeId);
			Log.d(TAG, "Getting info for slot: " + storeNo);
			if ((storeId & 1) == 1) {
				Log.d(TAG, "Slot has card");
				sendPtpDeviceEvent(PtpDeviceEvent.SdCardInserted, storeId >> 16);
				getStorageInfo(storeId);
				// getObjectHandles(storeId);
			} else {
				Log.d(TAG, "Slot is empty");
				 sendPtpDeviceEvent(PtpDeviceEvent.SdCardRemoved, storeId >> 16);
			}
		}

	}

	private void getStorageIdsCmd() {
		new Thread(new Runnable() {

			public void run() {
				getStorageIds();
			}
		}).start();
	}

	private void getStorageInfo(final int storageId) {
		Log.d(TAG, String.format("Get storage info: %#04x", storageId));
		PtpCommand cmd = sendCommand(getStorageInfoCommand(storageId));
		if ((cmd == null) || (cmd != null && !cmd.isDataOk()))
			return;
		PtpStorageInfo storage;
		if (mPtpStorages.containsKey(storageId)) {
			storage = mPtpStorages.get(storageId);
			storage.updateInfo(cmd.incomingData());
		} else {
			storage = new PtpStorageInfo(storageId, cmd.incomingData());
			mPtpStorages.put(storageId, storage);
		}
		sendPtpDeviceEvent(PtpDeviceEvent.SdCardInfoUpdated, storage);
	}

	private void getObjectHandles(final int storageId) {
		Log.d(TAG, String.format("load object handles: %#04x", storageId));
		PtpCommand cmd = sendCommand(getObjectHandlesCommand(storageId));
		if ((cmd == null) || (cmd != null && !cmd.isDataOk()))
			return;
		cmd.incomingData().parse();
		int count = cmd.incomingData().nextS32();
		for (int i = 1; i <= count; i++) {
			getObjectInfo(cmd.incomingData().nextS32());
		}
	}

	private PtpObjectInfo getObjectInfo(final int objectId) {
		Log.d(TAG, String.format("Get object info: %#04x", objectId));
		PtpObjectInfo obj = null;
		PtpCommand cmd = sendCommand(getObjectInfoCmd(objectId));
		if ((cmd == null) || (cmd != null && !cmd.isDataOk()))
			return null;
		cmd.incomingData().parse();
		int storageId = cmd.incomingData().nextS32();
		PtpStorageInfo storage = mPtpStorages.get(storageId);
		if (storage.objects.containsKey(objectId)) {
			obj = storage.objects.get(objectId);
			obj.parse(cmd.incomingData());
		} else {
			obj = new PtpObjectInfo(objectId, cmd.incomingData());
			storage.objects.put(objectId, obj);
		}
		loadObjectThumb(obj);
		// sendPtpServiceEvent(PtpServiceEventType.ObjectAdded, obj);
		return obj;
	}

	private void loadObjectThumb(PtpObjectInfo obj) {
		switch (obj.objectFormatCode) {
		case 0x3000:
		case 0x3801:
			File file = new File(mSdramSavingLocation + "/.dslrthumbs");
			if (!file.exists())
				file.mkdir();
			file = new File(file, obj.filename + ".jpg");
			if (!file.exists()) {

				PtpCommand cmd = sendCommand(getThumbCmd(obj.objectId));
				if ((cmd == null) || (cmd != null && !cmd.isDataOk()))
					return;
				obj.thumb = BitmapFactory.decodeByteArray(cmd.incomingData()
						.data(), 12, cmd.incomingData().data().length - 12);

				FileOutputStream fOut;
				try {
					fOut = new FileOutputStream(file);
					obj.thumb.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
					fOut.flush();
					fOut.close();
					obj.thumb.recycle();
				} catch (Exception e) {
				}

			}
			break;

		}
	}

	private PtpCommand getLargeThumbCommand(int objectId) {
		return new PtpCommand(PtpCommand.GetLargeThumb).addParam(objectId);
	}

	public byte[] getLargeThumb(int objectId) {
		PtpCommand cmd = sendCommand(getLargeThumbCommand(objectId));
		if (cmd != null && cmd.isResponseOk())
			return cmd.incomingData().data();
		else
			return null;
	}

	public void loadObjectInfos() {
		Log.d(TAG, "load object infos");
		if (!mIsPtpObjectsLoaded) {
			for (PtpStorageInfo info : mPtpStorages.values()) {
				getObjectHandles(info.storageId);
			}
			mIsPtpObjectsLoaded = true;
			// sendPtpServiceEvent(PtpServiceEventType.ObjectInfosLoaded, null);
		}
	}

	protected PtpCommand getPartialObjectCmd(PtpObjectInfo objectInfo,
			int maxSize, File file) {
		return getPartialObjectCmd(objectInfo, maxSize, file, null);
	}

	protected PtpCommand getPartialObjectCmd(
			PtpObjectInfo objectInfo,
			int maxSize,
			File file,
			PtpPartialObjectProccessor.PtpPartialObjectProgressListener progressListener) {
		PtpPartialObjectProccessor processor = new PtpPartialObjectProccessor(
				objectInfo, file);
		processor.setProgressListener(progressListener);
		return new PtpCommand(PtpCommand.GetPartialObject)
				.addParam(objectInfo.objectId)
				.addParam(0)
				.addParam(maxSize)
				.setFinalProcessor(processor);
	}


	public PtpCommand getPreviewImageCmd(
			PtpObjectInfo objectInfo,
			int maxSize,
			File file,
			PtpPartialObjectProccessor.PtpPartialObjectProgressListener progressListener) {
		return sendCommand(getPreviewImage(objectInfo, maxSize, file, progressListener));
	}
	
	private PtpCommand getPreviewImage(
			PtpObjectInfo objectInfo,
			int maxSize,
			File file,
			PtpPartialObjectProccessor.PtpPartialObjectProgressListener progressListener) {
		PtpGetPreviewImageProcessor processor = new PtpGetPreviewImageProcessor(objectInfo, file);
		processor.setProgressListener(progressListener);
		return new PtpCommand(PtpCommand.GetPreviewImage)
				.addParam(objectInfo.objectId)
				.addParam(1)
				.addParam(maxSize)
				.setFinalProcessor(processor);
	}
	
	private PtpCommand deleteObject(int objectId) {
		return new PtpCommand(PtpCommand.DeleteObject)
			.addParam(objectId);
	}
	
	public void deleteObjectCmd(ImageObjectHelper obj) {
		if (obj != null && obj.objectInfo != null) {
			PtpCommand cmd = sendCommand(deleteObject(obj.objectInfo.objectId));
			if (cmd != null && cmd.isResponseOk()) {
				PtpStorageInfo storage = mPtpStorages.get(obj.objectInfo.storageId);
				if (storage != null)
					storage.deleteObject(obj.objectInfo.objectId);
			}
				
		}
	}
	
	public void changeCameraMode() {
		if (mIsPtpDeviceInitialized) {
			PtpProperty property = getPtpProperty(PtpProperty.ExposureProgramMode);
			if (property != null)
				changeCameraMode(property.getIsWritable());
		}
	}

	private void changeCameraModeCmd(boolean cameraMode) {
		if (mIsPtpDeviceInitialized) {
			int mode = cameraMode ? 0 : 1;
			PtpCommand cmd = sendCommand(new PtpCommand(
					PtpCommand.ChangeCameraMode).addParam(mode));
			if (cmd != null && cmd.isResponseOk())
				updateDevicePropDescCmd(PtpProperty.ExposureProgramMode);
		}
	}

	public void changeCameraMode(final boolean cameraMode) {
		new Thread(new Runnable() {

			public void run() {
				changeCameraModeCmd(cameraMode);
			}
		}).start();
	}

	private boolean mNeedReturnToLiveview = false;

	private void checkReturnToLiveView() {
		Log.i(TAG, "Check return to live view: " + mNeedReturnToLiveview);
		if (mNeedReturnToLiveview) {
			Log.i(TAG, "Need return to live view");
			startLiveViewDisplay(true);
			mNeedReturnToLiveview = false;
		}
	}

	private void checkNeedReturnToLiveView(boolean captureToSdram) {
		mNeedReturnToLiveview = false;
		Log.i(TAG, "Check if we need to return to live view after capture");
		PtpProperty property = getPtpProperty(PtpProperty.LiveViewStatus);
		if (property != null) {
			mNeedReturnToLiveview = (Integer) property.getValue() != 0;
			Log.i(TAG, "Need to return to live view: " + mNeedReturnToLiveview);
			if (mNeedReturnToLiveview) {

				endLiveViewDisplay(true);

				// only return to live view if it is set in preferences
				mNeedReturnToLiveview = mLiveViewReturn;
			}
		}
	}

	public void changeAfAreaCmd(final int x, final int y) {
		new Thread(new Runnable() {

			public void run() {
				PtpCommand cmd = sendCommand(new PtpCommand(
						PtpCommand.ChangeAfArea).addParam(x).addParam(y));
				if (cmd != null && cmd.isDataOk()) {

				}
			}
		}).start();
	}

	public PtpLiveViewObject getLiveViewImage() {
		PtpCommand cmd = sendCommand(new PtpCommand(PtpCommand.GetLiveViewImage));
		if (cmd != null && cmd.isDataOk()) {
			mLvo.setBuffer(cmd.incomingData());
			return mLvo;
		}
		return null;
	}

	public void toggleInternalBracketing() {
		PtpProperty property = getPtpProperty(PtpProperty.EnableBracketing);
		if (property != null) {
			if ((Integer)property.getValue() == 0) {
				setDevicePropValueCmd(PtpProperty.EnableBracketing, 1);
				property = getPtpProperty(PtpProperty.StillCaptureMode);
				if (property != null) {
					mInternalBracketingShootingMode = property.getValue();
					setDevicePropValueCmd(PtpProperty.StillCaptureMode, 1);
				}
				property = getPtpProperty(PtpProperty.BurstNumber);
				if (property != null) {
					mInternalBracketingBurstNumber = property.getValue();
					setDevicePropValueCmd(PtpProperty.BurstNumber, 3);
				}
			}
			else {
				setDevicePropValueCmd(PtpProperty.EnableBracketing, 0);
				if (mInternalBracketingShootingMode != null) {
					setDevicePropValueCmd(PtpProperty.StillCaptureMode, mInternalBracketingShootingMode);
					mInternalBracketingShootingMode = null;
				}
				if (mInternalBracketingBurstNumber != null) {
					setDevicePropValueCmd(PtpProperty.BurstNumber, mInternalBracketingBurstNumber);
					mInternalBracketingBurstNumber = null;
				}
			}
		}
	}
	private int mOldAFMode = 0;
	private boolean mAfModeChanged = false;

	private PtpCommand initiateCaptureCommand(boolean captureToSdram) {
		return initiateCaptureCommand(captureToSdram, mAFBeforeCapture);
	}
	
	private PtpCommand initiateCaptureCommand(boolean captureToSdram, boolean performAf) {
		PtpCommand cmd = null;
		if (getIsCommandSupported(PtpCommand.InitiateCaptureRecInMedia)) {
			cmd = new PtpCommand(PtpCommand.InitiateCaptureRecInMedia);

			if (performAf)
				cmd.addParam(0xfffffffe); // perform AF before capture
			else
				cmd.addParam(0xffffffff); // no AF before capture

			if (captureToSdram)
				cmd.addParam(0x0001);
			else
				cmd.addParam(0x0000);
		} else {
			// check if need AF before capture
			cmd = new PtpCommand(PtpCommand.InitiateCapture);
			if (captureToSdram) {
				if (performAf) {
					if (getIsCommandSupported(PtpCommand.AfAndCaptureRecInSdram))
						cmd = new PtpCommand(PtpCommand.AfAndCaptureRecInSdram);
				} else
				if (getIsCommandSupported(PtpCommand.InitiateCaptureRecInSdram))
					cmd = new PtpCommand(PtpCommand.InitiateCaptureRecInSdram)
							.addParam(0xffffffff);
			}

			if (!performAf) {
				
				PtpProperty property = getPtpProperty(PtpProperty.AfModeSelect);
				if (property != null) {
					Integer oldAfMode = (Integer) property.getValue();
					if (oldAfMode != 4) {
						setDevicePropValue(PtpProperty.AfModeSelect, 4, true);
						mOldAFMode = oldAfMode;
						mAfModeChanged = true;
					}
				}
			}
		}
		return cmd;
	}

	public void initiateCaptureCmd() {
		initiateCaptureCmd(true);
	}

	private boolean mIsInCapture = false;
	
	private void initiateCapture(boolean checkNeedReturnToLV,
			boolean captureToSdram, boolean performAf) {
		
		if (checkNeedReturnToLV)
			checkNeedReturnToLiveView(captureToSdram);
		
		Log.d(TAG, "Initiate capture");
		sendPtpDeviceEvent(PtpDeviceEvent.CaptureInitiated, null);
		PtpCommand cmd = sendCommand(initiateCaptureCommand(captureToSdram, performAf));
		if (cmd != null && cmd.isResponseOk()) {
			boolean again = false;
			do {
				again = false;
				cmd = sendCommand(new PtpCommand(PtpCommand.DeviceReady));
				if (cmd != null) {
					switch (cmd.getResponseCode()) {
					case PtpResponse.OK:
						Log.i(TAG, "Initiate capture ok");
						break;
					case PtpResponse.DeviceBusy:
						Log.i(TAG, "Initiate capture busy");
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
						}
						again = true;
						break;
					default:
						Log.i(TAG,
								"Initiate capture error: "
										+ String.format("%04X",
												cmd.getResponseCode()));
						break;
					}
				}
			} while (again);
			mIsInCapture = true;
			sendPtpDeviceEvent(PtpDeviceEvent.CaptureStart, null);
			if (mAfModeChanged) {
				setDevicePropValue(PtpProperty.AfModeSelect, mOldAFMode, true);
				mAfModeChanged = false;
			}
		} else
			sendPtpDeviceEvent(PtpDeviceEvent.BusyEnd, null);
	}

	public void initiateCaptureCmd(final boolean checkNeedReturnToLV) {
		initiateCaptureCmd(checkNeedReturnToLV, mCaptureToSdram);
	}

	public void initiateCaptureCmd(final boolean checkNeedReturnToLV,
			final boolean captureToSdram) {
		sendPtpDeviceEvent(PtpDeviceEvent.BusyBegin, null);

		new Thread(new Runnable() {

			public void run() {
				
				if (mIsCustomBracketingEnabled)
					initCustomBracketing();
				else
					initiateCapture(checkNeedReturnToLV, captureToSdram, mAFBeforeCapture);
				
			}
		}).start();
	}

	private void captureComplete() { 
		if (mNeedBracketing)
			nextBracketingImage();
		if (mIsInFocusStacking)
			nextFocusStackingImage();
		
		if (!mNeedBracketing && !mTimelapseRunning && !mIsInFocusStacking) {
			sendPtpDeviceEvent(PtpDeviceEvent.BusyEnd, null);
			checkReturnToLiveView();
		}
	}

	public void startAfDriveCmd() {
		new Thread(new Runnable() {

			public void run() {
				sendCommand(new PtpCommand(PtpCommand.AfDrive));
			}
		}).start();
	}

	private void startEventListener() {
		if (!mCommunicator.getIsNetworkCommunicator()) {

			if (!mDeviceInfo.supportsOperation(PtpCommand.GetEvent)) {
				PtpUsbCommunicator communicator = (PtpUsbCommunicator) mCommunicator;
				if (communicator != null) {
					Log.d(TAG, "----- Using interrupt endpoint for events");
					mEventThread = new InterruptEventThread(
							communicator.getUsbDeviceConnection(),
							communicator.getInterrupEp());
					mEventThread.start();
				}
			} else {
				Log.d(TAG, "----- Using the GetEvent command for events");

				mEventThread = new EventThread();
				mEventThread.start();
			}
		}
	}

	private synchronized void processEvent(int eventCode, int eventParam) {
		// Log.d(MainActivity.TAG, "+*+*+*+* event code: " +
		// Integer.toHexString(eventCode) + " param: " +
		// Integer.toHexString(eventParam));
		switch (eventCode) {
		case PtpEvent.CancelTransaction:
			Log.d(TAG, "CancelTransaction: " + String.format("%#x", eventParam));
			break;
		case PtpEvent.ObjectAdded:
			Log.d(TAG, "ObjectAdded: " + String.format("%#x", eventParam));
			PtpObjectInfo info = getObjectInfo(eventParam);
			sendPtpDeviceEvent(PtpDeviceEvent.ObjectAdded, info);
			break;
		case PtpEvent.StoreAdded:
			Log.d(TAG, "StoreAdded: " + String.format("%#x", eventParam));
			getStorageInfo(eventParam);
			sendPtpDeviceEvent(PtpDeviceEvent.SdCardInserted, eventParam >> 16);
			break;
		case PtpEvent.StoreRemoved:
			Log.d(TAG, "StoreRemoved: " + String.format("%#x", eventParam));
			// remove the store
			mPtpStorages.remove(eventParam);
			sendPtpDeviceEvent(PtpDeviceEvent.SdCardRemoved, eventParam >> 16);
			break;
		case PtpEvent.DevicePropChanged:
			Log.d(TAG, "DevicePropChanged: " + String.format("%#x", eventParam));
			updateDevicePropDescCmd(eventParam);
			// getDevicePropDescCommand(eventParam);
			break;
		case PtpEvent.DeviceInfoChanged:
			Log.d(TAG, "DeviceInfoChanged: " + String.format("%#x", eventParam));
			break;
		case PtpEvent.RequestObjectTransfer:
			Log.d(TAG,
					"RequestObjectTransfer: "
							+ String.format("%#x", eventParam));
			break;
		case PtpEvent.StoreFull:
			Log.d(TAG, "StoreFull: " + String.format("%#x", eventParam));
			break;
		case PtpEvent.StorageInfoChanged:
			Log.d(TAG,
					"StorageInfoChanged: " + String.format("%#x", eventParam));
			getStorageInfo(eventParam);

			// execute when the capture was executed on camera
			// mIsInImageAqusition = false;
			// sendPtpServiceEvent(PtpServiceEventType.CaptureCompleteInSdcard,
			// null);
			break;
		case PtpEvent.CaptureComplete:
			Log.d(TAG, "CaptureComplete: " + String.format("%#x", eventParam));
			mIsInCapture = false;
			sendPtpDeviceEvent(PtpDeviceEvent.CaptureComplete, null);
			
			captureComplete();

			break;
		case PtpEvent.ObjectAddedInSdram:
			Log.d(TAG,
					"ObjectAddedInSdram: " + String.format("%#x", eventParam));
			sendPtpDeviceEvent(PtpDeviceEvent.ObjectAddedInSdram, null);
			// mIsInImageAqusition = true;
			// if (mEventThread != null)
			// mEventThread.setPauseEventListener(true);
			// sendPtpServiceEvent(PtpServiceEventType.GetObjectFromSdramStart,
			// null);
			if (mProductId == 0x0412)
				getPictureFromSdram(0xFFFF0001, true);
			else
				getPictureFromSdram(eventParam, true);

			break;
		case PtpEvent.CaptureCompleteRecInSdram:
			Log.d(TAG,
					"CaptureCompleteRecInSdram: "
							+ String.format("%#x", eventParam));
			mIsInCapture = false;
			sendPtpDeviceEvent(PtpDeviceEvent.CaptureCompleteInSdram, null);
			captureComplete();
			break;
		case PtpEvent.PreviewImageAdded:
			Log.d(TAG, "PreviewImageAdded: " + String.format("%#x", eventParam));
			
			break;
		}
	}

	private class EventThread extends ThreadBase {

		public EventThread() {
			mSleepTime = 100;
		}

		@Override
		public void codeToExecute() {
			// Log.d(TAG, "get DSLR events");
			PtpCommand cmd = sendCommand(getEventCmd());

			if (cmd != null) {
				if (cmd.isDataOk()) {
					cmd.incomingData().parse();
					int numEvents = cmd.incomingData().nextU16();
					int eventCode, eventParam;
					for (int i = 1; i <= numEvents; i++) {
						eventCode = cmd.incomingData().nextU16();
						eventParam = cmd.incomingData().nextS32();
						processEvent(eventCode, eventParam);
					}
				}
			}
			else
				this.interrupt();
		}
	}

	private class InterruptEventThread extends ThreadBase {
		private UsbDeviceConnection mUsbDeviceConnection = null;
		private UsbEndpoint mInterruptEp = null;

		public InterruptEventThread(UsbDeviceConnection usbDeviceConnection,
				UsbEndpoint interruptEp) {
			mUsbDeviceConnection = usbDeviceConnection;
			mInterruptEp = interruptEp;
			mSleepTime = 400;
		}

		private void processInterrupPacket(byte[] data)
				throws ExecutionException {
			PtpBuffer buf = new PtpBuffer(data);
			int eventCode = buf.getPacketCode();
			buf.parse();
			int eventParam = buf.nextS32();
			Log.d(TAG,
					"++=====++ New interrupt packet: " + buf.getPacketLength()
							+ " type: " + buf.getPacketType() + " code:"
							+ String.format("%#x", buf.getPacketCode()));
			processEvent(eventCode, eventParam);
		}

		@Override
		public void codeToExecute() {
			PtpBuffer buf = new PtpBuffer();
			byte[] tmpData = new byte[4096];
			byte[] data = null;
			boolean needMore = false;

			int counter = 0, size = 0, bytesRead = 0;
			try {
				if (needMore) {
					bytesRead = mUsbDeviceConnection.bulkTransfer(mInterruptEp,
							tmpData, tmpData.length, 200);
					if (bytesRead > 0) {
						Log.d(TAG, "bytes read: " + bytesRead);
						System.arraycopy(tmpData, 0, data, counter, bytesRead);
						counter += bytesRead;
						if (counter >= size) {
							needMore = false;
							processInterrupPacket(data);
						}
					}
				} else {
					bytesRead = mUsbDeviceConnection.bulkTransfer(mInterruptEp,
							tmpData, tmpData.length, 200);
					if (bytesRead > 0) {
						Log.d(TAG, "bytes read: " + bytesRead);
						buf.wrap(tmpData);
						size = buf.getPacketLength();
						Log.d(TAG, "packet size " + size);
						data = new byte[size];
						System.arraycopy(tmpData, 0, data, 0, bytesRead);
						if (buf.getPacketLength() > bytesRead) {
							needMore = true;
							counter = bytesRead;
						} else
							processInterrupPacket(data);
					}
				}
			} catch (ExecutionException e) {
				this.interrupt();
			}
		}
	}

	public void enterLiveViewAtStart() {
		new Thread(new Runnable() {

			public void run() {
				Log.i(TAG, "Entering live view at start");
				Log.i(TAG, "Setting recording media to sdram");
				setDevicePropValue(PtpProperty.RecordingMedia, 0x0001, true);

				Log.i(TAG, "Starting live view");
				startLiveViewDisplay(true);
			}
		}).start();
	}

	protected int startLiveView() {
		setDevicePropValue(PtpProperty.RecordingMedia, 0x0001, true);
		PtpCommand cmd = sendCommand(new PtpCommand(PtpCommand.StartLiveView));
		if (cmd != null) {
			boolean again = false;
			do {
				cmd = sendCommand(new PtpCommand(PtpCommand.DeviceReady));
				if (cmd != null) {
					switch (cmd.getResponseCode()) {
					case PtpResponse.OK:
						Log.i(TAG, "Live view start ok");
						return PtpResponse.OK;
					case PtpResponse.DeviceBusy:
						Log.i(TAG, "Live view start busy");
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
						}
						again = true;
						break;
					default:
						Log.i(TAG, "Live view start error");
						return 0;
					}
				}
			} while (again);
		} else
			Log.i(TAG, "Live view start error");
		return 0;
	}

	protected void startLiveViewDisplay(boolean notiftyUI) {
		switch (startLiveView()) {
		case PtpResponse.OK:
			if (notiftyUI)
				sendPtpDeviceEvent(PtpDeviceEvent.LiveviewStart);
			break;
		}
	}

	public void startLiveViewCmd() {
		if (mIsPtpDeviceInitialized) {
			new Thread(new Runnable() {

				public void run() {

					startLiveViewDisplay(true);
				}
			}).start();
		}
	}

	protected int endLiveView() {
		// it's a bit hard to stop so try it 3 times
		Log.d(TAG, "Stop live view");
		PtpCommand cmd = sendCommand(new PtpCommand(PtpCommand.EndLiveView));
		if (cmd != null) {
			Log.d(TAG, "Stop live view response " + cmd.isResponseOk());
			if (cmd.isResponseOk())
				return PtpResponse.OK;
			boolean again = false;
			do {
				Log.d(TAG, "Stop live view deviceready");
				cmd = sendCommand(new PtpCommand(PtpCommand.DeviceReady));
				if (cmd != null) {
					switch (cmd.getResponseCode()) {
					case PtpResponse.OK:
						Log.i(TAG, "Live view end ok");
						setDevicePropValue(PtpProperty.RecordingMedia, 0x0000, true);
						return PtpResponse.OK;
					case PtpResponse.DeviceBusy:
						Log.i(TAG, "Live view end busy");
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
						}
						again = true;
						break;
					default:
						Log.i(TAG, "Live view end error");
						return 0;
					}
				}
			} while (again);
		} else {
			Log.i(TAG, "Live view end error ");
		}
		return 0;
	}

	protected void endLiveViewDisplay(boolean notifyUI) {
		// if (mLiveviewThread != null)
		// mLiveviewThread.setIsLiveviewEnabled(false);

		switch (endLiveView()) {
		case PtpResponse.OK:
			if (notifyUI) {
				sendPtpDeviceEvent(PtpDeviceEvent.LiveviewStop);
				sendPtpDeviceEvent(PtpDeviceEvent.MovieRecordingEnd);
				mIsMovieRecordingStarted = false;
			}
			break;
		}
	}

	public void endLiveViewCmd(final boolean notifyUI) {
		if (mIsPtpDeviceInitialized) {
			new Thread(new Runnable() {

				public void run() {
					endLiveViewDisplay(notifyUI);
				}
			}).start();
		}
	}

	public void startMovieRecCmd() {
		if (!mIsMovieRecordingStarted) {
			PtpCommand cmd = sendCommand(new PtpCommand(
					PtpCommand.StartMovieRecInCard));
			if (cmd != null && cmd.isResponseOk()) {
				mIsMovieRecordingStarted = true;
				sendPtpDeviceEvent(PtpDeviceEvent.MovieRecordingStart, null);
			}
		}
	}

	public void stopMovieRecCmd() {
		PtpCommand cmd = sendCommand(new PtpCommand(PtpCommand.EndMovieRec));
		if (cmd != null && cmd.isResponseOk()) {
			mIsMovieRecordingStarted = false;
			sendPtpDeviceEvent(PtpDeviceEvent.MovieRecordingEnd, null);
		}

	}

	public File getObjectSaveFile(String objectName, boolean copyFileName) {
		File folder = new File(mSdramSavingLocation);
		if (!folder.exists()) {
			Log.d(TAG, "Make dir: " + folder.mkdir());
		}

		File f;
		if (!copyFileName) {
			int dotposition = objectName.lastIndexOf(".");
			String ext = objectName.substring(dotposition + 1, objectName.length());

			f = new File(folder, String.format("%s%04d.%s", mSdramPrefix,
					mSdramPictureNumbering, ext));

			setSdramPictureNumbering(mSdramPictureNumbering + 1);
		}
		else
			f = new File(folder,objectName);

		Log.d(TAG, "File name: " + f);

		return f;
	}

	private void runMediaScanner(File file) {
		Uri contentUri = Uri.fromFile(file);
		Intent mediaScanIntent = new Intent(
				"android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		mediaScanIntent.setData(contentUri);
		mPtpService.sendBroadcast(mediaScanIntent);
	}

	public void getObjectFromCamera(final ImageObjectHelper obj, PtpPartialObjectProccessor.PtpPartialObjectProgressListener listener) {
		
		PtpPartialObjectProccessor.PtpPartialObjectProgressListener mListener = new PtpPartialObjectProccessor.PtpPartialObjectProgressListener() {

			public void onProgress(int offset) {
				Log.d(TAG, "Progress");
				obj.progress = offset;

				sendPtpDeviceEvent(
						PtpDeviceEvent.GetObjectFromCameraProgress,
						obj);
			}
		};
		
		if (listener != null)
			mListener = listener;
		
		if (obj != null && obj.objectInfo != null) {
			sendPtpDeviceEvent(PtpDeviceEvent.GetObjectFromCamera, obj);

			obj.file = getObjectSaveFile(obj.objectInfo.filename, true);
			int maxSize = 0x200000;
			if (obj.objectInfo.objectCompressedSize < maxSize)
				maxSize = (int) (obj.objectInfo.objectCompressedSize / 2);

			PtpCommand cmd = sendCommand(getPartialObjectCmd(
					obj.objectInfo,
					maxSize,
					obj.file,
					mListener
					));

			if (cmd != null && cmd.isDataOk()) {
				runMediaScanner(obj.file);

				sendPtpDeviceEvent(PtpDeviceEvent.GetObjectFromCameraFinished,
						obj);
			}
		}
	}

	private void getPictureFromSdram(int objectId, boolean fromSdram) {
		if (mEventThread != null)
			mEventThread.setIsThreadPaused(true);
		try {
			final ImageObjectHelper helper = new ImageObjectHelper();
			helper.galleryItemType = ImageObjectHelper.PHONE_PICTURE;
			PtpCommand cmd = sendCommand(getObjectInfoCmd(objectId));
			if (cmd != null && cmd.isDataOk()) {
				helper.objectInfo = new PtpObjectInfo(objectId,
						cmd.incomingData());
				helper.file = getObjectSaveFile(helper.objectInfo.filename, false);

				sendPtpDeviceEvent(PtpDeviceEvent.GetObjectFromSdramInfo,
						helper);

				cmd = sendCommand(getThumbCmd(objectId));
				if (cmd != null && cmd.isDataOk()) {
					helper.saveThumb(cmd.incomingData(), fromSdram);

					sendPtpDeviceEvent(PtpDeviceEvent.GetObjectFromSdramThumb,
							helper);

					int maxSize = 0x200000;
					if (helper.objectInfo.objectCompressedSize < maxSize)
						maxSize = (int) (helper.objectInfo.objectCompressedSize / 2);

					cmd = sendCommand(getPartialObjectCmd(
							helper.objectInfo,
							maxSize,
							helper.file,
							new PtpPartialObjectProccessor.PtpPartialObjectProgressListener() {

								public void onProgress(int offset) {
									Log.d(TAG, "Progress");
									helper.progress = offset;

									sendPtpDeviceEvent(
											PtpDeviceEvent.GetObjectFromSdramProgress,
											helper);
								}
							}));

					if (cmd != null && cmd.isDataOk()) {
						// TODO : gps
						// if (mPreferencesHelper.mAddGpsLocation) {
						// addGpsLocation(helper.file);
						// }
						// else
						runMediaScanner(helper.file);

						sendPtpDeviceEvent(
								PtpDeviceEvent.GetObjectFromSdramFinished,
								helper);
					}
				}
			}
		} finally {
			if (mEventThread != null) {
				mEventThread.setIsThreadPaused(false);
			}
		}
	}

	private Object mCurrentEv = null;
	private Object[] mEvValues; 
	public int mBracketingCount = 0;
	public int mCurrentBracketing = 0; 
	public boolean mNeedBracketing = false; 
	
	public boolean getNeedBracketing() {
		return mNeedBracketing;
	}
	public int getBracketingCount() {
		return mBracketingCount;
	}
	public int getCurrentBracketing() {
		return mCurrentBracketing;
	}
	
	private void initCustomBracketing() {
		PtpProperty prop = getPtpProperty(PtpProperty.ExposureBiasCompensation);
//		PtpProperty afProp = getPtpProperty(PtpProperty.AfModeSelect);
		
//		if (afProp != null){
//			mAfValue = afProp.getValue();
//		}
//		else
//			mAfValue = null;
		mCurrentBracketing = 0;
		mBracketingCount = 0;
		
		if (prop != null){
			mCurrentEv = prop.getValue();
			
			mEvValues = new Object[mBktCount];
			
			final Vector<?> enums = prop.getEnumeration();
			int currentEv = enums.indexOf(prop.getValue());
			int evIndex = currentEv;
			int evCounter = 0;
			switch(mBktDirection) {
			case 0: // negative
				do {
					mEvValues[evCounter] = enums.get(evIndex);
					evCounter++;
					evIndex -= mBktStep;
					mBracketingCount++;
				} while (evIndex >= 0 && evCounter < mBktCount);
				break;
			case 1: // positive
				do {
					mEvValues[evCounter] = enums.get(evIndex);
					evCounter++;
					evIndex += mBktStep;
					mBracketingCount++;
				} while (evIndex < enums.size() && evCounter < mBktCount);
				break;
			case 2: // both
				mEvValues[evCounter] = enums.get(evIndex);
				mBracketingCount = 1;
				evCounter++;
				int counter = 1;
				do {
					evIndex = counter * mBktStep;
					if ((currentEv - evIndex) >= 0 && (currentEv + evIndex) <= enums.size()){
						mEvValues[evCounter] = enums.get(currentEv + evIndex);
						mEvValues[evCounter + 1] = enums.get(currentEv - evIndex);
						mBracketingCount += 2;
					}
					counter++;
					evCounter += 2;
				} while (evCounter < mBktCount);
				break;
			}
			mNeedBracketing = mBracketingCount > 0;
			
			// set focus to manual
//			mService.setDevicePropValue(PtpProperty.AfModeSelect, 0x0004, true);
			nextBracketingImage();
		} 		
	}
	
	private void nextBracketingImage() {
		Log.d(TAG, "Custom bracketing image: " + mCurrentBracketing);
		Log.d(TAG, "Custom bracketing focus first: " + mBktFocusFirst);
		if (mCurrentBracketing < mBracketingCount) {
			setDevicePropValue(PtpProperty.ExposureBiasCompensation, mEvValues[mCurrentBracketing], true);
			if (mCurrentBracketing == 0)
				initiateCapture(true, mCaptureToSdram, mBktFocusFirst);
			else
				initiateCapture(false, mCaptureToSdram, false);
			
			//mService.sendCommandNew(mService.getShootCommand(mToSdram));
			mCurrentBracketing++;
		} else {
			mNeedBracketing = false;
//			if (mAfValue != null)
//				mService.setDevicePropValue(PtpProperty.AfModeSelect, mAfValue, true);
			if (mCurrentEv != null)
				setDevicePropValue(PtpProperty.ExposureBiasCompensation, mCurrentEv, true);
		}
	}
	
	private boolean mTimelapseRunning = false;
	private int mTimelapseRemainingIterations = 10; 
	
	public boolean getIsTimelapseRunning(){
		return mTimelapseRunning;
	} 	
	
	public int getTimelapseRemainingIterations(){
		return mTimelapseRemainingIterations;
	}
	private Runnable timelapseTask = new Runnable() {
		
		public void run() {
			
			if (!mIsInCapture) {

				Log.d(TAG, "Timelapse image capture");
				if (mTimelapseRemainingIterations == mTimelapseIterations)
					initiateCaptureCmd(true);
				else
					initiateCaptureCmd(false);
			}
			mTimelapseRemainingIterations -= 1;
			sendPtpDeviceEvent(PtpDeviceEvent.TimelapseEvent, mTimelapseRemainingIterations);
			if (mTimelapseRemainingIterations == 0){
				stopTimelapseExecutor();
				return;
			}
			
			Log.d(TAG, "----- Timelapse event ----- iterations remain: " + mTimelapseRemainingIterations);
			
		}
	};
	ScheduledExecutorService mTimelapseScheduler; 
	
	public void startTimelapse(long interval, int frameCount){
		Log.d(TAG, "---- Timelapse started ----");
		mTimelapseRunning = true;
		
		setTimelapseIterations(frameCount);
		setTimelapseInterval(interval);
		
		mTimelapseRemainingIterations = frameCount;
		
		mTimelapseScheduler = Executors.newSingleThreadScheduledExecutor();
		mTimelapseScheduler.scheduleAtFixedRate(timelapseTask, 100, mTimelapseInterval, TimeUnit.MILLISECONDS);
		
		sendPtpDeviceEvent(PtpDeviceEvent.TimelapseStarted, null);
	}
	
	public void stopTimelapse() {
		stopTimelapseExecutor();
		if (!mIsInCapture && mNeedBracketing)
			captureComplete();
	}
	
	private void stopTimelapseExecutor(){
		
		Log.d(TAG, "---- Timelapse stoped ----");
	
			stopExecutor(mTimelapseScheduler);
		mTimelapseScheduler = null;
		mTimelapseRunning = false;
		
		sendPtpDeviceEvent(PtpDeviceEvent.TimelapseStoped, null);
		
	} 
	
	private int mFocusImages = 5;
	private int mFocusStep = 10;
	private boolean mFocusDirectionDown = true;
	private boolean mFocusFocusFirst = false;
	
	private boolean mIsInFocusStacking = false;
	private int mFocusStackingCurrentImage = 1;
	private boolean mStopFocusStacking = false;
	
	public int getFocusImages() {
		return mFocusImages;
	}
	public void setFocusImages(int value) {
		mFocusImages = value;
		mPrefs
			.edit()
			.putString(PREF_KEY_FOCUS_IMAGES, Integer.toString(mFocusImages))
			.commit();
	}
	public int getFocusStep(){
		return mFocusStep;
	}
	public void setFocusStep(int value) {
		mFocusStep = value;
		mPrefs
		.edit()
		.putString(PREF_KEY_FOCUS_STEPS, Integer.toString(mFocusStep))
		.commit();
	}
	public boolean getFocusDirectionDown(){
		return mFocusDirectionDown;
	}
	public void setFocusDirectionDown(boolean value){
		mFocusDirectionDown = value;
		mPrefs
		.edit()
		.putBoolean(PREF_KEY_FOCUS_DIRECTION_DOWN, mFocusDirectionDown)
		.commit();
	}
	public boolean getFocusFocusFirst(){
		return mFocusFocusFirst;
	}
	public void setFocusFocusFirst(boolean value) {
		mFocusFocusFirst = value;
		mPrefs
		.edit()
		.putBoolean(PREF_KEY_FOCUS_FOCUS_FIRST, mFocusFocusFirst)
		.commit();
	}
	public int getCurrentFocusStackingImage() {
		return mFocusStackingCurrentImage;
	}
	public boolean getIsInFocusStacking() {
		return mIsInFocusStacking;
	}
	public void stopFocusStacking() {
		mStopFocusStacking = true;
	}
	public boolean getStopFocusStacking() {
		return mStopFocusStacking;
	}
	
	public void startFocusStacking(int imagesToTake, int focusSteps, boolean directionDown, boolean focusFirst) {
		setFocusImages(imagesToTake);
		setFocusStep(focusSteps);
		setFocusDirectionDown(directionDown);
		setFocusFocusFirst(focusFirst);

		mStopFocusStacking = false;
		mIsInFocusStacking = true;
		mFocusStackingCurrentImage = 1;
		
		new Thread(new Runnable() {
			
			public void run() {
				sendPtpDeviceEvent(PtpDeviceEvent.BusyBegin, null);
				nextFocusStackingImage();
			}
		}).start();
		//nextFocusStackingImage();
	}
	
	private void nextFocusStackingImage() {
		Log.d(TAG, "Focus stacking image: " + mFocusStackingCurrentImage);
		if (mStopFocusStacking) {
			mIsInFocusStacking = false;
			mStopFocusStacking = false;
			captureComplete();
			return;
		}
		if (mFocusStackingCurrentImage == 1) {
			initiateCapture(true, mCaptureToSdram, mFocusFocusFirst);
			mFocusStackingCurrentImage++;
		}
		else {
			if (mFocusStackingCurrentImage <= mFocusImages) {
				// start live
				startLiveView();
				// get the afmode
				Object afMode = null;
				PtpProperty property = getPtpProperty(PtpProperty.AfModeSelect);
				if (property != null)
					afMode = property.getValue();
				// set afmode to af-s
				setDevicePropValue(PtpProperty.AfModeSelect, 0, true);
				// move the focus
				seekFocus(mFocusDirectionDown ? 1 : 2, mFocusStep);
				// set back the afmode
				setDevicePropValue(PtpProperty.AfModeSelect, afMode, true);
				// stop live view
				endLiveView();
				
				initiateCapture(false, mCaptureToSdram, false);
				mFocusStackingCurrentImage++;
				
			} else
				mIsInFocusStacking = false;
		}
		
	}
	
	public void seekFocusMin() {
		boolean repeat = true;
		while(repeat) {
			repeat = seekFocus(1, 32767) == PtpResponse.OK;
		}
	}

	public void seekFocusMax() {
		boolean repeat = true;
		while(repeat) {
			repeat = seekFocus(2, 32767) == PtpResponse.OK;
		}
	}
	
	public int seekFocus(int direction, int amount) {
		Log.i(TAG, "Seek focus direction: " + direction + " step: " + amount);
		boolean again = false;
		int retry = 0;
		int res = 0;
		do {
			res = driveFocusToEnd(direction, amount);
			switch(res) {
			case PtpResponse.OK:
				Log.i(TAG, "Seek focus OK");
				again = false;
				break;
			case PtpResponse.MfDriveStepEnd:
				retry += 1;
				Log.i(TAG, "Seek focus MfDriveStepEnd retry: " + retry);
				if (retry < 5)
					again = true;
				else
					again = false;
				break;
			case PtpResponse.MfDriveStepInsufficiency:
				Log.i(TAG, "Seek focus MfDriveStepInsufficiency retry: " + retry);
				if (retry > 0) {
					again = false;
				}
				else {
					retry += 1;
					again = true;
				}
				break;
			default:
				Log.i(TAG, "Seek focus other: " + res);
				again = false;
				break;
			}
			
		} while (again) ;
		
		return res;
	} 
	
	private int driveFocusToEnd(int direction, int step) {
		boolean again = true;
		PtpCommand cmd;
			cmd = sendCommand(getMfDriveCommand(direction, step));
			if (cmd != null) {
				switch(cmd.getResponseCode()) {
					case PtpResponse.OK:
						// do a deviceready command
						again = true;
						while(again) {
							cmd = sendCommand(new PtpCommand(PtpCommand.DeviceReady));
							if (cmd != null) {
								switch (cmd.getResponseCode()){
									case PtpResponse.DeviceBusy:
										// we do another deviceready
										// do some pause
										try {
											Thread.sleep(20);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
										break;
									default:
										// for all other cases assume there is an error
										return cmd.getResponseCode();
								}
							}
							else
								return 0;
						}
						break;
					default:
						return cmd.getResponseCode();
				}
			}
			else return 0;
		return 0;
	} 
	
	private PtpCommand getMfDriveCommand(int direction, int amount) {
		return new PtpCommand(PtpCommand.MfDrive)
			.addParam(direction)
			.addParam(amount);
	} 	
}
