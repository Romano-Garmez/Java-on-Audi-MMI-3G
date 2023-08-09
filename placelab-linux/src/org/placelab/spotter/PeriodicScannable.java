package org.placelab.spotter;

/**
 * Spotters can implement this interface to allow users to set a minimum
 * scanning interval for them to use.
 */
public interface PeriodicScannable {
    /**
     * Sets the minimum amount of time between
     * {@link SpotterListener#gotMeasurement(Spotter, Measurement)}
     * callbacks to listeners.
     */
	public void setPeriodicScanInterval(long intervalMillis);
	
	
	public long getPeriodicScanInterval();
}
