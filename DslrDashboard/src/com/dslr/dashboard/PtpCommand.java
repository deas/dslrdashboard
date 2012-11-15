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

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import android.util.Log;

public class PtpCommand implements Callable<PtpCommand> {

	private static String TAG = "PtpCommand";
	
	public interface OnCommandFinishedListener {
		public void onCommandFinished(PtpCommand command);
	}
	
	private OnCommandFinishedListener _cmdFinishedListener = null;
	
	private int mCommandCode;
	protected ArrayList<Integer> mParams;
	private int mSessionId = 0;
	private boolean mHasSessionId = false;
	private PtpBuffer mBuffer;
	private byte[] mCommandData = null;
	private boolean mHasSendData = false;
	private PtpCommunicatorBase mCommunicator;
	private IPtpCommandFinalProcessor mFinalProcessor = null;
	private String mNotificationMsg;
	
	public int getCommandCode(){
		return mCommandCode;
	}
	public IPtpCommandFinalProcessor getFinalProcessor(){
		return mFinalProcessor;
	}
	public ArrayList<Integer> getParams(){
		return mParams;
	}
	public int getSssionId(){
		return mSessionId;
	}
	public boolean getHasSessionId(){
		return mHasSessionId;
	}
	public boolean getHasSendData(){
		return mHasSendData;
	}
	public PtpCommunicatorBase getCommunicator(){
		return mCommunicator;
	}
	public String getNotificatinMsg(){
		return mNotificationMsg;
	}
	
	public PtpCommand addParam(int param) {
		mParams.add(param);
		return this;
	}
	public PtpCommand setCommandData(byte[] commandData){
		mCommandData = commandData;
		mHasSendData = true;
		return this;
	}
	public PtpCommand setOnCommandFinishedListener(OnCommandFinishedListener listener){
		_cmdFinishedListener = listener;
		return this;
	}
	public PtpCommand setFinalProcessor(IPtpCommandFinalProcessor processor){
		mFinalProcessor = processor;
		return this;
	}
	public FutureTask<PtpCommand> getTask(PtpCommunicatorBase communicator){
		mCommunicator = communicator;
		return new FutureTask<PtpCommand>(this);
	}
	
	public PtpCommand(int commandCode){
		mCommandCode = commandCode;
		mParams = new ArrayList<Integer>();
		mBuffer = new PtpBuffer();
	}
	
	public PtpCommand(String notification){
		mNotificationMsg = notification;
		mCommandCode = 0;
	}
	public PtpCommand call() throws Exception {
		//Log.d(TAG, "Before sending command: " + _communicator);
		try
		{
		mCommunicator.processCommand(this);
		}
		catch (Exception e)
		{
			Log.d(TAG, e.getMessage());
			throw e; 
		}
		if (_cmdFinishedListener != null)
			_cmdFinishedListener.onCommandFinished(this);
		//Log.d(TAG, String.format("Command finished: %#04x", _commandCode));
		return this;
	}

	
	protected byte[] getCommandPacket(int sessionId){
		mSessionId = sessionId;
		mHasSessionId = true;
		int bufLen = 12 + (4 * mParams.size());
		byte[] data = new byte[bufLen];
		mBuffer.wrap(data);
		mBuffer.put32(bufLen); // size of the packet
		mBuffer.put16(1); // this is a command packet
		mBuffer.put16(mCommandCode); // command code
		mBuffer.put32(mSessionId); // session id
		for(int i = 0; i < mParams.size(); i++){
			mBuffer.put32(mParams.get(i));
		}
		return data;
	}
	
	protected byte[] getCommandDataPacket(){
		if (mHasSessionId && mHasSendData){
			mBuffer.wrap(new byte[12 + mCommandData.length]);
			mBuffer.put32(mBuffer.length()); // size will be later set;
			mBuffer.put16(2); // this is a data packet
			mBuffer.put16(mCommandCode); // the command code
			mBuffer.put32(mSessionId); // session id
			// copy the data byte[] to the packet
			System.arraycopy(mCommandData, 0, mBuffer.data(), 12, mCommandData.length);
			return mBuffer.data();
		}
		else 
			return null;
	}
	

	private boolean mHasData = false;
	private boolean mHasResponse = false;
	private PtpBuffer mIncomingData = null;
	private PtpBuffer mIncomingResponse = null;
	
	private boolean _needMoreBytes = false;
	private int _bytesCount = 0;
	private int _packetLen = 0;

	private byte[] _tmpData = null;
	
	public boolean hasData(){
		return mHasData;
	}
	public boolean hasResponse(){
		return mHasResponse;
	}
	public boolean isResponseOk(){
		return mHasResponse ? mIncomingResponse.getPacketCode() == PtpResponse.OK : false;
	}
	public int getResponseCode() {
		return mHasResponse ? mIncomingResponse.getPacketCode() : 0;
	}
	public boolean isDataOk(){
		return mHasData ? isResponseOk() : false;
	}
	public PtpBuffer incomingData(){
		return mIncomingData;
	}
	public PtpBuffer incomingResponse(){
		return mIncomingResponse;
	}
	public int responseParam(){
		if (mHasResponse){
			mIncomingResponse.parse();
			return mIncomingResponse.nextS32();
		}
		return 0;
	}
	
	protected boolean newPacket(byte[] packet, int size){
		if (_needMoreBytes){
			System.arraycopy(packet, 0, _tmpData, _bytesCount, size);
			_bytesCount += size;
			if (_bytesCount >= _packetLen){
				_needMoreBytes = false;
				processPacket();
				return false;
			}
			else
				return true;
		}
		else {
			mBuffer.wrap(packet);
			_packetLen = mBuffer.getPacketLength();
			
			
			int packetType = mBuffer.getPacketType();
			switch(packetType){
				case 2: // data
					mIncomingData = new PtpBuffer(new byte[_packetLen]);
					_tmpData = mIncomingData.data();
					break;
				case 3: // response
					mIncomingResponse = new PtpBuffer(new byte[_packetLen]);
					_tmpData = mIncomingResponse.data();
					break;
			}
			System.arraycopy(packet, 0, _tmpData, 0, size);
			
			if (_packetLen > size) {// we need more bytes
				_needMoreBytes = true;
				_bytesCount = size;
				return true;
			}
			else {
				processPacket();
				return false;
			}
		}
	}
	
	protected void processPacket(){
		mBuffer.wrap(_tmpData);
		switch (mBuffer.getPacketType()) {
		case 2:
			//Log.d(TAG, "--- Incoming data packet");
			mHasData = true;
			break;
		case 3:
			//Log.d(TAG, "--- Incoming response packet");
			mHasResponse = true;
			//Log.d(TAG, "--- Response code " + Integer.toHexString(_incomingResponse.getPacketCode()));
			break;
		default:
			break;
		}
	}

	private void reset(){
		mHasSessionId = false;
		mHasData = false;
		mHasResponse = false;
		_needMoreBytes = false;
		mIncomingData = null;
		mIncomingResponse = null;
		_tmpData = null;
	}
	
	public boolean weFinished()
	{
		boolean result = doFinalProcessing();
		if (!result){
			// we finished
		}
		else {
			// we need another run, reset evrything
			reset();
		}
		return result;
	}
	protected boolean doFinalProcessing(){
		return mFinalProcessor == null ? false : mFinalProcessor.doFinalProcessing(this);
	}
	
	
    public static final int GetDeviceInfo               = 0x1001;
    public static final int OpenSession                 = 0x1002;
    public static final int CloseSession                = 0x1003;
    public static final int GetStorageIDs               = 0x1004;
    public static final int GetStorageInfo              = 0x1005;
    public static final int GetNumObjects               = 0x1006;
    public static final int GetObjectHandles            = 0x1007;
    public static final int GetObjectInfo               = 0x1008;
    public static final int GetObject                   = 0x1009;
    public static final int GetThumb                    = 0x100a;
    public static final int DeleteObject                = 0x100b;
    public static final int SendObjectInfo              = 0x100c;
    public static final int SendObject                  = 0x100d;
    public static final int InitiateCapture             = 0x100e;
    public static final int FormatStore                 = 0x100f;
    public static final int ResetDevice                 = 0x1010;
    public static final int SelfTest                    = 0x1011;
    public static final int SetObjectProtection         = 0x1012;
    public static final int PowerDown                   = 0x1013;
    public static final int GetDevicePropDesc           = 0x1014;
    public static final int GetDevicePropValue          = 0x1015;
    public static final int SetDevicePropValue          = 0x1016;
    public static final int ResetDevicePropValue        = 0x1017;
    public static final int TerminateOpenCapture        = 0x1018;
    public static final int MoveObject                  = 0x1019;
    public static final int CopyObject                  = 0x101a;
    public static final int GetPartialObject            = 0x101b;
    public static final int InitiateOpenCapture         = 0x101c;
    
    public static final int InitiateCaptureRecInSdram	= 0x90c0;
    public static final int AfDrive						= 0x90c1;
    public static final int ChangeCameraMode			= 0x90c2;
    public static final int DeleteImagesInSdram			= 0x90c3;
    public static final int GetLargeThumb				= 0x90c4;
    public static final int GetEvent					= 0x90c7;
    public static final int DeviceReady					= 0x90c8;
    public static final int SetPreWbData				= 0x90c9;
    public static final int GetVendorPropCodes			= 0x90ca;
    public static final int AfAndCaptureRecInSdram		= 0x90cb;
    public static final int GetPicCtrlData				= 0x90cc;
    public static final int SetPicCtrlData				= 0x90cd;
    public static final int DeleteCustomPicCtrl			= 0x90ce;
    public static final int GetPicCtrlCapability		= 0x90cf;
    public static final int GetPreviewImage				= 0x9200;
    public static final int StartLiveView				= 0x9201;
    public static final int EndLiveView					= 0x9202;
    public static final int GetLiveViewImage			= 0x9203;
    public static final int MfDrive						= 0x9204;
    public static final int ChangeAfArea				= 0x9205;
    public static final int AfDriveCancel				= 0x9206;
    public static final int InitiateCaptureRecInMedia   = 0x9207;
    public static final int StartMovieRecInCard		    = 0x920a;
    public static final int EndMovieRec                 = 0x920b;
    
    public static final int MtpGetObjectPropsSupported  = 0x9801;
    public static final int MtpGetObjectPropDesc        = 0x9802;
    public static final int MtpGetObjectPropValue       = 0x9803;
    public static final int MtpSetObjectPropValue       = 0x9804;
    public static final int MtpGetObjPropList           = 0x9805;
	
} 