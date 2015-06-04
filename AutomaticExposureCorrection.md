# Introduction #

The automatic exposure correction will change the camera shutter speed and ISO according the camera current EV reading while capturing time lapse sequences (Nikon only).


# Details #

To bring up the EV correction dialog in LRTimelapse help screen press the EV correction button

![http://wiki.dslrdashboard.googlecode.com/git/images/ev_correction_select.jpg](http://wiki.dslrdashboard.googlecode.com/git/images/ev_correction_select.jpg)


![http://wiki.dslrdashboard.googlecode.com/git/images/ev_correction.jpg](http://wiki.dslrdashboard.googlecode.com/git/images/ev_correction.jpg)

Description of the Exposure correction dialog
  1. With this check box you can enable or disable the automatic Exposure correction
  1. Number of frames before the application checks if the camera reported exposure value is reached the defined EV difference set under 6.
  1. Number of safety checks before the Exposure correction is applied - if you use the default 3 frames for exposure check and 3 frames for safety check the Exposure correction will be applied if the change remains for 9 frames
  1. The current exposure reading from the camera
  1. If checked the current exposure reading is used as 'value for comparison' else 0 is used - the application will try to keep the camera at this exposure value
  1. The EV difference that will trigger the exposure correction - if the difference between the current reading and the 'value for comparison' equals or is greater then this value the Exposure correction will be started
  1. The minimum and maximum for the shutter speed - if minimum is reached the ISO will be increased, maximum is used in over exposure correction
  1. The EV increase/decrease for ISO if the minimum or maximum shutter speed is reached
  1. Exposure correction direction - you can choose in what direction should the application monitor the exposure value change reported by camera
  1. The camera current shutter speed
  1. The camera current ISO
  1. Test button to check the settings in live - don't use if you already started a time-lapse as when enabled it will change the camera shutter speed and ISO values.

How it works:
  1. After you start the automatic Exposure correction the application will check after every frame if the number of frames for check (2) has been reached.
  1. If the number of frames for check (2) is reached the application checks if the difference between 'value for comparison' (5) and the camera reported exposure value is equal or greater then the EV difference value (6).
  1. If the EV difference value (6) is reached the application checks if the safety frame number (3) is reached, if yes the application will apply the exposure correction (step 5.), if not then it will increase the safety frame counter and go to step 1.
  1. If the EV difference value (6) is not reached the application will reset the safety frame counter to 1 and continue at step 1.
  1. The application will apply the exposure correction:
    * for under exposure: it will decrease the shutter speed by the EV difference (6) value. If shutter speed minimum (7) is reached then it will increase the camera ISO and shutter speed by the ISO EV (8) value until shutter speed is higher then shutter speed minimum (7).
    * for over exposure: it will always try to reach the camera minimum ISO and after that it will increase the shutter speed.
