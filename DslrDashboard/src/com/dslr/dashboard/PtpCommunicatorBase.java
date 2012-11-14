// Copyright 2012 by Zoltan Hubai <hubaiz@gmail.com>
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version as long the source code includes
// this copyright notice.
//  
package com.dslr.dashboard;

public abstract class PtpCommunicatorBase {
	protected abstract void processCommand(PtpCommand cmd) throws Exception;
	public abstract boolean getIsNetworkCommunicator();
	public abstract int getWriteEpMaxPacketSize();
	public abstract void closeCommunicator();
}
