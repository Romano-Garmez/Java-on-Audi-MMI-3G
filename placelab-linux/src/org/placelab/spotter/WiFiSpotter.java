/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.spotter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.Measurement;
import org.placelab.core.Types;
import org.placelab.core.WiFiReading;
import org.placelab.eventsystem.EventSystem;
import org.placelab.util.StringUtil;


/**
 * WiFiSpotter depends on a native code library to poll the WiFi
 * hardware.
 * 
 * 
 *  
 */
public class WiFiSpotter extends SyncSpotter implements PeriodicScannable {
	private long scanIntervalMillis=0;
	private boolean spotterLibraryLoaded=false, spotterInitialized=false;

    public native boolean spotter_init();
	public native void spotter_shutdown();
	public native String[] spotter_poll();

           
	/** create a spotter with a default scanning interval; the scanning interval is
	 *  relevant only if the spotter is used in continuous scanning mode */
	public WiFiSpotter() {
		this(1000);
	}
	/** 
	 * create a spotter with the specified scanning interval.  The scanning interval
	 * is relevant only if the spotter is used in continuous scanning mode.  This 
	 * form of the contructor will result in a new thread getting created to perform
	 * continuous scans
	 */
	public WiFiSpotter(long scanIntervalMillis) {
		super();
		this.scanIntervalMillis = scanIntervalMillis;
	}
	public synchronized void setPeriodicScanInterval(long intervalMillis) {
		scanIntervalMillis = intervalMillis;
	}
	public synchronized long getPeriodicScanInterval() {
		return scanIntervalMillis;
	}
	
	public void open() throws SpotterException {
		if (spotterInitialized) return;
		
        try {
			if (!spotterLibraryLoaded) {
				spotterLoadLibrary();
				spotterLibraryLoaded = true;
			}
			if (!spotterInitialized) {
				if (spotter_init())
					spotterInitialized = true;
			}
		} catch (UnsatisfiedLinkError e) {
			throw new SpotterException("MSG: " + e.getMessage() + 
                  "\n*** WiFi spotter library unavailable, running without spotter");
		} catch (IOException e) {
			throw new SpotterException("MSG: " + e.getMessage() + 
            "*** WiFi spotter library unavailable, running without spotter\n" +
					"*** Error loading spotter library, running without spotter");
		}

	}

	public void close() {
		if (spotterInitialized) {
            spotter_shutdown();
            spotterInitialized = false;
		}
	}
	

	/* Override the default eventsystem-based scanning in the SyncSpotter class to use 
	 * a background thread, since the getMeasurement() operation is actually blocking
	 */
	public void startScanning(EventSystem evs) {
		scanUsingBackgroundThread(evs);
	}

	protected long nextScanInterval() {
		return getPeriodicScanInterval();
	}
	protected synchronized Measurement getMeasurementImpl() throws SpotterException {
		if (!spotterLibraryLoaded || !spotterInitialized) {
			throw new SpotterException("Spotter not initialised");
		}

		// call the native method
		String sarr[] = spotter_poll();

		// putting these in a hashtable will eliminate dupes
		HashMap readings = new HashMap();
		for (int i = 0; sarr != null && i <= (sarr.length - 5); i += 5) {
			try {
				sawAP(readings, StringUtil.canonicalizeBSSID(sarr[i]),
						sarr[i + 1], Integer.parseInt(sarr[i + 2]), !"0"
								.equals(sarr[i + 3]), !"0".equals(sarr[i + 4]));
			} catch (Exception ex) {
			    throw new SpotterException("WiFiSpotter: Malformed reply from native spotter:\n"+ex.getMessage());
				//System.out.println("Malformed spotter reply");
				//ex.printStackTrace();
			}
		}

		BeaconMeasurement meas = new BeaconMeasurement(System.currentTimeMillis());
		for (Iterator it = readings.values().iterator(); it.hasNext();) {
			WiFiReading reading = (WiFiReading) it.next();
			meas.addReading(reading);
		}
		return meas;

	}
    private void sawAP(HashMap readings, String bssid, String ssid, int rssi, boolean wep, boolean inf) {
		/*
		 * Sometimes cards return funky RSSI values; until we figure out what is
		 * going on, we do the hack below (yatin)
		if (rssi <= -100)
			rssi = -65;
		if (rssi >= -35)
			rssi = -65;
        */
    	
    	/* our code expects RSSI values to be between -Types.NETSTUMBLER_RSSI_ADJUSTMENT and zero 
    	 * check for it
    	 */
    	if (rssi < -Types.NETSTUMBLER_RSSI_ADJUSTMENT) rssi = -Types.NETSTUMBLER_RSSI_ADJUSTMENT;
    	else if (rssi > 0) rssi = 0;
    	
		WiFiReading reading = new WiFiReading(bssid, ssid, rssi, wep, inf);
		readings.put(bssid, reading);
	}
    
    
    private void spotterLoadLibrary() throws IOException {
		// JWSTODO de-wifi-ification: native WiFiSpotter library should not be
		// called simply "spotter"
		String likelyLibName = System.mapLibraryName("spotter");
		// Get the extension
		int idx = likelyLibName.lastIndexOf('.');
		String ext;
		String pre;
		if (idx != -1) {
			ext = likelyLibName.substring(idx, likelyLibName.length());
			pre = likelyLibName.substring(0, idx);
		} else {
			pre = "spotter";
			ext = "";
		}

		if (likelyLibName.endsWith("dll")) {
			// then its a windows of some form
			String osName = System.getProperty("os.name");
			if (osName.equalsIgnoreCase("Windows CE")) {
				likelyLibName = "spotter_ce.dll";
			}
		}
		Enumeration e = this.getClass().getClassLoader().getResources(likelyLibName);
		boolean found = false;
		File tempFile = File.createTempFile(pre, ext);
		while (e.hasMoreElements()) {
			URL jarURL = (URL) e.nextElement();
			InputStream is = jarURL.openStream();
			// now squirt this lib into tempFile
			FileOutputStream fo = new FileOutputStream(tempFile);
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				fo.write(buffer, 0, bytesRead);
			}
			fo.close();
			String path = tempFile.getAbsolutePath();
			try {
				System.load(path);
				found = true;
				break;
			} catch (UnsatisfiedLinkError ule) {
				tempFile.delete();
				tempFile = File.createTempFile(pre, ext);
			}
		}
		if (!found) {
			tempFile.delete();
			// then maybe the library isn't in a jar, but somewhere else
			System.loadLibrary("spotter");
		} else {
			tempFile.deleteOnExit();
		}
		//System.loadLibrary("spotter");
		//System.load("/Applications/eclipse/spotter.dylib");
	}
}
