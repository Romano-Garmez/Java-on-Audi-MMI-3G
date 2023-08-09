package org.placelab.spotter;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.placelab.core.PlacelabProperties;

/**
 * SerialJ9GPSSpotter is the PocketPC implementation of a serial spotter.
 * Instead of using RXTX, we use IBM's implementation.  We subclass 
 * StreamGPSSpotter and it handles the IO, all we have to do is open 
 * the serial port and get the streams.
**/
public class SerialJ9GPSSpotter extends StreamGPSSpotter {
	private String gpsPort = null;
    private StreamConnection serialPort = null;
   
	public SerialJ9GPSSpotter() {
		super();
	}
	public void open() throws SpotterException {	
		try {
			if(serialPort != null) {
				close();
			}
			gpsPort = PlacelabProperties.get("placelab.gps_device");
			if(gpsPort == null) {
				throw new SpotterException("placelab.gps_device undefined");
			}
			int speed = getSerialSpeed();
			System.out.println("SerialJ9GPSSpotter: Using serial device: " + gpsPort + 
					" @ " + speed + " baud");
			serialPort = (StreamConnection) Connector.open("comm:"+gpsPort+";baudrate="+getSerialSpeed());
			
			if (serialPort == null) {
				throw new SpotterException("serial port could not be opened");
			}
			
			super.open(serialPort.openInputStream(), serialPort.openOutputStream());
		} catch (IOException ioe) {
			throw new SpotterException(ioe);
		} catch (Exception e) {
			throw new SpotterException(e);
		}
	}
	
	public void close() throws SpotterException {
		super.close();
		if (serialPort != null) {
			try {
				serialPort.close();
			} catch (Throwable t) {
				// This is horrible but necessary.
			}
			serialPort = null;
		}
	}
	
	
	private static int getSerialSpeed() {
	    int speed = 4800;
	    try {
	        speed = Integer.parseInt(PlacelabProperties.get("placelab.gps_speed"));
	    } catch (NumberFormatException nfe) { }
	    return speed;
	}
}
