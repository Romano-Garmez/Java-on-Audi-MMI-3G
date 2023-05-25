
package org.placelab.stumbler;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.placelab.core.BeaconMeasurement;
import org.placelab.core.BeaconReading;
import org.placelab.core.GPSMeasurement;
import org.placelab.core.Measurement;
import org.placelab.core.PlacelabProperties;
import org.placelab.demo.mapview.BrowserControl;
import org.placelab.spotter.PlacelabStumblerLogSpotter;
import org.placelab.spotter.Spotter;

/**
 * 
 *
 * This isn't really so much an audio notifier, as it calls out to external
 * executables which could do anything. It does come by default configured for
 * playing sounds on XP (and mac os too! - fats)
 */
public class AudioNotifier implements StumblerFunnelUpdateListener {
	private static String[] EXEC_NEW_AP;
	private static String[] EXEC_GPS_LOCKED;
	private static String[] EXEC_GPS_UNLOCKED;
	
	private String[] execNewAP = null;
	private String[] execGpsLocked = null;
	private String[] execGpsUnlocked = null;
	
	public boolean printNotifications = true;
	
	private Hashtable seenAPs;
	private boolean wasValid = false;
	
	static {
	    if(BrowserControl.isMacPlatform()) {
	        EXEC_NEW_AP = new String[] {"osascript", "-e", "say \"new\""};
	        EXEC_GPS_LOCKED = new String[] {"osascript", "-e", "say \"locked\""};
	        EXEC_GPS_UNLOCKED = new String[] {"osascript", "-e", "say \"unlocked\""};
	    } else if(BrowserControl.isWindowsPlatform()) {
	        EXEC_NEW_AP = 
	            new String[] {"c:\\windows\\system32\\cmd.exe /C start /min /wait c:\\windows\\system32\\sndrec32.exe /play /close c:\\windows\\Media\\Windows XP Ringin.wav"};
	        EXEC_GPS_LOCKED = 
	            new String[] {"c:\\windows\\system32\\cmd.exe /C start /min /wait c:\\windows\\system32\\sndrec32.exe /play /close c:\\windows\\Media\\Windows XP Hardware Insert.wav"};
	    	EXEC_GPS_UNLOCKED = 
	    	    new String[] {"c:\\windows\\system32\\cmd.exe /C start /min /wait c:\\windows\\system32\\sndrec32.exe /play /close c:\\windows\\Media\\Windows XP Hardware Remove.wav"};
	    }
	}
	
	public AudioNotifier() {
		seenAPs = new Hashtable();
		String str = null;
		str = PlacelabProperties.get("placelab.stumbler.newapcmd");
		if ((str != null) && (str.length() > 0)) {
			execNewAP = new String[] {str};
		} else {
			execNewAP = EXEC_NEW_AP;
		}
		str = PlacelabProperties.get("placelab.stumbler.lostgpscmd");
		if ((str != null) && (str.length() > 0)) {
			execGpsUnlocked = new String[] {str};
		} else {
			execGpsUnlocked = EXEC_GPS_UNLOCKED;
		}
		str = PlacelabProperties.get("placelab.stumbler.lockedgpscmd");
		if ((str != null) && (str.length() > 0)) {
			execGpsLocked = new String[] {str};
		} else {
			execGpsLocked = EXEC_GPS_LOCKED;
		}
		System.out.println("Serving audio notifications...");
	}
	
	public void inspectMeasurement(Measurement m) {
		if (m instanceof BeaconMeasurement) {
			BeaconMeasurement bm = (BeaconMeasurement) m;
			BeaconReading br[] = bm.getReadings();
			for (int j = 0; j < br.length; j++) {
				checkAP(br[j]);
			}
		}		
	}

	public void checkAP(BeaconReading br) {
		if (seenAPs.get(br.getId()) == null) {
			// New AP
			seenAPs.put(br.getId(),"yup");
			if (printNotifications) {
				System.out.println("=== " + br.getId() + "  " + br.getHumanReadableName());
			}
			if (execNewAP != null) {
				try {
					Runtime.getRuntime().exec(execNewAP);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Pass me a file!");
			return;
		}
		try {
			Spotter sp = new PlacelabStumblerLogSpotter(args[0]);
			AudioNotifier an = new AudioNotifier();
			while (true) {
				Measurement meas = sp.getMeasurement();
				if (meas == null) {
					System.out.println("All Done!");
					return;
				}
				an.inspectMeasurement(meas);
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stumblerUpdated(Hashtable updates) {
        for(Enumeration e1 = updates.elements(); e1.hasMoreElements(); ) {
        	Object o = e1.nextElement();
        	if (o instanceof BeaconMeasurement) {
        		handleBeacons((BeaconMeasurement)o);
        	} else if (o instanceof GPSMeasurement) {
        		handleGPS((GPSMeasurement)o);
        		
        	} else {
        		System.out.println("Got something unexpected! " + o.getClass().getName());
        		System.exit(1);
        	}
        }
	}
		
	private void handleGPS(GPSMeasurement p) {
		if (p.isValid()) {
			// If we're already valid that's cool
			if (wasValid == true) {
				return;
			}
			wasValid = true;
			// Play sound
			if (execGpsLocked != null) {
				try {
					Runtime.getRuntime().exec(execGpsLocked);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			// If we're already valid that's cool
			if (wasValid == false) {
				return;
			}
			wasValid = false;
			// Play sound
			if (execGpsUnlocked != null) {
				try {
					Runtime.getRuntime().exec(execGpsUnlocked);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void handleBeacons(BeaconMeasurement p) {
		for (int j=0; j<p.numberOfReadings(); j++) {
			checkAP(p.getReading(j));
		}
	}

}
