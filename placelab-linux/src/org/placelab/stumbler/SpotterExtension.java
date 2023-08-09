/*
 * Created on Jul 22, 2004
 *
 */
package org.placelab.stumbler;

import org.placelab.core.Measurement;
import org.placelab.eventsystem.EventSystem;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.SpotterListener;

public class SpotterExtension implements Spotter, SpotterListener {
	private Spotter spotter;
	private boolean isQuick;
	private long staleTimeMillis;
	private Measurement latestMeasurement=null;
	private boolean isScanning = false;

	/* Even Spotters that return with a quickness take some time, and
	 * should only be called so often.
	 * When using a BluetoothSpotter as a critical spotter, we may ask
	 * them to get measurement whilst they are currently doing a getMeasurement
	 * or back to back to back.
	 * This breaks things on windows, so I return the last measurement
	 * if a stale time is specified on a quick SpotterExtension and it hasn't
	 * yet been reached since the last call to getLatestMeasurement.
	 * 
	 * If you know this won't affect you, just use a staleTimeMillis of -1,
	 * but if you plan to do wifi on windows, expect it to give you trouble
	 * - fats
	 */
	
	
	// GPS measurements that are older than 3 seconds are no good.
	public static long GPS_STALE_TIME=3000;
	
	public SpotterExtension(Spotter spotter, boolean isQuick, long staleTimeMillis) {
		this.spotter = spotter;
		this.isQuick = isQuick;
		this.staleTimeMillis = staleTimeMillis;
	}
	
	public Spotter getSpotter() { return spotter; }

	public void open() throws SpotterException {
		spotter.open();
		latestMeasurement = null;
		spotter.addListener(this);
	}

	public void close() throws SpotterException {
		try {
			spotter.close();
		} finally {
			spotter.removeListener(this);
			latestMeasurement = null;
		}
	}
	
	public Measurement getLatestMeasurement() throws SpotterException {
		if (isQuick) {
			if(latestMeasurement == null ||
					staleTimeMillis <= 0) {
				latestMeasurement = spotter.getMeasurement();
				return latestMeasurement;
			} else {
				long stamp = latestMeasurement.getTimestamp();
				long difference = System.currentTimeMillis() - stamp;
				if(difference > staleTimeMillis) {
					// once we have a latestMeasurement, we don't ever want
					// it to become null again
					Measurement temp = spotter.getMeasurement();
					if(temp != null) latestMeasurement = temp;
					return temp;
				} else {
					// too fast between queries
					return null;
				}
			}
		} else {
            if (latestMeasurement == null) return null;
            long stamp = latestMeasurement.getTimestamp();
            long difference = System.currentTimeMillis() - stamp;
            if(difference > staleTimeMillis) {
                return null;
            } else {
                return latestMeasurement;
            }
		}
	}

	public Measurement getMeasurement() throws SpotterException {
		return getLatestMeasurement();
	}

	public void startScanning() {
		spotter.startScanning();
		isScanning = true;
	}

	public void startScanning(EventSystem evs) {
		spotter.startScanning(evs);
		isScanning = true;
	}
	
	public boolean isScanning () {
		return isScanning;
	}

	public void stopScanning() {
		spotter.stopScanning();
		isScanning = false;
	}

	public void scanOnce() {
		spotter.scanOnce();
	}

	public void scanOnce(EventSystem evs) {
		spotter.scanOnce(evs);
	}

	public void addListener(SpotterListener listener) {
		spotter.addListener(listener);
	}

	public void removeListener(SpotterListener listener) {
		spotter.removeListener(listener);
	}
	
	public void gotMeasurement(Spotter sender, Measurement m) {
		latestMeasurement = m;
	}

	/* (non-Javadoc)
	 * @see org.placelab.spotter.SpotterListener#spotterExceptionThrown(org.placelab.spotter.SpotterException)
	 */
	public void spotterExceptionThrown(Spotter s, SpotterException ex) {
		// TODO Auto-generated method stub
		
	}
}
