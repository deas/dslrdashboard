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
