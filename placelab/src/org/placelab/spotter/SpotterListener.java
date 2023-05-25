/*
 * Created on 23-Jun-2004
 *
 */
package org.placelab.spotter;

import org.placelab.core.Measurement;

/**
 * SpotterListener is the basic interface for receiving callbacks
 * from asynchronous Spotter operations.  Callbacks are sent either
 * in the Spotter's thread, which may not be the same as the thread
 * in which the Spotter was started, or using the 
 * {@link org.placelab.eventsystem.EventSystem} if
 * one is registered with the Spotter.
 */
public interface SpotterListener {
	public void gotMeasurement(Spotter sender, Measurement m);
	public void spotterExceptionThrown(Spotter sender, SpotterException ex);
}
