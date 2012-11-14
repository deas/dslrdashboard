package com.dslr.dashboard;

import java.util.HashMap;
import java.util.Iterator;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class PtpService extends ServiceBase {

	public interface OnPtpServiceEventListener {
		public void ServiceEvent(PtpServiceEvent event);
	}
	private OnPtpServiceEventListener mOnPtpServiceEventListener = null;
	public void setOnPtpServiceEventListener(OnPtpServiceEventListener listener) {
		mOnPtpServiceEventListener = listener;
	}
	private static final String TAG = "PtpService";
	
	private void sendPtpServiceEvent(PtpServiceEvent event) {
		if (mOnPtpServiceEventListener != null && mIsBind) {
			mOnPtpServiceEventListener.ServiceEvent(event);
		}
	}
	
	private UsbManager mUsbManager;
	private UsbDevice mUsbDevice;
	private UsbDeviceConnection mUsbConnection = null;
	private UsbInterface mUsbIntf = null;
	private UsbEndpoint mUsbWriteEp = null;
	private UsbEndpoint mUsbReadEp = null;
	private UsbEndpoint mUsbInterruptEp = null;
	
	private boolean mIsUsbDevicePresent = false;
	private boolean mIsUsbDeviceInitialized = false;
	private boolean mIsUsbInterfaceClaimed = false;
	
	private PtpDevice mPtpDevice = null;
	
	// preferences
	private SharedPreferences mPrefs = null;
	
	
	public boolean getIsUsbDevicePresent() {
		return mIsUsbDevicePresent;
	}
	public boolean getIsUsbDeviceInitialized() {
		return mIsUsbDeviceInitialized;
	}
	public PtpDevice getPtpDevice() {
		return mPtpDevice;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		cancelNotification();
		return super.onBind(intent);
	}
	
	@Override
	public void onRebind(Intent intent) {
		Log.d(TAG, "onRebind");
		super.onRebind(intent);
	}
	
	private static final String ACTION_USB_PERMISSION = "com.dslr.dashboard.USB_PERMISSION";
	PendingIntent mPermissionIntent;
	
	private final BroadcastReceiver mUsbPermissionReceiver = new BroadcastReceiver() {

	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        Log.d(TAG, "Intent received " + action);
	        if (ACTION_USB_PERMISSION.equals(action)) {
	            synchronized (this) {
	                UsbDevice usbDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

	                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) { 
	                	Log.d(TAG, "USB device permission granted");
	                		mUsbDevice = usbDevice;
	                		mIsUsbDevicePresent = true;
	                		initUsbConnection();
	                }
	            }
	        }
	    }
	};
	
	private final BroadcastReceiver mUsbDeviceDetached = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice usbDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				
				
				if (mUsbDevice == usbDevice) {
					Log.d(PtpService.TAG, "DSLR USB device detached");
					usbDeviceDetached();
				}
			}
		}
	};


	private void usbDeviceDetached()
	{
		Log.d(TAG, "Usb device removed, stoping the service");
		sendPtpServiceEvent(PtpServiceEvent.UsbDeviceRemoved);
		closeUsbConnection(true);
		mIsUsbDevicePresent = false;
		
		if (!mIsBind)
			stopSelf();
	}
	public void closeUsbConnection() {
		closeUsbConnection(false);
	}
	public void closeUsbConnection(boolean isUsbUnpluged){
		Log.d(TAG, "closeUsbConnection");

		mPtpDevice.stop(isUsbUnpluged);
		
		if (mUsbConnection != null) {
			if (mIsUsbInterfaceClaimed) {
				Log.d(TAG, "Releasing USB interface");
				mUsbConnection.releaseInterface(mUsbIntf);
			}
			Log.d(TAG, "USB connection present, closing");
			mUsbConnection.close();
		}
		mIsUsbDevicePresent = false;
		mUsbIntf = null;
		mUsbReadEp = null;
		mUsbWriteEp = null;
		mUsbInterruptEp = null;
		mUsbConnection = null;
		mUsbDevice = null;
		mIsUsbInterfaceClaimed = false;
		mIsUsbDeviceInitialized = false;
		sendPtpServiceEvent(PtpServiceEvent.UsbDeviceRemoved);
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		super.onCreate();

		
        IntentFilter usbDetachedFilter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbDeviceDetached, usbDetachedFilter);
		
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbPermissionReceiver, filter);
        

        // get the shared preferences
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		// create the ptpdevice
        mPtpDevice = new PtpDevice(this);
		
		try {
			mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
		}
		catch(Exception e) {
			Log.d(TAG, "UsbManager not available: " + e.getMessage());
		}
		
	}

	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		
		
		return START_STICKY; //super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");

    	unregisterReceiver(mUsbDeviceDetached);
		unregisterReceiver(mUsbPermissionReceiver);

		super.onDestroy();
	}
	
	@Override
	public void onLowMemory() {
		Log.d(TAG, "onLowMemory");
		super.onLowMemory();
	}
	
	public void stopPtpService(boolean keepAliveIfUsbConnected){
		Log.d(TAG, "stopPtpService");
		cancelNotification();
		if (mIsUsbDeviceInitialized){
			if (!keepAliveIfUsbConnected) {
				closeUsbConnection();
				stopSelf();
			}
			else {
				createNotification();
			}
		}
		else {
			stopSelf();
		}
	}
	
	private static int mId = 0x0001;
	
	private void cancelNotification() {
		NotificationManager mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(mId);
	}
	
	private void createNotification() {
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.dslrlauncher)
		        .setContentTitle("DslrDashboard")
		        .setContentText("Running in background");
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(mId, mBuilder.build());	}
	
	public void searchForUsbCamera() {
		boolean deviceFound = false;
		try
		{
			if (mUsbDevice == null) {
					Log.d(TAG, "Ptp service usb device not initialized, search for one");
			
					if (mUsbManager != null)
					{
						HashMap<String, UsbDevice> devices = mUsbManager.getDeviceList();
						Log.d(TAG, "Found USB devices count: " + devices.size());
						Iterator<UsbDevice> iterator = devices.values().iterator();
						while(iterator.hasNext())
						{
							UsbDevice usbDevice = iterator.next();
							Log.d(TAG, "USB Device: " + usbDevice.getDeviceName() + " Product ID: " + usbDevice.getProductId() + " Vendor ID: " + usbDevice.getVendorId() + " Interface count: " + usbDevice.getInterfaceCount());
							for(int i = 0; i < usbDevice.getInterfaceCount(); i++){
								UsbInterface intf = usbDevice.getInterface(i);
								Log.d(TAG, "Interface class: " + intf.getInterfaceClass() ); 
								if (intf.getInterfaceClass() == android.hardware.usb.UsbConstants.USB_CLASS_STILL_IMAGE)
								{
									//mUsbDevice = usbDevice;
									Log.d(TAG, "Ptp Service imaging usb device found requesting permission");
									mUsbManager.requestPermission(usbDevice, mPermissionIntent);
									deviceFound = true;
									break;
								}
							}
							if (deviceFound)
								break;
						}	 
					}
					else
						Log.d(TAG, "USB Manager is unavailable");
				
			}
			else {
				Log.d(TAG, "Ptp service usb imaging device already present");
				//_usbManager.requestPermission(_usbDevice, mPermissionIntent);
			}
		} catch (Exception e) {
			Log.d(TAG, "PtpService search for USB camrea exception: " + e.getMessage());
		}
		
	}
	
	private void initUsbConnection()
	{
		
		try {
			if (mUsbDevice != null)	{
				if (mUsbIntf == null) {
					for(int i = 0; i < mUsbDevice.getInterfaceCount(); i++)	{
						UsbInterface uintf = mUsbDevice.getInterface(i);
						if (uintf.getInterfaceClass() == UsbConstants.USB_CLASS_STILL_IMAGE){
							// we have a still image interface
							// Log.d(MainActivity.TAG, "Imaging USB interface found");
							mUsbIntf = uintf;
							break;
						}
					}
					if (mUsbIntf != null) {
						// get the endpoints
						for(int i =0; i< mUsbIntf.getEndpointCount(); i++) {
							UsbEndpoint ep = mUsbIntf.getEndpoint(i);
							if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
								if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
									mUsbWriteEp = ep;
									// Log.d(MainActivity.TAG, "write endpoint found");
								}
							}
							else {
								switch(ep.getType()) {
									case UsbConstants.USB_ENDPOINT_XFER_BULK:
										mUsbReadEp = ep;
										// Log.d(MainActivity.TAG, "read endpoint found");
										break;
									case UsbConstants.USB_ENDPOINT_XFER_INT:
										mUsbInterruptEp = ep;
										// Log.d(MainActivity.TAG, "interrupt endpoint found");
										break;
								}
							}
						}
					}
					else 
						Log.d(TAG, "No compatible USB interface found");
				}
				else
					Log.d(TAG, "USB interface found");
				// if we have read and write endpoints then we good to go
				if (mUsbReadEp != null && mUsbWriteEp != null) {
					if (!mIsUsbDeviceInitialized) {
						mUsbConnection = mUsbManager.openDevice(mUsbDevice);
						mIsUsbDeviceInitialized = mUsbConnection != null;
					}
					if (mIsUsbDeviceInitialized) {
						// Log.d(TAG, "USB Device Initialized");
						if (!mIsUsbInterfaceClaimed) {
							mIsUsbInterfaceClaimed = mUsbConnection.claimInterface(mUsbIntf, true);
							// Log.d(TAG, "USB Interface claimed: " + isInterfaceClaimed);
						}
						sendPtpServiceEvent(PtpServiceEvent.UsbDeviceInitialized);
						// create the USB communicator
						PtpUsbCommunicator communicator = new PtpUsbCommunicator(new PtpSession());
						// initialize the USB communicator
						communicator.initCommunicator(mUsbConnection, mUsbWriteEp, mUsbReadEp, mUsbInterruptEp);
						// initialize the PTP device
						mPtpDevice.initialize(mUsbDevice.getVendorId(), mUsbDevice.getProductId(),  communicator);
					}
				}
			
			}
			else
				Log.d(TAG, "No USB device present");
			
		} catch (Exception e){
			Log.d(TAG, "InitUsb exception: " + e.getMessage());
		}
	}
	
	// preferences properties
	public SharedPreferences getPreferences() {
		return mPrefs;
	}
	

    
}
