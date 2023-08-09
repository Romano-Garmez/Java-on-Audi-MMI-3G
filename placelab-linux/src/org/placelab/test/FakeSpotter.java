package org.placelab.test;

import org.placelab.core.BeaconMeasurement;
import org.placelab.core.Measurement;
import org.placelab.core.WiFiReading;
import org.placelab.spotter.SyncSpotter;


/**
 * 
 * Used for testing.
 */
public class FakeSpotter extends SyncSpotter {
	private BeaconMeasurement data;
	
	public static final String LAB_AP_0_MAC = "001122334455";
	public static final String LAB_AP_1_MAC = "112233445566";
	
	public void fakeLab() {
		data=new BeaconMeasurement(0L);
		data.addReading(new WiFiReading(LAB_AP_0_MAC, "1100", 0, false, true));
		data.addReading(new WiFiReading("223344556677", "IntelGuest", 0, false, true));
		data.addReading(new WiFiReading(LAB_AP_1_MAC, "1100", 0, false, true));
	}
	
	protected Measurement getMeasurementImpl() {
		return data;
	}


	public void open() {
	}
	public void close() {
	}
	protected long nextScanInterval() {
		return Integer.MAX_VALUE;
	}
}
