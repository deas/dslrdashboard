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

import java.util.EnumSet;

import android.util.Log;
import android.view.ViewConfiguration;

public class ArduinoButton {

    private static final String TAG = "ArduinoButton";
    
    private int mButtonState = 0xffff;
    
    private ArduinoButtonListener mButtonChangeListener = null;
    
    public void setArduinoButtonListener(ArduinoButtonListener listener) {
    	mButtonChangeListener = listener;
    }
    
    public interface ArduinoButtonListener {
    	void buttonStateChanged(EnumSet<ArduinoButtonEnum> pressedButtons, EnumSet<ArduinoButtonEnum> releasedButtons);
    }
    
    public ArduinoButton()
    {
    }
    
    public void newButtonState(int buttonState) {
        int oldStates = mButtonState;
        EnumSet<ArduinoButtonEnum> pressedButtons = EnumSet.noneOf(ArduinoButtonEnum.class);
        EnumSet<ArduinoButtonEnum> releasedButtons = EnumSet.noneOf(ArduinoButtonEnum.class);
        mButtonState = buttonState;
        for (ArduinoButtonEnum button : ArduinoButtonEnum.values()) {
            if ((oldStates & button.getButtonPosition()) != (mButtonState & button.getButtonPosition())) {
            	boolean pressed = (mButtonState & button.getButtonPosition()) == 0;
            	boolean longPress = false;
            	if (pressed) {
            		button.pressStart(System.currentTimeMillis());
                	pressedButtons.add(button);
            	}
            	else {
            		button.pressEnd(System.currentTimeMillis());
                	releasedButtons.add(button);
            	}
                //Log.d(TAG, "Status change " + button.toString() + " KeyDown " + pressed);
            }
        }
        if (mButtonChangeListener != null)
        	mButtonChangeListener.buttonStateChanged(pressedButtons, releasedButtons);
    }
    
    public boolean getIsButtonPressed(ArduinoButtonEnum button) {
        return (mButtonState & button.getButtonPosition()) != 0;
    }
}
