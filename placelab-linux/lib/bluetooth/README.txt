Blue Cove
---------

Blue Cove is an open source implementation of the JSR-82 Bluetooth API for Java, targetted at desktop (J2SE) platforms.

At present only a subset of the API is implemented. 

We current support the Microsoft Bluetooth stack for Windows XP Service Pack 2. 

To get started, place intelbth.dll in your library path, and BlueCove.jar in your classpath. You will then be able to compile and run the following test applications.

ServerTest
----------

Creates and advertises a Bluetooth service with a specified name. When a device connects to this service, reads a UTF-8 string, prints it on the screen, and terminates.

ClientTest
----------

Connects to every nearby instance of ServerTest, and writes a specified UTF-8 string to each.

See LICENSE for licensing information. Comments, queries and suggestions to bluecove-devel@lists.sourceforge.net.