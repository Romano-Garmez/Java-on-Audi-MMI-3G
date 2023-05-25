package org.placelab.midp.stumbler;

import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.BeaconReading;
import org.placelab.core.GPSMeasurement;
import org.placelab.core.Measurement;
import org.placelab.midp.EventLogger;
import org.placelab.midp.GSMReading;
import org.placelab.midp.GSMSpotter;
import org.placelab.spotter.InquiryBluetoothGPSSpotter;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.SpotterListener;

/**
 * This class manages the GSM and Bluetooth spotters for the phone stumbler.
 * It registers as a listener with each spotter and sends appropriate
 * updates its respective listeners.
 */
public class PhoneStumblerManager implements SpotterListener{
	InquiryBluetoothGPSSpotter bgps;
	GSMSpotter gsm;
	LinkedList listeners;
	LinkedList allListeners;
	Measurement latestMeasurement = null;
	
	boolean sendNext = false;
	boolean sent = false;
	
	public PhoneStumblerManager() {
		listeners = new LinkedList();
		allListeners = new LinkedList();
		
		bgps = new InquiryBluetoothGPSSpotter();
		gsm = new GSMSpotter(1000);

		bgps.addListener(this);
		gsm.addListener(this);
	}

	public String getGPSStatus() {
		return bgps.getState();
	}

	/*
	 * Add a listener that is only notified of changes in the spotters
	 * such as a GSM cell changing
	 */
	public synchronized void addListener(StumblerListener sl) {
		listeners.add(sl);
	}

	/*
	 * Remove a listener that is only notified of changes in the spotters
	 * such as a GSM cell changing
	 */

	public synchronized void removeListener(StumblerListener sl) {
		listeners.remove(sl);
	}
	
	/*
	 * Add a listener that listens to every update from the spotters
	 */
	public synchronized void addAllListener(StumblerListener sl) {
		allListeners.add(sl);
	}

	/*
	 * Remove a listener that listens to every update from the spotters
	 */
	public synchronized void removeAllListener(StumblerListener sl) {
		allListeners.remove(sl);
	}
	
	/*
	 * Notify listeners who have registered to listen to every
	 * update the manager receives
	 */
	public synchronized void notifyAllListeners(Measurement[] m) {
		for(Iterator i = allListeners.iterator();i.hasNext();) {
			StumblerListener sl = (StumblerListener) i.next();
			sl.gotMeasurement(m);
		}
	}

	/*
	 * Notify listeners who have registered to listen to unique
	 * updates the manager receives
	 */
	public synchronized void notifyListeners(Measurement[] m) {
		notifyAllListeners(m);
		for(Iterator i = listeners.iterator();i.hasNext();) {
			StumblerListener sl = (StumblerListener) i.next();
			sl.gotMeasurement(m);
		}
	}
	
	public synchronized void gotMeasurement(Spotter sender, Measurement m) { 
		if(m == null)
			return;
		
		if(sender == bgps) {
			if(!((GPSMeasurement) m).isValid())
				return;
			
			Measurement[] ms = new Measurement[2];
				
			ms[0] = latestMeasurement;
			ms[1] = m;
				
			if(latestMeasurement != null && m != null) {
				notifyListeners(ms);
				sendNext = true;
			}
		} else if(sender == gsm) {
			//filter out bad gsm readings
			BeaconReading[] readings = ((BeaconMeasurement) m).getReadings();
			GSMReading reading = (GSMReading) readings[0];
			if(!reading.isValid()) {
				return;
			}

			//continue on
			Measurement[] ms = new Measurement[1];
			ms[0] = m;
			
			if(latestMeasurement == null || sendNext) {
				latestMeasurement = m;
				notifyListeners(ms);
				sendNext = false;
			} else {
				BeaconReading[] latestReadings = ((BeaconMeasurement) latestMeasurement).getReadings();
				GSMReading latestReading = (GSMReading) latestReadings[0];

				BeaconReading[] brs = ((BeaconMeasurement) m).getReadings();
				GSMReading thisReading = (GSMReading) brs[0];
				
				if (!latestReading.getId().equals(thisReading.getId())) {
					latestMeasurement = m;					
					notifyListeners(ms);
				} else {
					notifyAllListeners(ms);
				}
			}
		}
	}

	public void open() {
		try {
			bgps.open();
			gsm.open();
		} catch(SpotterException se) {
			StumblerMidlet.alert(se.getMessage());
		}
	}
	
	public synchronized void close() {
		try {
			if(bgps != null) bgps.close();
			if(gsm != null) gsm.close();
		} catch(SpotterException se) {
			StumblerMidlet.alert(se.getMessage());
		}
	}
	
	public synchronized void start() {
		try {
			gsm.startScanning();
			bgps.startScanning();		
		} catch(Exception e) {
			EventLogger.logError(e);
			e.printStackTrace();
		}

	}
	
	public synchronized void stop() {
		bgps.stopScanning();
		gsm.stopScanning();
	}

	public void spotterExceptionThrown(Spotter sender, SpotterException ex) {
	}
}
		

