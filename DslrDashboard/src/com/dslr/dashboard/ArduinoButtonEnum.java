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
