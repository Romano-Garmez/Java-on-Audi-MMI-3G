
package org.placelab.demo.jsr0179;

import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;


public class Locator implements LocationListener {

	private LocationProvider provider;
	private DebugWindow debug = new DebugWindow("Locator Demo");
	
	public Locator () {
		try {
			provider = LocationProvider.getInstance(null);
		} catch (LocationException e) {
			debug.println("Couldn't get instance of Location Provider.");
			e.printStackTrace(debug);
			return;
		}
		
		provider.setLocationListener(this, 2, -1, -1);
		
		debug.show();
	}
	
	public void locationUpdated(LocationProvider provider, Location location) {
		
		if (location.isValid()) {
			debug.println("Latitude: " + location.getQualifiedCoordinates().getLatitude());
			debug.println("Longitude: " + location.getQualifiedCoordinates().getLongitude());
		} else {
			debug.println("Invalid location estimate.");
		}
		
		debug.println("---------------------------------");
	}

	public void providerStateChanged(LocationProvider provider, int newState) {
		debug.println("state change = " + newState);
	}
	
	public static void main (String[] args) {
		new Locator();
	}

}
