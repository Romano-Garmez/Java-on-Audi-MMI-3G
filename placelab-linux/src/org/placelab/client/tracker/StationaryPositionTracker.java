package org.placelab.client.tracker;

import org.placelab.core.Measurement;
import org.placelab.core.TwoDCoordinate;

/**
 * 
 * A sample tracker that always returns the same position as its estimate
 */
public class StationaryPositionTracker extends Tracker {
	private TwoDCoordinate position;
	
	public StationaryPositionTracker(TwoDCoordinate coord) {
		super();
		position = new TwoDCoordinate(coord);
	}
	public void updateEstimateImpl(Measurement IGNORED) {
		//should never be called
		throw new IllegalArgumentException("Should never call "+
			"update estimate of StationaryPositionTracker");
	
	}
	public Estimate getEstimate() {
		return new TwoDPositionEstimate(System.currentTimeMillis(), position, 0.0);
	}

	public boolean acceptableMeasurement(Measurement m) {
		//ignores measurements
		return false;
	}

	public void updateWithoutMeasurement(long durationMillis) {
		//doesn't move
	}

	protected void resetImpl() {
		//does nothing
	}
}
