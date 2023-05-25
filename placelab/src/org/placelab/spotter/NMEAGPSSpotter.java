package org.placelab.spotter;

import org.placelab.collections.UnsupportedOperationException;
import org.placelab.core.GPSMeasurement;
import org.placelab.core.Measurement;
import org.placelab.eventsystem.EventSystem;

/**
 * GPS devices which use some form of NMEA should use this class.
 * The NMEAGPSSpotter uses the NMEASentenceGatherer which uses 
 * $GPGGA and $GPRMC sentences to gather location and fix data 
 * and thereby construct its GPSMeasurements.
 */
public abstract class NMEAGPSSpotter extends AbstractSpotter {
	private boolean isScanning=false;
	private EventSystem eventSystem=null;
	private NMEASentenceGatherer gatherer=null;
	
	public void startScanning() {
		startScanning(null);
	}
	public void startScanning(EventSystem evs) {
		if (isScanning) {
			throw new UnsupportedOperationException("a continuous scan operation is already in progress");
		}
		eventSystem = evs;
		isScanning = true;
		gatherer = new NMEASentenceGatherer() {
			public void measurementAvailable(GPSMeasurement m) {
				NMEAGPSSpotter.this.measurementAvailable(m);
			}
		};
		startScanningImpl();
	}
	public void stopScanning() {
		if (!isScanning) return;
		
		stopScanningImpl();
		isScanning = false;
		eventSystem = null;
		gatherer = null;
	}
	public Measurement getMeasurement() throws SpotterException {
		// the array below is a hack to allow us to set the measurement in the anonymous inner class below
		final Measurement[] meas = new Measurement[1];
		final SpotterException[] ex = new SpotterException[1];
		ex[0] = null;
		
		SpotterListener listener = new SpotterListener() {
			public void gotMeasurement(Spotter s, Measurement m) {
				meas[0] = m;
				stopScanning();
				synchronized(this) {
					this.notify();
				}
			}
			public void spotterExceptionThrown(Spotter s,SpotterException e) {
				ex[0] = e;
				stopScanning();
				synchronized(this) {
					this.notify();
				}
			}
		};
		this.addListener(listener);
		startScanning();
		synchronized(listener) {
			try { listener.wait(); } catch(InterruptedException e) {}
		}
		this.removeListener(listener);
		if (ex[0] != null) throw ex[0];
		return meas[0];
	}
	
	/**
	 * Begin collecting and processing sentences from the gps device,
	 * handing them off to the {@link #lineAvailable(String)} method
	 * as they are read in.
	 */
	protected abstract void startScanningImpl();
	/**
	 * Stop collecting sentences from the gps device
	 */
	protected abstract void stopScanningImpl();
	
	/**
	 * Send a sentence, complete with checksum, to the gps device.
	 * Neither newline nor carraige return should be included.
	 */
	public abstract void sendASentence(String sentence);

	/**
	 * Subclasses of NMEAGPSSpotters should call this method when they
	 * have read a complete line from the gps device.  The line should
	 * not include carraige return or newline.
	 */
	protected void lineAvailable(String line) {
		if(gatherer == null) {
			gatherer = new NMEASentenceGatherer() {
				public void measurementAvailable(GPSMeasurement m) {
					NMEAGPSSpotter.this.measurementAvailable(m);
				}
			};

		}
		gatherer.lineAvailable(line);
	}
	
	protected void measurementAvailable(GPSMeasurement m) {
		notifyGotMeasurement(eventSystem, m);
	}
}
