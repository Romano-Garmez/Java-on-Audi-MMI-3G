package org.placelab.client.tracker;

import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.core.Measurement;
import org.placelab.core.Types;

/**
 * A CompoundTracker encapsulates multiple {@link Tracker} objects
 * in a single Tracker.  
 * This is useful primarily for comparing multiple Trackers against
 * one another.  For instance, using a CompoundTracker with a
 * {@link org.placelab.demo.mapview.TrackedMapView} will produce multiple position reticles
 * on the map, one for each Tracker in the CompoundTracker. 
 */
public class CompoundTracker extends Tracker {
	private LinkedList trackers;
	
	public CompoundTracker() {
		trackers = new LinkedList();
	}

	/**
	 * Adds a {@link Tracker} to the CompoundTracker
	 * All trackers added will be forwarded all requests to
	 * update, either with or without Measurement.
	 * @param t the {@link Tracker} to add
	 */
	public void addTracker(Tracker t) {
		trackers.add(t);
	}
	
	/**
	 * Gets a list of all Trackers in this CompoundTracker
	 */
	public LinkedList getTrackers() {
		return trackers;
	}
	
	/**
	 * Removes all the Trackers from the CompoundTracker.  The Trackers
	 * will not be reset and may continue to be used elsewhere, but they
	 * will no longer be updated with by the CompoundTracker and their estimates
	 * will no longer be included in the CompoundTracker's {@link CompoundEstimate}
	 * results.
	 */
	public void clearTrackers() {
		trackers.clear();
	}

	HashMap badAPs = new HashMap();
	
	public void updateEstimateImpl(Measurement m) {
		for (Iterator it = trackers.iterator(); it.hasNext();) {
			Tracker t = (Tracker)it.next();
			t.updateEstimate(m);
		}
	}
	
	public Estimate getEstimate() {
		if (trackers.size() > 0) {
			Iterator it = trackers.iterator();
			Tracker t = (Tracker) it.next();
			CompoundEstimate rv = new CompoundEstimate(t.getEstimate());
			while (it.hasNext()) {
				t = (Tracker) it.next();
				rv.addEstimate(t.getEstimate());
			}
			return rv;
		} else {
			return new CompoundEstimate(Types.newEstimate(0L,Types.newCoordinate(),"0.0"));
		}
	}

	public boolean acceptableMeasurement(Measurement m) {
		for (Iterator it = trackers.iterator(); it.hasNext();) {
			Tracker t = (Tracker)it.next();
			if (t.acceptableMeasurement(m)) {
				return true;
			}
		}
		return false;
	}

	public void updateWithoutMeasurement(long durationMillis) {
		for (Iterator it = trackers.iterator(); it.hasNext();) {
			Tracker t = (Tracker)it.next();
			t.updateWithoutMeasurement(durationMillis);
		}
	}

	public void resetImpl() {
		for (Iterator it = trackers.iterator(); it.hasNext();) {
			Tracker t = (Tracker)it.next();
			t.reset();
		}
	}
}
