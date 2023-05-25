
package org.placelab.demo.virtualgps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.placelab.client.tracker.CentroidTracker;
import org.placelab.core.PlacelabProperties;
import org.placelab.mapper.CompoundMapper;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;

/**
 * Outputs NMEA formatted sentences over a serial port allowing
 * you to use any external application that reads NMEA.
 * 
 * The serial port should be specified with the system
 * properties placelab.gps_device, and placelab.gps_baud
 * 
 * usage: VirtualGPS [ spotterClassName [ spotterClassName2 ] [...] ] 
 * 
 * Running with no arguments will cause the program to automatically
 * start the WiFiSpotter.  If you wish to run different spotters,
 * you must specify the class names (as they appear in org.placelab.spotter)
 * on the command line.
 * 
 * 
 */

public class VirtualGPS {
	
	private static IOPort ioPort;
	
	static {
		
		String osName = System.getProperty("os.name").toLowerCase();
		String gpsDevice = PlacelabProperties.get("placelab.gps_device");
		
		if ((gpsDevice.length() == 0) || (gpsDevice.toLowerCase().trim().equals("stdout"))) {
			System.err.println("Using stdout instead of serial.");
			ioPort = new IOPort() {
				public InputStream getInputStream () {
					return System.in;
				}
				
				public OutputStream getOutputStream () {
					return System.out;
				}
			};
		} else {
			if (osName.equals("windows ce")) {
				ioPort = new J9SerialPort();
			} else {
				// default to RXTX
				ioPort = new RXTXSerialPort();
			}
		}
			
	}
	
	public static void main (String[] args) {
		
		NMEASimulator nmeaSim = new NMEASimulator();
		
		if (args.length == 0)
			args = new String[]{ "WiFiSpotter" };
		
		for (int i=0;i<args.length;i++) {
			String arg = args[i].trim();
			if (arg.startsWith("org.placelab.spotter."))
				arg = arg.substring(22);
			
			Spotter spotter = null;
			String className = "org.placelab.spotter." + arg;
			
			try {
				spotter = (Spotter) Class.forName(className).newInstance();
			} catch (IllegalAccessException e) {
			} catch (InstantiationException e) {
			} catch (ClassNotFoundException e) {
			}
			
			if (spotter == null) {
				System.err.println("ERR: unable to load class: " + className);
				return;
			}
			try {
			spotter.open();
			} catch (SpotterException ex) {
				ex.printStackTrace();
				System.exit(1);
			}
			spotter.startScanning();
			
			nmeaSim.addSpotter(spotter);
		}
		
		try {
			nmeaSim.addTracker(new CentroidTracker(CompoundMapper.createDefaultMapper(true,true)));
		} catch (Exception e) {
			System.err.println("ERR: staring mapper: " + e);
			return;
		}
		
		InputStream in = nmeaSim.getInputStream();
		OutputStream out = ioPort.getOutputStream();
		
		System.err.println("Running... ");
		
		try {
			for (;;) {
				int b = in.read();
				out.write((char) b);
			}
		} catch (IOException e) {
			System.err.println("ERR: problem writing to port or reading from gps simulator:" + e);
			e.printStackTrace();
		}
		
	}

}
