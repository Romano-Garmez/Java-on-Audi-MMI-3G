
package org.placelab.demo.virtualgps;

import org.placelab.core.PlacelabProperties;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

public class J9SerialPort extends IOPort {
	
	private InputStream in;
	private OutputStream out;
	
	public J9SerialPort () {

		String device = PlacelabProperties.get("placelab.gps_device");
		String baud = PlacelabProperties.get("placelab.gps_baud");
		
		if (device.length() == 0) {
			System.err.println("ERROR: placelab.gps_device not set.");
			return;
		}
		
		if (baud.length()==0) {
			baud = "4800";
		}
		
		StreamConnection serialPort;
		try {
			serialPort = (StreamConnection) Connector.open("comm:"+device+";baudrate="+baud);
			in = serialPort.openInputStream();
			out = serialPort.openOutputStream();
		} catch (IOException e) {
			System.err.println("ERR: unable to open port: " + e);
			return;
		}
	}
	
	public InputStream getInputStream () {
		return in;
	}
	
	public OutputStream getOutputStream () {
		return out;
	}
}
