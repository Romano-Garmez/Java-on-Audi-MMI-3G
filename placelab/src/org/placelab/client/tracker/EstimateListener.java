/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.client.tracker;

import org.placelab.core.Measurement;

/**
 * An EstimateListener can be registered with a {@link Tracker} to be
 * notified when the Tracker updates.
 */
public interface EstimateListener {
    /**
     * Implement this method to respond to updates from the Tracker(s)
     * you are registered with.
     * @param t the Tracker producing the Estimate
     * @param e the Estimate produced by the Tracker
     * @param m the latest Measurement used by the Tracker to produce the Estimate
     */
	public void estimateUpdated(Tracker t, Estimate e, Measurement m);
}
