/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.client.tracker;

import org.placelab.collections.LinkedList;
import org.placelab.collections.List;
import org.placelab.core.Measurement;

/**
 * A Tracker takes {@link Measurement} objects and then uses 
 * them to produce an {@link Estimate}.  A Measurement is the
 * result of the sensory operation, and the Tracker is the brain
 * that turns the sensation into a location estimate.
 */
public abstract class Tracker {
	private List listeners = new LinkedList();
	private int numMeasurements = 0;
	private long lastUpdated = 0L;

	/**
	 * Subclasses should implement this to do the actual work of
	 * updating their {@link Estimate} with the given {@link Measurement}.
	 * @param m use this to update your {@link Estimate}
	 */
	protected abstract void updateEstimateImpl(Measurement m);

	/**
	 * @return The Tracker's latest Estimate
	 */
	public abstract Estimate getEstimate();

	/**
	 * Returns true if the tracker can make use of this reading to meaningfully
	 * update the estimate.
	 */
	public abstract boolean acceptableMeasurement(Measurement m);

	/**
	 * This method notifies a tracker that time has elapsed without a new
	 * mesurement. This gives the tracker a chance to update its estimate to
	 * account for predicted motion.
	 */
	public abstract void updateWithoutMeasurement(long timeSinceMeasurementMillis);

	/** Subclasses should implement this to reset themselves to their initial state. */
	protected abstract void resetImpl();
	
	/** This method resets the tracker to its initial state. */
	public void reset() {
		lastUpdated = 0L;
		numMeasurements = 0;
		resetImpl();
	}
	
	/**
	 * @return the time that the Tracker was last asked to update, either with
	 * or without a {@link Measurement}
	 */
	public long getLastUpdatedTime() {
		return lastUpdated;
	}

	/**
	 * Register an EstimateListener to be notified when this tracker is updated.
	 * Trackers may have multiple EstimateListeners registered and all of them will
	 * be notified when the Tracker has a new {@link Estimate}.
	 * @param listener an object to be notified when the Tracker has a new {@link Estimate} 
	 */
	public void addEstimateListener(EstimateListener listener) {
		if (listeners.contains(listener)) {
			throw new IllegalArgumentException("Can't add the same listener "
					+ "twice to the same tracker");
		}
		listeners.add(listener);
	}

	public void removeEstimateListener(EstimateListener listener) {
		if (!listeners.contains(listener)) {
			throw new IllegalArgumentException("Can't remove a listener "
					+ "that is not on the tracker");
		}
		listeners.remove(listener);
	}

	/**
	 * Called to give the tracker a new measurement.
	 * @param m A measurement from a Spotter.
	 * @see #acceptableMeasurement(Measurement)
	 */
	public void updateEstimate(Measurement m) {
		if (m == null) {
			return;
		}
		//System.out.println("Tracker.java: updateEstimate called with "+m);
		// If this measurement has a new timestamp, erase our old average
		if (lastUpdated < m.getTimestamp()) {
			lastUpdated = m.getTimestamp();
		}
		updateEstimateImpl(m);
		numMeasurements++;
		fireEstimateUpdate(m);
	}

	/**
	 * When subclasses have a new estimate they can use this to notify their
	 * {@link EstimateListener} objects.
	 * @param m the most recent {@link Measurement} influencing the update or
	 * <code>null</code> if updateWithoutMeasurement was called
	 * @see #updateWithoutMeasurement(long) 
	 */
	protected void fireEstimateUpdate(Measurement m) {
		Estimate e = getEstimate();
		for (int i = 0; i < listeners.size(); ++i) {
			EstimateListener l = (EstimateListener) listeners.get(i);
			l.estimateUpdated(this, e, m);
		}
	}

	/**
	 * @return the number of {@link Measurement} objects passed to
	 * updateEstimate since the creation, or reset, of the Tracker.
	 * @see #updateEstimate(Measurement)
	 * @see #reset()
	 */
	public int numberOfMeasurements() {
		return numMeasurements;
	}
	
}