package com.dslr.dashboard;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

public class UsbSerialService extends ServiceBase {

	private final static String TAG = UsbSerialService.class.getSimpleName();

	@Override
	public void onCreate() {

		Log.d(TAG, "onCreate");

		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);

		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		registerReceiver(mUsbPermissionReceiver, filter);

		try {
			mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		} catch (Exception e) {
			Log.d(TAG, "UsbManager not available: " + e.getMessage());
		}

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");

		searchForSerialUsb();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		stopIoManager();
		unregisterReceiver(mUsbPermissionReceiver);
		super.onDestroy();
	}

	public interface ButtonStateChangeListener {
		void onButtonStateChanged(int buttons);
	}

	private ButtonStateChangeListener mButtonStateListener = null;

	public void setButtonStateChangeListener(ButtonStateChangeListener listener) {
		mButtonStateListener = listener;
	}

	private final ExecutorService mExecutor = Executors
			.newSingleThreadExecutor();

	private UsbSerialDriver mSerialDevice;
	private UsbManager mUsbManager;
	private UsbDevice mUsbSerialDevice;
	PendingIntent mPermissionIntent;
	private SerialInputOutputManager mSerialIoManager;

	private static final String ACTION_USB_PERMISSION = "com.dslr.dashboard.USB_SERIAL_PERMISSION";

	private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() {

		public void onRunError(Exception e) {
			Log.d(TAG, "Runner stopped.");
		}

		public void onNewData(final byte[] data) {
			updateReceivedData(data);
		}
	};

	private void startUsbSerialDevice() {
		Log.d(TAG, "Starting USB Serial device");
		mSerialDevice = UsbSerialProber.acquire(mUsbManager);
		Log.d(TAG, "Resumed, mSerialDevice=" + mSerialDevice);
		if (mSerialDevice == null) {
			Log.d(TAG, "No serial device.");
		} else {
			try {
				mSerialDevice.open();
			} catch (IOException e) {
				Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
				try {
					mSerialDevice.close();
				} catch (IOException e2) {
					// Ignore.
				}
				mSerialDevice = null;
				return;
			}
			Log.d(TAG, "Serial device: " + mSerialDevice);
		}
		onDeviceStateChange();
	}

	private void stopIoManager() {
		if (mSerialIoManager != null) {
			Log.i(TAG, "Stopping io manager ..");
			mSerialIoManager.stop();
			mSerialIoManager = null;
			mUsbSerialDevice = null;
		}
	}

	private void startIoManager() {
		if (mSerialDevice != null) {
			Log.i(TAG, "Starting io manager ..");
			mSerialIoManager = new SerialInputOutputManager(mSerialDevice,
					mListener);
			mExecutor.submit(mSerialIoManager);

		}
	}

	private void onDeviceStateChange() {
		stopIoManager();
		startIoManager();
	}

	private boolean mNeedMoreBytes = false;
	private byte[] mData = new byte[1024];
	private int mOffset = 0;
	private boolean mSyncFound = false;
	private int mSyncPos = 0;

	private void updateReceivedData(byte[] data) {
		// Log.d(TAG, "Incomind size: " + data.length);
		System.arraycopy(data, 0, mData, mOffset, data.length);
		mOffset += data.length;

		if (!mSyncFound) {
			int i = 0;
			do {
				if ((mData[i] & 0xff) == 0xaa) {
					//Log.d(TAG, "First sync found 0xaa");
					if ((i + 1) < mOffset) {
						if ((mData[i+1] & 0xff) == 0x55) {
							//Log.d(TAG, "Second sync found 0x55");
							mSyncFound = true;
							mSyncPos = i+2;
							mNeedMoreBytes = false;
							break;
						}
					} else {
						//Log.d(TAG, "Need more bytes for second sync");
						mNeedMoreBytes = true;
					}
				}
				i++;
			} while (i < mOffset);
		}
		if (mSyncFound) {
			
			int length = 0;
			if (mNeedMoreBytes) {
				//Log.d(TAG, "Sync found need more byte");
				length = (0xff & mData[mSyncPos + 1] << 8) + 0xff & mData[mSyncPos];
				// Log.d(TAG, "Need size: " + length + " Offset: " + mOffset);
				if ((mSyncPos + length) == mOffset) {
					// Log.d(TAG, String.format("got all data: %d, %d",length,
					// mOffset) );
					mNeedMoreBytes = false;
					mOffset = 0;
				}

			} else {
				//Log.d(TAG, "Sync found first check for bytes");
				//Log.d(TAG, "Packet syncpos: " + mSyncPos + " offset: " + mOffset);
				if ((mSyncPos + 1) < mOffset) {
				//if (data.length > 1) {
					length = (0xff & mData[mSyncPos + 1] << 8) + 0xff & mData[mSyncPos];
					//Log.d(TAG, "Packet length: " + length + " syncpos: " + mSyncPos + " offset: " + mOffset);
					//length = (0xff & data[1] << 8) + 0xff & data[0];
					if ((mSyncPos + length) > mOffset) {
					//if (length != mOffset) {
						mNeedMoreBytes = true;
					} else {
						// Log.d(TAG,
						// String.format("got right size: %d, %d",length,
						// mOffset) );
						mOffset = 0;
					}
				} else {
					mNeedMoreBytes = true;
				}
			}
		}
		 if (!mNeedMoreBytes && mSyncFound) {
			 int command = ((0xff & mData[mSyncPos + 3]) << 8) + (0xff & mData[mSyncPos +2]);
			 switch (command) {
			 	case 0x0001:
				 int buttons = ((0xff & mData[mSyncPos + 5]) << 8) + (0xff & mData[mSyncPos +4]);
				 mSyncFound = false;
				 mSyncPos = 0;
				 //Log.d(TAG, "Serial button change detected");
				 if (mButtonStateListener != null)
					 mButtonStateListener.onButtonStateChanged(buttons);
				break;

			}
		 }
	}

	public void searchForSerialUsb() {
		if (mUsbSerialDevice == null) {
			Log.d(TAG, "Ptp Serial USB device not initialized, search for one");

			if (mUsbManager != null) {
				HashMap<String, UsbDevice> devices = mUsbManager
						.getDeviceList();
				Log.d(TAG, "Found USB devices count: " + devices.size());
				Iterator<UsbDevice> iterator = devices.values().iterator();
				while (iterator.hasNext()) {
					UsbDevice usbDevice = iterator.next();
					Log.d(TAG,
							"USB Device: " + usbDevice.getDeviceName()
									+ " Product ID: "
									+ usbDevice.getProductId() + " Vendor ID: "
									+ usbDevice.getVendorId()
									+ " Interface count: "
									+ usbDevice.getInterfaceCount());
					if (usbDevice.getVendorId() == 0x2341) {// check if arduino
						Log.d(TAG, "Found Arduino");
						mUsbManager.requestPermission(usbDevice,
								mPermissionIntent);
						break;
					}
				}
			} else
				Log.d(TAG, "USB Manager is unavailable");

		}
	}

	private final BroadcastReceiver mUsbPermissionReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "USB Serial permission Intent received " + action);
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice usbDevice = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						Log.d(TAG, "USB device permission granted");
						// if arduino, then start USB serial communication
						if (usbDevice.getVendorId() == 0x2341) {
							mUsbSerialDevice = usbDevice;
							startUsbSerialDevice();
						}
					}
				}
			}
		}
	};

}
