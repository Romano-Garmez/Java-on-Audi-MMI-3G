package org.placelab.client.tracker;

import org.placelab.core.Measurement;
import org.placelab.core.TwoDCoordinate;


/**
 * This simple smoother creates a reading that is a mix of the last 2
 * Estimates produced by a wrapped Tracker. 
 * X and Y will not change by more than a fixed percentage, nor
 * will the error if the Estimate has one.
 */
public class SmoothingTracker extends Tracker implements EstimateListener {
	private Tracker smoothedTracker;

	private TwoDPositionEstimate currentEstimate = null;

	private double distanceThreshold = 1000; // meters

	private double percentNew = 0.02;

	private double deviationThreshold = 500; // meters

	private double percentNewDeviation = 0.02;

	private double minDeviation = 30; // meters

	public SmoothingTracker(Tracker t) {
		smoothedTracker = t;
		t.addEstimateListener(this);
	}

	public String toString() {
		return smoothedTracker.toString();
	}
	
	public Tracker getTracker() {
		return smoothedTracker;
	}

	/**
	 * Sets the threshold at which a new Estimate from the wrapped
	 * Tracker will have its coordinate used without being smoothed with a previous
	 * Estimate's coordinate.  The default is 1000 meters.
	 * @see #setDeviationThreshold(double)
	 */
	public void setDistanceThreshold(double newThreshold) {
		distanceThreshold = newThreshold;
	}

	/**
	 * Sets the percentage by which the Estimate's coordinate produced by this SmoothingTracker
	 * will change from its last Estimate in light of a new Estimate produced by
	 * its wrapped Tracker.  For instance, if newRatio is .5, then the new Estimate
	 * is produced in the following way:
	 * <code>newLat = previousLat + (incomingEstimateLat * .5)</code>
	 * and the same for the longitude.
	 * @see #setNewToOldDeviationRatio(double)
	 */
	public void setNewToOldRatio(double newRatio) {
		percentNew = newRatio;
	}

	/**
	 * Sets the threshold at which a new Estimate from the wrapped Tracker
	 * will have its standard deviation used without being smoothed with
	 * a previous Estimate's standard deviation.  The default is 500 meters.
	 * @see #setDistanceThreshold(double)
	 */
	public void setDeviationThreshold(double newThreshold) {
		deviationThreshold = newThreshold;
	}

	/**
	 * Sets the percentage by which the Estimate's standard deviation produced 
	 * by this SmoothingTracke will change from its last Estimate in light of 
	 * a new Estimate produced by its wrapped Tracker
	 */
	public void setNewToOldDeviationRatio(double newRatio) {
		percentNewDeviation = newRatio;
	}

	/**
	 * Sets the minimum standard deviation an Estimate produced by this
	 * SmoothingTracker will have.
	 */
	public void setMinDeviation(double newMin) {
		minDeviation = newMin;
	}

	protected void updateEstimateImpl(Measurement m) {
		//smoothedTracker.updateEstimate(m);
	}
	
	public void updateEstimate(Measurement m) {
	    smoothedTracker.updateEstimate(m);
	    super.updateEstimate(m);
	}

	public Estimate getEstimate() {
		if (currentEstimate == null) {
			return new TwoDPositionEstimate(getLastUpdatedTime(),
					TwoDCoordinate.NULL, 0.0);
		} else {
			return currentEstimate;
		}
	}

	public boolean acceptableMeasurement(Measurement m) {
		return smoothedTracker.acceptableMeasurement(m);
	}

	public void updateWithoutMeasurement(long timeSinceMeasurementMillis) {
		smoothedTracker.updateWithoutMeasurement(timeSinceMeasurementMillis);
	}

	protected void resetImpl() {
		smoothedTracker.reset();
	}

	public void estimateUpdated(Tracker t, Estimate e, Measurement m) {
		double newLat, newLon;
		double newDeviation = 0.0;
		TwoDPositionEstimate est = (TwoDPositionEstimate) e;
		// take the new estimate if its too far off from the last
		if ((currentEstimate == null) || (currentEstimate.getCoord().isNull())
				|| (currentEstimate.getTwoDPosition().distanceFrom(
						est.getTwoDPosition()) > distanceThreshold)) {
			newLat = est.getTwoDPosition().getLatitude();
			newLon = est.getTwoDPosition().getLongitude();
		} else {
			newLat = (1.0 - percentNew) * currentEstimate.getTwoDPosition().getLatitude()
					+ percentNew * est.getTwoDPosition().getLatitude();
			newLon = (1.0 - percentNew) * currentEstimate.getTwoDPosition().getLongitude()
					+ percentNew * est.getTwoDPosition().getLongitude();
		}

		// smooth the error as well
		if ((currentEstimate == null) || (currentEstimate.getCoord().isNull())
				|| (Math.abs(currentEstimate.getStdDev() - est.getStdDev()) > deviationThreshold)) {
			newDeviation = est.getStdDev();
		} else {
			newDeviation = (1.0 - percentNewDeviation) * currentEstimate.getStdDev()
					+ percentNewDeviation * est.getStdDev();
		}
		
		if (newDeviation < minDeviation) {
			newDeviation = minDeviation;
		}
		currentEstimate = new TwoDPositionEstimate(getLastUpdatedTime(), 
				new TwoDCoordinate(newLat, newLon), newDeviation);
	}

}