package org.placelab.client.tracker;

import org.placelab.core.GPSMeasurement;
import org.placelab.core.Measurement;
import org.placelab.core.PositionMeasurement;
import org.placelab.core.TwoDCoordinate;

/**
 * A Tracker that takes {@link PositionMeasurement} objects and returns
 * the latest measurement as its estimate of position.
 */
public class PositionTracker extends Tracker {
	private PositionMeasurement mostRecent=new GPSMeasurement(0L, TwoDCoordinate.NULL);
	
	public String toString() {
		return "Global Positioning System (GPS)";
	}
	
	public void updateEstimateImpl(Measurement m) {
		if (m instanceof PositionMeasurement) {
			mostRecent=(PositionMeasurement)m;
		}
	}
	
	public Estimate getEstimate() {
		Estimate rv = new TwoDPositionEstimate(mostRecent.getTimestamp(), (TwoDCoordinate)mostRecent.getPosition(), 0.0);
		return rv;
	}

	public boolean acceptableMeasurement(Measurement m) {
		return (m instanceof PositionMeasurement);
	}
	public void updateWithoutMeasurement(long durationMillis) {
		//doesn't move
	}
	protected void resetImpl() {
		mostRecent=new GPSMeasurement(0L, TwoDCoordinate.NULL);
	}
}
