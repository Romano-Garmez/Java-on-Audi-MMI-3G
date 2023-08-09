/*
 * Created on Jul 20, 2004
 *
 */
package org.placelab.spotter;

import org.placelab.core.Measurement;
import org.placelab.eventsystem.EventSystem;

/**
 * A spotter is a java object that generalizes the function of an environmental
 * sensor like a GPS unit or a WiFi card. A spotter produces 
 * {@link org.placelab.core.Measurement} objects. The
 * types of the measurements produced depend on the type of spotter.
 * <p>
 * Regardless of the underlying implementation of the Spotter, all Spotters
 * support the following three modes of operation:
 * <ul>
 * <li>Synchronous: Call {@link #getMeasurement} at application defined intervals
 * and get back a single Measurement for each call.  This mode of operation is
 * simple, but may result in excessive wait times while the method blocks to collect
 * measurements or it may result in missed Measurements if it is not called often
 * enough.
 * <li>Continous Scan: Call {@link #startScanning()} to have the Spotter begin
 * scanning for Measurements in the background, notifying its registered 
 * {@link SpotterListener} objects when new Measurements are available.  The
 * continous scan will continue providing Measurements until {@link #stopScanning()}
 * is called.
 * <li>Single Background Scan: Call {@link #scanOnce()} to have the Spotter
 * do a single background scan and return a Measurement to the registered
 * SpotterListeners.
 * </ul> 
 */
public interface Spotter {
	/** 
	 * Load resources used by the spotter.  Applications should be able to
	 * invoke this methods multiple times in sequence and have the "right" things happen.
	 * All Spotters should be opened before being used.
	 * @throws SpotterException if the necessary hardware or software for this spotter
	 * is not present
	 */
	public void open() throws SpotterException;
	
	/**
	 * Unloads resources used by the spotter.  Multiple calls to close are ok.
	 */
	public void close() throws SpotterException;
	
	/** 
	 * A blocking call to get a new Measurement.  A spotter implementation may choose to
	 * just return the last cached Measurement, or go ahead and do a new scan and return
	 * the results of that scan.  Note that the accuracy of timestamps returned by this
	 * usage model depends on the spotter implementation.  You should not call this method
	 * while this Spotter is performing a continous scan.  Doing so will result in a 
	 * SpotterException being thrown.
	 */
	public Measurement getMeasurement() throws SpotterException;
	
	/** 
	 * Start scanning for Measurements in the background and return the Measurements
	 * by notifying SpotterListeners with the 
	 * {@link SpotterListener#gotMeasurement(Spotter, Measurement)} method.  
	 * Depending on the Spotter implementation this
	 * may start up a new background thread.  Callbacks to the SpotterListener are
	 * not guaranteed to (and probably won't) be in the the same thread as 
	 * startScanning was called in.
	 * @see #startScanning(EventSystem)
	 */
	public void startScanning();
	
	/** 
	 * Start scanning for Measurements.  This method may or may not create a new thread
	 * internally, but should never expose that thread to the application.  Instead
	 * callbacks to SpotterListeners should be invoked via the Eventsystem.notifyTransientEvent()
	 * method, so that the callbacks get run through the EventSystem thread.
	 * @see #startScanning()
	 */
	public void startScanning(EventSystem evs);
	
	/** 
	 * Stops a currently running background scan started from either scanOnce or
	 * startScanning.
	 */
	public void stopScanning();

	/**
	 * Performs a single background scan for Measurements and returns the result
	 * to registered SpotterListeners' 
	 * {@link SpotterListener#gotMeasurement(Spotter, Measurement)} method.
	 * This may start a new thread and deliver the callback in that new thread.
	 * <p>
	 * Registered SpotterListeners that also implement the ScanOnceListener interface
	 * will also receive {@link ScanOnceListener#endOfScan(Spotter)} notifications.
	 * @see #scanOnce(EventSystem)
	 */
	public void scanOnce();	
	
	/**
	 * Like {@link #scanOnce()} but instead uses the given EventSystem to deliver the
	 * callback to hide the background thread from the application.
	 */
	public void scanOnce(EventSystem evs);	

	/**
	 * Adds a SpotterListener to be called back whenever new Measurements are generated
	 * by the Spotter.
	 */
	public void addListener(SpotterListener listener);
	public void removeListener(SpotterListener listener);
	
}
