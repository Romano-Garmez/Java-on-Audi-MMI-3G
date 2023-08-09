package org.placelab.example;

import org.placelab.core.BeaconMeasurement;
import org.placelab.core.WiFiReading;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.WiFiSpotter;

/**
 * A sample that creates a WiFiSpotter and uses it to get measurements.
 * This will only return readings if a WiFi card is present.
 */
public class WiFiSpotterExample {

	public static void main(String[] args) {
		
		Spotter s = new WiFiSpotter();
		try {
			s.open();
			BeaconMeasurement m = (BeaconMeasurement) s.getMeasurement();
			System.out.println(m.numberOfReadings() + " APs were seen\n");
			if (m.numberOfReadings() > 0) {
				System.out.println(pad("MAC Address", 20) + pad("SSID", 30)
						+ pad("RSSI", 10));
				// Iterate through the list and print the readings
				for (int i = 0; i < m.numberOfReadings(); i++) {
					WiFiReading r = (WiFiReading) m.getReading(i);
					System.out.println(pad(r.getId(), 20)
							+ pad(r.getSsid(), 30) + pad("" + r.getRssi(), 10));
				}
			}
		} catch (SpotterException ex) {
			ex.printStackTrace();
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
