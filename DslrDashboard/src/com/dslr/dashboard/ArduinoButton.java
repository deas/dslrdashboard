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
