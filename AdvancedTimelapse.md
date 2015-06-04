# Advanced Timelapse with DslrDashboard #


In latest DslrDashboard version 0.30.11 I included a new way to execute Timelapse shooting.
To use the new Advanced Timelapse you first need to create a text file called timelpase.ddt.
You must put the file in your SDRAM image saving location folder (DCIM/DSLR by default).
The timelapse.ddt file format should be like this:

```
10
0;9000
1;0;F:20;AC:1:W:1
3;100;AC:2:S:test string
6;-100;F:-20;AC:3:F:21.1
```

explanation of timelapse.ddt lines:

  * **first line** in timelapse.ddt is **always the number of the frames** that will be captured - 10 in the example
  * all the following lines are key frame lines and they should be in format: **frame\_number**;**interval**;**command1**;**command2**;**commandn** For field value separation the **;** (semi colon) is used.


The **first key frame line is mandatory and should always point to Frame 0 and as interval it should have the desired interval in ms** (0;9000 in the example).

The interval part in the key frame lines that follow the first should contain only the interval change that will be used after the key frame is capture is finished. In example till frame 3 the interval is 9000ms, after frame 3 the interval is increased by 100ms and will be 9100ms till frame 6 where the interval is decreased by 100ms and the interval till the end of time lapse will be again 9000ms.

The commands that can be used with key frames for now are:
  * **F** - will move the camera focus. Positive value will move the focus towards infinity and negative value towards the closes point.
  * **AC** - will send a command to Arduino connected to your Android device. The format is: **arduino\_command**:**param\_type**:**param\_value**.
    * **arduino\_command** is a number (1-65535) that is used to implement different commands on Arduino
    * **param\_type** is the type of the parameter that is sent with the command. param\_type could be: B, W, L, F, S.
      * **B** - byte value 1 byte (0-255)
      * **W** - word value 2 bytes (0-65535) - 1;0;F:20;AC:1:W:1
      * **L** - long value 4 bytes (0 - 0xffffffff)
      * **F** - float value (sent as 4 bytes) - 6;-100;F:-20;AC:3:F:21.1
      * **S** - string value - 3;100;AC:2:S:test string
    * **param\_value** - should be according the param\_type

In future versions I will see to include more commands and also to add some sort of visual editor for the timelapse.ddt.

And here is a sample code for Arduino that will check if there is data on serial port and process it:

```
// check if we have a serial command packet
// sync bytes 0x55 0xaa
// packet length 2 bytes (packet_command + command_param)
// packet_command 2 bytes
// command_param if any
void checkSerial() {
	int tmp;
	
	if (Serial.available() > 0)
		lcd.clear();
	
	while (Serial.available() > 0) {
		tmp = Serial.read();
		if (tmp == 0x55) {
			tmp = Serial.peek();
			if (tmp == 0xaa) {
				// we have a sync, process the packet
				Serial.read();

				processPacket();
			}
		}
	}
}

void processPacket() {
	// check if we have at least 4 bytes (data length and command)
	if (Serial.available() >= 4) {
		uint16_t pLen = readWordFromSerial();
		uint16_t pCmd = readWordFromSerial();

		processDdCommand(pLen, pCmd);
	}
}

uint16_t readWordFromSerial() {
	if (Serial.available() >= 2) {
		int b1 = Serial.read();
		int b2 = Serial.read();
		return (uint16_t)word(b2, b1);
	}
	else
		return 0;
}

float readFloatFromSerial() {
	union u_tag
	{
		byte bytes[4] ;
		float buffer;
	} u;
	if (Serial.available() >= 4) {
		u.bytes[0] = Serial.read();
		u.bytes[1] = Serial.read();
		u.bytes[2] = Serial.read();
		u.bytes[3] = Serial.read();
		return u.buffer;
	}
	else
		return 0;
}
void processDdCommand(uint16_t dLen, uint16_t pCmd) {
	int i = 0;
	switch(pCmd) {
	case 0x0001:
		if (Serial.available() >=2) {
			lcd.setCursor(0, 1);
			uint16_t cParam = readWordFromSerial();

			lcd.print("W ");
			lcd.print(cParam);
		}
		break;
	case 0x0002:
		// string test
		lcd.setCursor(0,0);
		while (i < (dLen-2)) {
			int ch = Serial.read();
			lcd.print((char)ch);
			i++;
		}
		break;
	case 0x0003:
		lcd.setCursor(8,1);
		lcd.print("F ");
		lcd.print(readFloatFromSerial());
		break;
	}
```