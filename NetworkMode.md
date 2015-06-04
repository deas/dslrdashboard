# DslrDashboard Network mode #

If you have 2 Android devices with Network mode you can connect your Dslr camera connected to Android device to another Android device over WIFI.

To enable this, on one Android device in menu select 'Start Network server'. This device will act as server and will await incoming connections. After you enabled the server DslrDashboard will display you the IP and port (4757 by default) where the server is listening for incoming connections.

Connect your Dslr camera with USB to second Android device. After the Dslr camera is recognised by DslrDashboard in menu you can select the 'Start Network client' option. After that you will get a dialog where you need to enter the IP from the server (the IP that was displayed when you started the server). After a short time the client Android device will switch of the screen and the server Android will take full control of your Dslr camera.

To return control of camera to client Android device in menu select 'Stop Network client', that will close the network connection to server and return the control over camera to client Android.

To stop the Network server select 'Stop Network server' on your Android device that was acting as server device.

You can connect more clients to the server and switch between them. The server can also have a camera connected via USB.

This version requires Internet and WIFI permission to work. The Android devices need to be on same network.


**The protocol between the client and server**

Client awaits server commands, executes them and returns the result to server.

The packet contains header and data

Packet header is always
```
byte(0x55)
byte(0xaa)
dword(packet length) - the packet length includes the header length to
```

Packet data in case of server packet (from server to client)
```
PTP command packet (12 bytes + 4 * num of params)
PTP command data packet (if the command has command data)
```

Packet data in case of client packet (from client to server)
```
PTP response packet (12 bytes + 4 * num of response params)
PTP response data packet (if there was a response data packet)
```

After the client connects to server the server first sends a PTP command with 0x0001 code.
```
0x55 
0xaa 
0x00000012 - total packet size
0x0000000c - ptp packet size
0x0001     - ptp container type (command)
0x0001     - ptp command code
0x00000000 - session id
```

the client should answer with a response containing the USB Vendor Id and Product Id.
Sample client response in case of Nikon D5000
```
0x55
0xaa
0x0000001a - total packet size
0x00000014 - ptp packet size
0x0003     - ptp container type (response)
0x2001     - ptp response code (OK)
0x00000000 - session Id
0x000004b0 - Vendor Id (Nikon)
0x00000423 - Product Id (D5000)
```

In future I will try to implement a WIFI Arduino client when the post delivers my packet (Dslr camera connected to Arduino with USB and Arduino connected with WIFI to DslrDashboard server).

Demo of DslrDashboard connected to Carambola:
<a href='http://www.youtube.com/watch?feature=player_embedded&v=J_sfvSelj-Q' target='_blank'><img src='http://img.youtube.com/vi/J_sfvSelj-Q/0.jpg' width='425' height=344 /></a>