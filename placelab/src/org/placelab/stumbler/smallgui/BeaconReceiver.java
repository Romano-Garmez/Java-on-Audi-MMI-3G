
package org.placelab.stumbler.smallgui;

import java.util.Hashtable;

import org.placelab.collections.HashMap;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.BeaconReading;
import org.placelab.core.Measurement;
import org.placelab.spotter.Spotter;

public abstract class BeaconReceiver extends Receiver {

	private HashMap uniqueNodeList = new HashMap();
	
	private Hashtable[] lastMeasurement = null;
	
	public BeaconReceiver (Class readingClass) {
		super(BeaconReading.class, readingClass);
	}
	
	public void gotMeasurementImpl(Spotter sender, Measurement m) {
		
		BeaconReading[] readings = (BeaconReading[]) ((BeaconMeasurement) m).getReadings();
		
		if (readings == null)
			return;
		
		Hashtable[] data = new Hashtable[readings.length];
		
		for (int i=0;i<readings.length;i++) {
			data[i] = extractFields(readings[i]);
			
			if (data[i].containsKey("address")) {
				String address = (String) data[i].get("address");
				
				if (!uniqueNodeList.containsKey(address)) 
					uniqueNodeList.put(address, null);
			}
			
		}
		
		lastMeasurement = data;
	}
	
	public abstract Hashtable extractFields (BeaconReading br);
	
	public abstract Class[] getSupportedSpotters();
	
	public int totalNumBeacons () {
		return uniqueNodeList.size();
	}
	public int currentNumBeacons () {
		if (lastMeasurement == null) 
			return 0;
		
		return lastMeasurement.length;
	}
	public Hashtable[] getBeaconData () {
		return lastMeasurement;
	}
}
