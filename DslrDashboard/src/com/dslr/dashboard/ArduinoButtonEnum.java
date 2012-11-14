package com.dslr.dashboard;

import android.view.ViewConfiguration;

public enum ArduinoButtonEnum {
    Button0 (0x0001),
    Button1 (0x0002),
    Button2 (0x0004),
    Button3 (0x0008),
    Button4 (0x0010),
    Button5 (0x0020),
    Button6 (0x0040),
    Button7 (0x0080),
    Button8 (0x0100),
    Button9 (0x0200);
    
    private int mButtonPosition;
    private long mLongPressStart;
    private long mLongPressEnd;
    
    private boolean mIsPressed = false;
    private boolean mIsLongPress = false;

    public void pressStart(long value) {
    	mIsLongPress = false;
    	mIsPressed = true;
    	mLongPressStart = value;
    }
    public void pressEnd(long value) {
    	mLongPressEnd = value;
    	mIsPressed = false;
    	mIsLongPress = ((System.currentTimeMillis() - mLongPressStart) > ViewConfiguration.getLongPressTimeout());
    }
    
    public int getButtonPosition() {
        return mButtonPosition;
    }
    ArduinoButtonEnum(int buttonPosition) {
        mButtonPosition = buttonPosition;
        mLongPressStart = System.currentTimeMillis();
    }
    
    public boolean getIsPressed() {
    	return mIsPressed;
    }
    public boolean getIsLongPress() {
    	if (!mIsPressed)
    		return mIsLongPress;
    	else
    		return false;
    }
    public long getLongPressStart(){
    	return mLongPressStart;
    }
    public long getLongPressEnd(){
    	return mLongPressEnd;
    }
}
