package org.placelab.client.tracker;

import org.placelab.core.BeaconMeasurement;
import org.placelab.core.BeaconReading;
import org.placelab.core.Measurement;
import org.placelab.core.ThreeDCoordinate;
import org.placelab.core.TwoDCoordinate;
import org.placelab.core.Types;
import org.placelab.mapper.Beacon;
import org.placelab.mapper.Mapper;
import org.placelab.mapper.WiFiBeacon;

/**
 * A simple tracker that computes the geometric center of the current set of
 * readings.  
 * 
 * Right now, this is the best general purpose Tracker for WiFi
 * because it produces the most accurate Measurements.
 */
public class CentroidTracker extends BeaconTracker {	
	private Estimate estimate;

	
	public CentroidTracker(Mapper m) { 
		super(m);
	}
	
	public Estimate getEstimate() {
		
		if (estimate != null) {
			return estimate;
		} else if(System.getProperty(Types.COORDPROPERTY) != null && System.getProperty(Types.COORDPROPERTY).equals(Types.THREEDCOORDINATE)) {
			Estimate e = new ThreeDPositionEstimate(getLastUpdatedTime(), ThreeDCoordinate.NULL, 0.0);
			return e;
		}
		else {
			Estimate e = new TwoDPositionEstimate(getLastUpdatedTime(), TwoDCoordinate.NULL, 0.0);
			return e;
		}
	}

	public void reset() {
		estimate = null;
	}
	
	/**
	 * updateEstimateImpl uses the passed measurement to compute a simple
	 * geometic center. 
	 */
	public void updateEstimateImpl(Measurement m) {

		// we only want BeaconMeasurements
		if (!(m instanceof BeaconMeasurement)) {
			return;
		}

		//Determine coorinate system
		if(System.getProperty(Types.COORDPROPERTY) != null && System.getProperty(Types.COORDPROPERTY).equals(Types.THREEDCOORDINATE)) {
			updateEstimateImpl3D(m);
		}
		else {
			updateEstimateImpl2D(m);
		}
    }

	private void updateEstimateImpl2D(Measurement m) {
		BeaconMeasurement meas = (BeaconMeasurement) m;

		double totalLat=0.0, totalLon=0.0;
		int count=0;
		for (int i=0; i < meas.numberOfReadings(); i++) {
			BeaconReading reading = meas.getReading(i);
			Beacon beacon =  findBeacon(reading.getId(), meas, 
					(estimate==null ? null: ((TwoDPositionEstimate)estimate).getTwoDPosition()));
			if ((estimate != null) && (beacon != null) && (beacon instanceof WiFiBeacon) &&
				(beacon.getPosition().distanceFromInMeters(estimate.getCoord()) > WIFI_MAX_DISTANCE)) {
				beacon = null;
			}
//			System.out.println("beacon is " + beacon + " " + reading.getId() + " ");
			if (beacon == null) {
				continue;
			}
			TwoDCoordinate pos = (TwoDCoordinate)beacon.getPosition();
			totalLat += pos.getLatitude();
			totalLon += pos.getLongitude();
			count++;
		}

		if (count == 0) { // keep the same estimate as before
			return;
		}
		
		TwoDCoordinate mean = new TwoDCoordinate(totalLat/count, totalLon/count);
		double totalDistanceSq = 0.0;
		for (int i=0; i < meas.numberOfReadings(); i++) {
			BeaconReading reading = meas.getReading(i);
			Beacon beacon = findBeacon(reading.getId(), meas,
					(estimate==null ? null: ((TwoDPositionEstimate)estimate).getTwoDPosition()));
			if (beacon == null) continue;
			
			TwoDCoordinate pos = (TwoDCoordinate) beacon.getPosition();
			double x = pos.xDistanceFrom(mean), y = pos.yDistanceFrom(mean);
			double distanceSq = x*x + y*y;
			totalDistanceSq += distanceSq;
		}
		
		double stdDev;
		if (count > 1) {
			stdDev = Math.sqrt(totalDistanceSq/(count - 1));
		} else {
			stdDev = 0.0;
		}
		estimate = new TwoDPositionEstimate(getLastUpdatedTime(), mean, stdDev);
	}

	private void updateEstimateImpl3D(Measurement m) {
		BeaconMeasurement meas = (BeaconMeasurement) m;
		double totalLat=0.0, totalLon=0.0, totalElv=0.0;
		int count=0;
		for (int i=0; i < meas.numberOfReadings(); i++) {
			BeaconReading reading = meas.getReading(i);
			Beacon beacon = findBeacon(reading.getId(), meas, 
					(estimate==null ? null: ((ThreeDPositionEstimate)estimate).getThreeDPosition()));
			if (beacon == null) {
				continue;
			}
			ThreeDCoordinate pos = (ThreeDCoordinate)beacon.getPosition();
			totalLat += pos.getLatitude();
			totalLon += pos.getLongitude();
			totalElv += pos.getElevation();
			count++;
		}
		
		if (count == 0) {
			return;
		}
		
		ThreeDCoordinate mean = new ThreeDCoordinate(totalLat/count, totalLon/count, totalElv/count);
		double totalDistanceSq = 0.0;
		for (int i=0; i < meas.numberOfReadings(); i++) {
			BeaconReading reading = meas.getReading(i);
			Beacon beacon = findBeacon(reading.getId(), meas,
					(estimate==null ? null: ((ThreeDPositionEstimate)estimate).getThreeDPosition()));
			if (beacon == null) continue;
			
			ThreeDCoordinate pos = (ThreeDCoordinate) beacon.getPosition();
			double x = pos.xDistanceFrom(mean), y = pos.yDistanceFrom(mean), z = pos.zDistanceFrom(mean);
			double distanceSq = x*x + y*y + z*z;
			totalDistanceSq += distanceSq;
		}
		
		double stdDev;
		if (count > 1) {
			stdDev = Math.sqrt(totalDistanceSq/(count - 1));
		} else {
			stdDev = 0.0;
		}
		
		estimate = new ThreeDPositionEstimate(getLastUpdatedTime(), mean, stdDev);
	}

	/**
	 * @param m returns <code>true</code> if this is a BeaconMeasurement
	 */
	public boolean acceptableMeasurement(Measurement m) {
		return (m instanceof BeaconMeasurement);
	}

	public void updateWithoutMeasurement(long durationMillis) {
	}

	protected void resetImpl() {
		estimate = null;
	}
}
