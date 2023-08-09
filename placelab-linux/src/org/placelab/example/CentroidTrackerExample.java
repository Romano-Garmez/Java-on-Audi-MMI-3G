package org.placelab.example;

import org.placelab.client.tracker.Estimate;
import org.placelab.client.tracker.Tracker;
import org.placelab.client.tracker.TwoDPositionEstimate;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.BeaconReading;
import org.placelab.core.Measurement;
import org.placelab.core.TwoDCoordinate;
import org.placelab.mapper.Beacon;
import org.placelab.mapper.CompoundMapper;
import org.placelab.mapper.Mapper;
import org.placelab.mapper.WiFiBeacon;
import org.placelab.spotter.LogSpotter;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.WiFiSpotter;

/**
 * This sample is a tracker that calculates the centroid of the observed
 * readings.
 * 
 * This tracker estimates the device position to be the geometric center of the
 * readings that have the same timestamp.
 */
public class CentroidTrackerExample extends Tracker {
	/** The mapper to query for information about beacons */
	private Mapper mapper;
	
	private TwoDPositionEstimate estimate;
	
	public CentroidTrackerExample(Mapper m) { 
		mapper = m;
	}

	/** return an estimate based on the last set of measurements we saw * */
	public Estimate getEstimate() {
		if (estimate != null) {
			return estimate;
		} else {
			return new TwoDPositionEstimate(getLastUpdatedTime(), TwoDCoordinate.NULL, 0.0);
		}
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
		BeaconMeasurement meas = (BeaconMeasurement) m;

		/* calculate the mean */
		double totalLat=0.0, totalLon=0.0;
		int count=0;
		for (int i=0; i < meas.numberOfReadings(); i++) {
			BeaconReading reading = meas.getReading(i);
			Beacon beacon = mapper.findBeacon(reading.getId());
			if (beacon == null) continue;
			TwoDCoordinate pos = (TwoDCoordinate)((WiFiBeacon)beacon).getPosition();
			totalLat += pos.getLatitude();
			totalLon += pos.getLongitude();
			count++;
		}
		TwoDCoordinate mean = new TwoDCoordinate(totalLat/count, totalLon/count);
		
		/* you can also calculate the standard deviation here.  For this sample,
		 * let's ignore it.
		 */
		
		estimate = new TwoDPositionEstimate(getLastUpdatedTime(), mean, 0.0);
	}

	//** returns true if m is a wifi measurement **/
	public boolean acceptableMeasurement(Measurement m) {
		return (m instanceof BeaconMeasurement);
	}

	public void updateWithoutMeasurement(long durationMillis) {
	}

	protected void resetImpl() {
		estimate = null;
	}

	public static void main(String[] args) {
		try {
			Spotter spotter;
			Mapper mapper;

			// Create a mapper
			mapper = CompoundMapper.createDefaultMapper(true, true);
			
			// Create a new tracker
			Tracker t = new CentroidTrackerExample(mapper);
			Estimate e1, e2;

			// Create either a live spotter or a log spotter
			if (args.length == 0) {
				spotter = new WiFiSpotter();
			} else {
				spotter = LogSpotter.newSpotter(args[0]);
			}
			spotter.open();

			// Get readings from the spotter
			BeaconMeasurement m = (BeaconMeasurement)spotter.getMeasurement();
			System.out.println("The spotter saw " + m.numberOfReadings() + " readings.");
			t.updateEstimate(m);
			e1 = t.getEstimate();
			System.out.println("Estimated position: " + e1.getCoord());

			// wait around for 2 seconds
			Thread.sleep(2000);

			// Do it all again
			m = (BeaconMeasurement)spotter.getMeasurement();
			System.out.println("The spotter saw " + m.numberOfReadings() + " readings.");
			t.updateEstimate(m);
			e2= t.getEstimate();
			System.out.println("Estimated position: " + e2.getCoord().getLatitudeAsString()
					+ "," + e2.getCoord().getLongitudeAsString());

			
			// Calculate the distance between the 2 readings
			TwoDCoordinate c1 = (TwoDCoordinate)e1.getCoord(),
				c2 = (TwoDCoordinate)e2.getCoord();
			double distance = c1.distanceFrom(c2);
			System.out.println("The distance between estimates is: "
					+ (int) distance + " meters.");

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}