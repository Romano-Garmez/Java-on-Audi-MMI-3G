
package org.placelab.demo.virtualgps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.placelab.client.tracker.CentroidTracker;
import org.placelab.mapper.CompoundMapper;
import org.placelab.spotter.LogSpotter;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;

/**
 * 
 * You can pretend to be a GPS from a stumbler log.
 * 
 * usage: VirtualGPSLogDemo logFile
 * 
 * where logFile is some file residing in placelab.logdir
 *  
 */

public class VirtualGPSLogDemo {
	
	private static IOPort ioPort;
	
	static {
		
		String osName = System.getProperty("os.name").toLowerCase();
		String gpsDevice = System.getProperty("placelab.gps_device");
		gpsDevice = (gpsDevice == null) ? "" : gpsDevice;
		
		if ((gpsDevice == null) || (gpsDevice.toLowerCase().trim().equals("stdout"))) {
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
		
		if (args.length == 0) {
			System.err.println("ERR: no log file specified.  You must specify a file that resides in placelab.logdir");
			return;
		}
		
		String logFile = args[0].trim();
		
		Spotter logSpotter = LogSpotter.newSpotter(logFile);
		try {
			logSpotter.open();
		} catch (SpotterException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		logSpotter.startScanning();
		
		nmeaSim.addSpotter(logSpotter);
		
		
		nmeaSim.addTracker(new CentroidTracker(CompoundMapper.createDefaultMapper(true, true)));
		
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
