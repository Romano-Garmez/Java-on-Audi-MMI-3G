
package org.placelab.demo.jsr0179;

import javax.microedition.location.Coordinates;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.ProximityListener;

/**
 * Sample class that demonstrates how to use the Location API to receive
 * notifications whenever you are within a certain proximity/radius of a given
 * coordinate.
 */

public class Proximity implements ProximityListener {
	
	private static DebugWindow debug;
	
	private long start;
	
	public Proximity () {
			
		LocationProvider provider;
		
		try {
			provider = LocationProvider.getInstance(null);
		} catch (LocationException e) {
			debug.println("Couldn't get instance of Location Provider.");
			e.printStackTrace(debug);
			return;
		}
		
		
	
		Location home = null;
		
		for (int i = 0;i<5;i++) {
			debug.print("Trying to get current location...");
			try {
				home = provider.getLocation(5);
				break;
			} catch (LocationException le) {
				debug.println("failed: " + le.toString());
				le.printStackTrace(debug);
			} catch (InterruptedException le) {
				le.printStackTrace(debug);
			}
			
			if (i==4) {
				debug.println("Failed to get current location on all attempts.");
				return;
			}
	
			try {
				debug.println("Waiting 1 second before trying again.");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
			}
		}
		
		if (home == null) 
			return;

		if (!home.isValid()) {
			debug.println("unavailable.");
			debug.println("I was unable to determine your current location.  The area you are in probably is not in the mapping database.");
			return;
		}
		
		// success & valid location reading
		debug.println("success.");
		
		debug.println("Current Location:");
		debug.println("Latitude: " + home.getQualifiedCoordinates().getLatitude());
		debug.println("Longitude: " + home.getQualifiedCoordinates().getLongitude());
		
		debug.println("-----------------------------");
		
		debug.println("This app demonstrates the proximity notification feature.  In this demo, we have defined " +
				"the above location to be your \"home\".  As long as you are within 20 meters of your home, you will " +
				"see a notification in this window.  When you go further than 20 meters from your home, these " +
				"notifications will cease.");
		
		debug.println("-----------------------------");
		
		try {
			LocationProvider.addProximityListener(this, home.getQualifiedCoordinates(), 20);
			start = System.currentTimeMillis();
		} catch (LocationException e) {
			debug.println("Failed to set up proximity listener.");
			e.printStackTrace(debug);
			return;
		}
		
		
	}
	
	public void monitoringStateChanged(boolean isMonitoringActive) {
		debug.println("Monitoring State: " + (isMonitoringActive ? "Active" : "Inactive"));
	}
	
	public void proximityEvent (Coordinates coordinates, Location location) {
		int secs = (int) ((System.currentTimeMillis() - start) / 1000);
		debug.println("You are near your home. [ +" + secs + " secs ]");
	}
	
	public static void main (String[] args) {
		
		debug = new DebugWindow("Proximity Demo");
		
		/*
		 * This thread business is only necessary because the DebugWindow class
		 * will run its own SWT event loop in the current thread, so we must be
		 * sure that we carry out all our actions in a separate thread.
		 */
		new Thread(new Runnable(){
			public void run () {
				new Proximity();
			}
		}).start();
		
		debug.show();
		
		
	}
}
