/*
 * Created on 21-May-04
 *
 */
package org.placelab.spotter;

import javax.bluetooth.LocalDevice;

import org.placelab.core.BeaconMeasurement;
import org.placelab.core.BluetoothReading;
import org.placelab.core.Measurement;
import org.placelab.eventsystem.EventSystem;

/**
 * Spotter for Bluetooth on systems with JSR-82 support. 
 * 
 * 
 */
public class BluetoothSpotter extends AsyncSpotter implements PeriodicScannable {	
	private long scanIntervalMillis;
	private BluetoothScan currentScan=null;
	private boolean filterPhones = false;
	
	public BluetoothSpotter() {
		this(1000,false);
	}
	
	public BluetoothSpotter(long intervalMillis,boolean filterPhones) {
		super();
		scanIntervalMillis = intervalMillis;
		this.filterPhones = filterPhones;
	}
	public synchronized void setPeriodicScanInterval(long intervalMillis) {
		scanIntervalMillis = intervalMillis;
	}
	public synchronized long getPeriodicScanInterval() {
		return scanIntervalMillis;
	}
	
	public void open() throws SpotterException {
		// i try to get the local device to find out if
		// the local machine indeed has bluetooth.  if not,
		// then i throw a spotter exception
		try {
			LocalDevice localDevice = LocalDevice.getLocalDevice();
			if(localDevice.getBluetoothAddress().equals("000000000000")) {
				throw new SpotterException("Can't see local bluetooth device.");
			}
		} catch (Throwable t) {
			throw new SpotterException("Can't see local bluetooth device "
					+ t.getMessage());
		}
	}
	public void close() {
	}

	protected long nextScanInterval() {
		return scanIntervalMillis;
	}
	
	protected void startOneScan(final EventSystem evs) {
		currentScan = new BluetoothScan() {
			public void gotReading(BluetoothReading br) {
				//System.err.println("bt got reading");
				BluetoothReading[] arr = new BluetoothReading[1];
				arr[0] = br;
				notifyGotMeasurement(evs, new BeaconMeasurement(System.currentTimeMillis(), arr));
			}
			public void scanDone() {
				//System.err.println("bt done");
				BluetoothSpotter.this.scanDone();
				currentScan = null;
			}
		};
		if(filterPhones)
			currentScan.setFilterPhones(true);
		
		if (!currentScan.start()) {
			currentScan = null;
			notifyGotException(evs, new SpotterException("could not initiate bluetooth scan"));
			return;
		}
	}
	protected void cancelScan() {
		if (currentScan == null) return;
		currentScan.cancel();
		currentScan = null;
	}
	protected Measurement getMeasurementImpl() throws SpotterException {
		final BeaconMeasurement meas=new BeaconMeasurement(System.currentTimeMillis());
		BluetoothScan scan = new BluetoothScan() {
			public void gotReading(BluetoothReading br) {
				meas.addReading(br);
			}
			public void scanDone() {
				synchronized(this) {
					this.notify();
				}
			}
		};
		if(filterPhones)
			scan.setFilterPhones(true);

		
		if (!scan.start()) {
			throw new SpotterException("could not initiate bluetooth scan");
		}
		boolean done=false;
		while (!done) {
			try {
				synchronized(scan) {
					scan.wait();
				}
				done = true;
			} catch (InterruptedException e) {
			}
		}
		return meas;
	}
}
