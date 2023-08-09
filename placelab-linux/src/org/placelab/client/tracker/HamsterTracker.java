/*
 * Created on Apr 30, 2004
 */
package org.placelab.client.tracker;

import org.placelab.core.Measurement;
import org.placelab.core.TwoDCoordinate;


/**
 * A Tracker that wraps another Tracker and makes it behave like a
 * Hamster in a ball.  This makes it possible to reduce jitter in
 * some Trackers.
 * That is, the wrapped Tracker must produce Estimates whose positions
 * lie outside the radius of a center circle in order to move the 
 * Estimate produced by the HamsterTracker.  When such an Estimate is
 * encountered, the HamsterTracker's Estimate is shifted by no more
 * than 50% of the distance to the new Estimate's location.
 * 
 */
public class HamsterTracker extends Tracker implements EstimateListener {
	private Tracker child;
	private TwoDCoordinate center;
	private double stddev;
	private double radius;
	
	public double RADIUS = 30;

	public HamsterTracker(Tracker insideTracker) {
		if (insideTracker == null)
			throw new NullPointerException();
		
		child = insideTracker;	
		child.addEstimateListener(this);
		
		reset();
	}
	
	public double getRadius() {
	    return RADIUS;
	}
	
	/**
	 * Sets the radius for which Estimates produced by the wrapped
	 * Tracker must fall away from the previous center to be incorporated
	 * into this HamsterTracker's Estimate.  Calling this method has
	 * the side effect of resetting the HamsterTracker, since the previous
	 * center will no longer be meaningful with a different radius.
	 */
	public void setRadius(double newRadius) {
	    this.RADIUS = newRadius;
	    this.reset();
	}
		
	
	protected void updateEstimateImpl(Measurement m) {
	    // nop, we use updateEstimate to tell the child to do updateEstimate which
	    // causes the child to then do updateEstimateImpl
	}
	
	public void updateEstimate(Measurement m) {
	    child.updateEstimate(m);
	    super.updateEstimate(m);
	}

	public Estimate getEstimate() {
		if (center == null) {
			return new TwoDPositionEstimate(getLastUpdatedTime(), TwoDCoordinate.NULL, 0.0);
		} else {
			/* XXX: ignoring stddev here */
			return new TwoDPositionEstimate(getLastUpdatedTime(), (TwoDCoordinate)center.createClone(), stddev);
		}
	}

	public boolean acceptableMeasurement(Measurement m) {
		return child.acceptableMeasurement(m);
	}


	public void updateWithoutMeasurement(long timeSinceMeasurementMillis) {
		child.updateWithoutMeasurement(timeSinceMeasurementMillis);
	}
	
	public String toString() {
		return child.toString();
	}
	
	public void estimateUpdated(Tracker t, Estimate e, Measurement m) {
	    if (! (e instanceof TwoDPositionEstimate)) return;
	    
	    if (e.getCoord().isNull()) {
	    	center = (TwoDCoordinate)e.getCoord();
	    	return;
	    }
	    
	    if ((center == null) || center.isNull()) {
	    	center = (TwoDCoordinate)e.getCoord();
	    	return;
	    }
		
		stddev = ((TwoDPositionEstimate)e).getStdDev();
		TwoDPositionEstimate est = (TwoDPositionEstimate) e;
		
		
		TwoDCoordinate coord = est.getTwoDPosition();
		
		// sometimes a tracker will return NaN, and this is very bad for the hamster
		// tracker, so ignore it  -fats
		if(coord.isNull()) return;
		
		if (center != null) {
			double d = center.distanceFrom(coord);
			
			if (d > radius) {
				double percent = (d - radius) / radius;
				
				// only jump half the radius at once.
				percent = Math.min(.5, percent);
				
				double x = coord.xDistanceFrom(center) * percent;
				double y = coord.yDistanceFrom(center) * percent;
				center.moveBy(x, y);
			}
		} else {
			center = est.getTwoDPosition();
		}
	}
	
	protected void resetImpl() {
		center = null;
		radius = RADIUS;
		stddev = 0.0;
	}
}
