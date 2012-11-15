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

import java.nio.channels.ClosedByInterruptException;

import android.util.Log;

public abstract class ThreadBase extends Thread {
	private boolean mIsThreadPaused = false;
	protected int mSleepTime = 10;
	
	public boolean getIsThreadPaused() {
		return mIsThreadPaused;
	}
	public void setIsThreadPaused(boolean value) {
		mIsThreadPaused = value;
	}
	
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(mSleepTime);
			} catch (InterruptedException e) {
				Log.i("ThreadBase", "Thread interrupted");
				break;
			} catch (Exception e) {
				Log.i("ThreadBase", "Thread exception " + e.getMessage());
				break;
			}
			if (!mIsThreadPaused) {
				codeToExecute();
			}
		}
		Log.i("ThreadBase", "Thread ended");
	}
	
	public abstract void codeToExecute();
}
