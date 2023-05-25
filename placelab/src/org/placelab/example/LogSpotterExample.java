package org.placelab.example;


import org.placelab.core.BeaconReading;
import org.placelab.core.StumblerMeasurement;
import org.placelab.spotter.LogSpotter;
import org.placelab.spotter.SpotterException;
import org.placelab.util.Cmdline;

/** This sample demonstrates the use of the LogSpotter class
 *  We show how to parse a large text-exported trace,
 *  report the size of the trace and enlist some key values in the log.
 */

public class LogSpotterExample {

	
	public static void main(String [] args)
	{
		Cmdline.parse(args);
		
		String inputFile = Cmdline.getArg("tracefile");
		
		if (inputFile == null) {
			System.err.println("Usage: java " + LogSpotterExample.class.getName() + " --tracefile filename");
			System.exit(1);
		}
		
		LogSpotter log = LogSpotter.newSpotter(inputFile);
		try {
			log.open();
			// output as many chunks as are there in the inputfile
			if(!log.logIsFinished()) do {
				StumblerMeasurement m = (StumblerMeasurement)log
						.getMeasurement();
				if(m == null) {
				    System.out.println("log is finished");
				    break;
				}
				if (m.numberOfReadings() > 0) {
					System.out.println(pad("Timestamp", 20)
							+ pad("Latitude", 20) + pad("Longitude", 20)
							+ pad("BSSID", 30) + pad("RSSI", 10));
					//iterate through the list and print the readings
					for (int i = 0; i < m.numberOfReadings(); i++) {
						BeaconReading br = (BeaconReading) m
								.getReading(i);
						System.out.println(pad(""
								+ (((long) m.getTimestamp() / 1000L) * 1000L),
								20)
								+ pad(m.getPosition().getLatitudeAsString(),
										20)
								+ pad(m.getPosition().getLongitudeAsString(),
										20)
								+ pad(br.getId(), 30)
								+ pad("" + br.getNormalizedSignalStrength(), 10));
					}
					System.out.println();
				}
			} while(!log.logIsFinished());
		} catch (SpotterException ex) {
			ex.printStackTrace();
		}

	}
	
	public static String pad(String str, int len) {
		StringBuffer sb = new StringBuffer(str);
		for (int i=str.length(); i < len; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}
	
}
