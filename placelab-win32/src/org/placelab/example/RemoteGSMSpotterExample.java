package org.placelab.example;

import org.placelab.core.BeaconMeasurement;
import org.placelab.midp.GSMReading;
import org.placelab.spotter.RemoteGSMSpotter;
import org.placelab.spotter.Spotter;

/**
 * This sample class accesses the GSM server on the cell phone and 
 * returns remote GSM readings. This will not work for you unless
 * you have a 60 Series cell phone and it is running the GSMBT midlet.
 */
public class RemoteGSMSpotterExample {

	public static void main(String[] args) {
		
		if (args.length != 1) {
			System.out.println("This sample prgram requires that you have a 60 Series cell phone that is running");
			System.out.println("the BTGSM midlet. When you run the midlet copy the mac address and port number that");
			System.out.println("are printed during initialization. Pass those in as the argument to this program.");
			System.out.println("*** Example: java RemoteGSMSpotterExample 000e6d43ec17:4");
			System.exit(1);
		}
		
		Spotter s = new RemoteGSMSpotter(args[0],false);
		try {
			s.open();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Sorry, your cell phone must not be set up correctly");
		}
		
		System.out.println("Getting 5 GSM readings from the phone, 2 seconds apart\n");
		System.out.println(pad("Tower ID", 25) + pad("Name", 40)
				+ pad("Signal strength", 10));
		int i = 0;
		while (i < 5) {
			try {
				BeaconMeasurement m = (BeaconMeasurement) s.getMeasurement();
				if ((m != null) && (m.numberOfReadings() > 0)) {
					// This will really only be 1
					for (int j = 0; j < m.numberOfReadings(); j++) {
						GSMReading r = (GSMReading) m.getReading(j);
						System.out.println(pad(r.getId(), 25)
								+ pad(r.getHumanReadableName(), 40) + pad("" + r.getNormalizedSignalStrength(), 10));
						i++;
					}
				}
				Thread.sleep(2000);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	// Pad out a string to the passed length
	public static String pad(String str, int len) {
		StringBuffer sb = new StringBuffer(str);
		for (int i=str.length(); i < len; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}
	
}
