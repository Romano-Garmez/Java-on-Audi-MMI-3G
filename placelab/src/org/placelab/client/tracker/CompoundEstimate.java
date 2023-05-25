package org.placelab.client.tracker;

import org.placelab.collections.LinkedList;
import org.placelab.collections.UnsupportedOperationException;
import org.placelab.core.Coordinate;

/**
 * CompoundEstimates are produced by {@link CompoundTracker} objects.
 * They contain all the {@link Estimate} objects produced by the Trackers
 * contained in the CompoundTracker.
 */
public class CompoundEstimate implements Estimate {
	private LinkedList estimates;
	private long timestamp;
	
	/**
	 * Typically, only the CompoundTracker will create CompoundEstimates
	 */
	public CompoundEstimate(Estimate e) {
		timestamp = e.getTimestamp();
		estimates = new LinkedList();
		estimates.add(e);
	}
	
	/**
	 * The CompoundTracker uses this to add Estimates.  This is not
	 * needed by consumers of the Estimate
	 */
	public void addEstimate(Estimate e) {
		estimates.add(e);
	}
	
	/**
	 * Get all the {@link Estimate} objects in this CompoundEstimate.
	 * The Estimates are returned in the order corresponding to the order
	 * in which the Trackers were added to the CompoundTracker.  So, 
	 * for instance, if you put a CentroidTracker and then a PositionTracker
	 * in the CompoundTracker, the CompoundEstimates produced would be
	 * a BeaconMeasurement and a PositionMeasurement, in that order.
	 */
	public LinkedList getEstimates() {
		return estimates;
	}

	/**
	 * Throws UnsupportedOperationException because CompoundEstimates don't have
	 * a position.  Instead, use {@link #getEstimates()} and call getPosition on
	 * each Estimate returned.
	 */
	public Coordinate getCoord() {
		throw new UnsupportedOperationException("You cannot invoke getPosition() on a CompoundEstimate");
	}

	/**
	 * Throws UnsupportedOperationException because CompoundEstimates don't have
	 * a position.  Instead, use {@link #getEstimates()} and call getStdDevAsString
	 * on each Estimate returned.
	 */
	public String getStdDevAsString() {
		throw new UnsupportedOperationException("You cannot invoke getStdDevString() on a CompoundEstimate");
	}

	/**
	 * Throws UnsupportedOperationException because CompoundEstimates don't have
	 * a position.  Instead, use {@link #getEstimates()} and call getStdDevInMeters
	 * on each Estimate returned.
	 */
	public int getStdDevInMeters() {
		throw new UnsupportedOperationException("You cannot invoke getStdDevInMeters() on a CompoundEstimate");
	}
	
    public long getTimestamp() {
        return timestamp;
    }

    public void construct(long timestamp, Coordinate position, String stdDevString) {
		throw new UnsupportedOperationException("You cannot invoke construct() on a CompoundEstimate");
                
    }
	
}
