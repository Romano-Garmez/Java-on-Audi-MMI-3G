
package org.placelab.demo.virtualgps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.placelab.core.PlacelabProperties;

import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import gnu.io.NoSuchPortException;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;

public class RXTXSerialPort extends IOPort {
	private InputStream in;
	private OutputStream out;
	
	public RXTXSerialPort () {
		
		String device = PlacelabProperties.get("placelab.gps_device");
		String baud = PlacelabProperties.get("placelab.gps_baud");
		
		if (device.length() == 0) {
			System.err.println("ERROR: placelab.gps_device not set.");
			return;
		}
		
		if (baud.length()==0) {
			baud = "4800";
		}
		
	    SerialPort sp;
	    CommPortIdentifier p;
	    InputStream inputStream;
	    OutputStream outputStream;

	    try {
	      p = CommPortIdentifier.getPortIdentifier(device);
	      sp = (SerialPort) p.open("placelabgps", 1000);

	      sp.setSerialPortParams(Integer.parseInt(baud),
	                             SerialPort.DATABITS_8,
	                             SerialPort.STOPBITS_1,
	                             SerialPort.PARITY_NONE);

	      in = sp.getInputStream();
	      out = sp.getOutputStream();

	    }
	    catch (UnsupportedCommOperationException e) {
	    		System.err.println("ERR: UnsupportedCommOperationException: " + e);
			return;
		} catch (NoSuchPortException e) {
			System.err.println("ERR: NoSuchPortException: " + e);
			return;
		} catch (PortInUseException e) {
			System.err.println("ERR: PortInUseException: " + e);
			return;
		} catch (IOException e) {
			System.err.println("ERR: IOException: " + e);
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
