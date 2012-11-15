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

import android.hardware.usb.*;
import android.util.Log;

public class PtpUsbCommunicator extends PtpCommunicatorBase {
	private static String TAG = "PtpCommunicator";
	
	private final PtpSession mSession;
	private UsbDeviceConnection mUsbDeviceConnection;
	private UsbEndpoint mWriteEp;
	private UsbEndpoint mReadEp;
	private UsbEndpoint mInterrupEp;
	private boolean mIsInitialized = false;
	
	public PtpUsbCommunicator(PtpSession session){ 
		mSession = session;
	}

	public void initCommunicator(
			UsbDeviceConnection connection,
			UsbEndpoint writeEp,
			UsbEndpoint readEp,
			UsbEndpoint interrupEp) {
		mUsbDeviceConnection = connection;
		mWriteEp = writeEp;
		mReadEp = readEp;
		mInterrupEp = interrupEp;
		mIsInitialized = true;
	}
	public void closeCommunicator() {
		mIsInitialized = false;
	}
	public UsbDeviceConnection getUsbDeviceConnection() {
		return mUsbDeviceConnection;
	}
	public UsbEndpoint getInterrupEp() {
		return mInterrupEp;
	}
	public int getWriteEpMaxPacketSize() {
		return mWriteEp.getMaxPacketSize();
	}
	public boolean getIsInitialized(){
		return mIsInitialized;
	}
	
	protected synchronized void processCommand(PtpCommand cmd) throws Exception{
		if (mIsInitialized)
		{
			boolean needAnotherRun = false;
			do {
				//Log.d(TAG, "+++ Sending command to device");
				int bytesCount = 0;
				int retry = 0;
				byte[] data;
				synchronized (mSession) {
					data = cmd.getCommandPacket(mSession.getNextSessionID());
				}
				while(true) {
					bytesCount = mUsbDeviceConnection.bulkTransfer(mWriteEp, data, data.length , 200);
					if (bytesCount != data.length) {
						Log.d(TAG, "+++ Command packet sent, bytes: " + bytesCount);
						retry += 1;
						if (retry > 2)
							throw new Exception("writen length != packet length");
					}
					else
						break;
				}
				if (cmd.getHasSendData()){
					data = cmd.getCommandDataPacket();
					bytesCount = mUsbDeviceConnection.bulkTransfer(mWriteEp, data, data.length, 200);
					//Log.d(TAG, "+++ Command data packet sent, bytes: " + bytesCount);
					if (bytesCount != data.length)
						throw new Exception("writen length != packet length");
					// give the device a bit time to process the data
					Thread.sleep(100);
				}
				data = new byte[mReadEp.getMaxPacketSize()];
				while (true){
					bytesCount = mUsbDeviceConnection.bulkTransfer(mReadEp, data, data.length, 200);
	//				if (bytesCount < 1)
	//					Log.d(TAG, "+++ Packet received, bytes: " + bytesCount);
					if (bytesCount > 0)
					{
						if (!cmd.newPacket(data, bytesCount)){
							//data = null;
							if (cmd.hasResponse()) {
								needAnotherRun = cmd.weFinished();
								break;
							}
						}
					}
				}
	//			if (needAnotherRun)
	//				Thread.sleep(300);
			} while (needAnotherRun);
		}
	}

	@Override
	public boolean getIsNetworkCommunicator() {
		return false;
	} 
}
