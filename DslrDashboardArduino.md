# Interfacing Arduino with Android and DslrDashboard #

A simple wiring that will allow buttons connected to Arduino control DslrDashboard:

![http://wiki.dslrdashboard.googlecode.com/git/images/dslrdashboard_buttons.png](http://wiki.dslrdashboard.googlecode.com/git/images/dslrdashboard_buttons.png)


And the Arduino sketch:

```
/**
* Uses internal pullups to read pushbutton states,
* Comunicate the state of the buttons using serial interface
*/

#define IN1 22  
#define IN2 24  
#define IN3 26  
#define IN4 28 
#define IN5 30 
#define IN6 32
#define IN7 34
#define IN8 36
#define IN9 38
#define IN0 40

const int buttons[] = { IN1, IN2, IN3, IN4, IN5, IN6, IN7, IN8, IN9, IN0 };

int states[] = {HIGH, HIGH, HIGH, HIGH, HIGH, HIGH, HIGH, HIGH, HIGH, HIGH };


void setup() {
  Serial.begin(115200);
  
  for (int i = 0; i < 10; i++) 
  {
	  pinMode(buttons[i], INPUT);
	  digitalWrite(buttons[i], HIGH);
  }
}

void loop() {
  delay(10); // debounces switches
  
  int values[10];

  for (int i = 0; i < 10; i++ )
	  values[i] = digitalRead(buttons[i]);

  boolean change = false;

  for (int i = 0; i < 10; i++) 
  {
	  if (states[i] != values[i]) 
	  {
		  states[i] = values[i];
		  change = true;
	  }
  }

  if (change)
  {
	  unsigned int state = 0;
	  for (int i =0; i < 10; i++)
	  {
		  state |= values[i] << i;
	  }
	  uint8_t buf[] = { 0xaa, 0x55, 0x06, 0x00, 0x01, 0x00, state & 0xff, (state >> 8) & 0xff };
	  Serial.write(buf, 8);
	  Serial.flush();
  }
}
```

I only had 10 buttons when I was doing this but actually you could connect up to 16 with this sketch and some modification.
The 0xaa and 0x55 are for syncing.

0x06 0x00 is the packet size.

0x01 0x00 is the command for buttons (here it can be expanded for sensors, rotary encoder, potentiometer, etc).


Last 2 bytes are the button states (one bit represents a button state - pressed/released).

You can use the USB port on Arduino for serial communication or you can use a serial-usb shield.


Here an image of the complete wiring and connection over USB HUB.

![http://wiki.dslrdashboard.googlecode.com/git/images/arduino_dslrdashboard.jpg](http://wiki.dslrdashboard.googlecode.com/git/images/arduino_dslrdashboard.jpg)