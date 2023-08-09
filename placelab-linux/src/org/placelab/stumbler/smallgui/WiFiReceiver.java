package org.placelab.stumbler.smallgui;

import java.util.Hashtable;

import org.placelab.core.BeaconReading;
import org.placelab.core.Coordinate;
import org.placelab.core.WiFiReading;
import org.placelab.spotter.NMEAGPSSpotter;
import org.placelab.spotter.WiFiSpotter;


public class WiFiReceiver extends BeaconReceiver {

	
	public WiFiReceiver() {
		super(WiFiReading.class);
	}
	
	public Hashtable extractFields (BeaconReading reading) {
		WiFiReading r = (WiFiReading) reading;
		
		Hashtable hash = new Hashtable();
		hash.put("type", "WiFi");
		
		hash.put("address", r.getBssid());
		hash.put("name", r.getHumanReadableName());
		hash.put("rssi", Integer.toString(r.getNormalizedSignalStrength()));
		hash.put("mode", r.isInfrastructureMode() ? "Infr" : "Ad-Hoc");
	
		GPSReceiver gps = (GPSReceiver) Stumbler.getStumbler().getReceiver(NMEAGPSSpotter.class);
		Coordinate c = gps.getLastCoordinate();
		
		String lat = "";
		String lon = "";
		
		if (c != null) {
			lat = c.getLatitudeAsString();
			lon = c.getLongitudeAsString();
		}
		
		hash.put("lat", lat);
		hash.put("lon", lon);
		
		return hash;
	}
	
	public Class[] getSupportedSpotters () {
		return new Class[]{ WiFiSpotter.class };
	}
	
	public String getType () {
		return "WiFi";
	}
	
}
