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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class AutoRepeatImageView extends ImageView {

	  private long initialRepeatDelay = 500;
	  private long repeatIntervalInMilliseconds = 100;

	  private Runnable repeatClickWhileButtonHeldRunnable = new Runnable() {
		  
	    public void run() {
	      //Perform the present repetition of the click action provided by the user
	      // in setOnClickListener().
	      performClick();

	      //Schedule the next repetitions of the click action, using a faster repeat
	      // interval than the initial repeat delay interval.
	      postDelayed(repeatClickWhileButtonHeldRunnable, repeatIntervalInMilliseconds);
	    }
	  };

	  private void commonConstructorCode() {
	    this.setOnTouchListener(new OnTouchListener() {
	    	
	      public boolean onTouch(View v, MotionEvent event) {
	                int action = event.getAction(); 
	                if(action == MotionEvent.ACTION_DOWN) 
	                {
	                  //Just to be sure that we removed all callbacks, 
	                  // which should have occurred in the ACTION_UP
	                  removeCallbacks(repeatClickWhileButtonHeldRunnable);

	                  //Perform the default click action.
	                  performClick();

	                  //Schedule the start of repetitions after a one half second delay.
	                  postDelayed(repeatClickWhileButtonHeldRunnable, initialRepeatDelay);
	                }
	                else if(action == MotionEvent.ACTION_UP) {
	                  //Cancel any repetition in progress.
	                  removeCallbacks(repeatClickWhileButtonHeldRunnable);
	                }

	                //Returning true here prevents performClick() from getting called 
	                // in the usual manner, which would be redundant, given that we are 
	                // already calling it above.
	                return true;
	      }

	    });
	  }
	  
	    public AutoRepeatImageView(Context context, AttributeSet attrs, int defStyle) {
	        super(context, attrs, defStyle);
	        commonConstructorCode();
	    }


	    public AutoRepeatImageView(Context context, AttributeSet attrs) {
	        super(context, attrs);
	        commonConstructorCode();
	    }

	  public AutoRepeatImageView(Context context) {
	    super(context);
	    commonConstructorCode();
	  }	  
}
