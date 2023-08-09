/*
 * Created on Jun 28, 2004
 *
 */
package org.placelab.client.tracker;

import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.Coordinate;
import org.placelab.core.Measurement;
import org.placelab.mapper.Beacon;
import org.placelab.mapper.Mapper;
import org.placelab.mapper.NeighborhoodBeacon;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.SpotterListener;

/**
 * This is the base class for all Beacon-based trackers.
 * 
 * It includes methods to look up {@link org.placelab.mapper.Beacon}
 * objects in a {@link org.placelab.mapper.Mapper} given a unique 
 * identifier for the Beacon.
 * <p>
 * Because a Beacon's unique identifier may not be truly unique, there is
 * a need to choose a Beacon from a set of Beacons that match the unique
 * identifier given a current location and/or the surrounding neighborhood
 * of Beacons.  Utility methods are provided to meet this need.
 * 
 * 
 *
 */
public abstract class BeaconTracker extends Tracker implements SpotterListener {
	private Mapper mapper;

	public BeaconTracker(Mapper m) {
		mapper = m;
	}
	/**
	 * Gets the {@link org.placelab.mapper.Mapper} used by this BeaconTracker
	 * to map Beacon ids to {@link org.placelab.mapper.Beacon} objects.
	 */
	public Mapper getMapper() { return mapper; }

	/** @param m return <code>true</code> if this is a {@link org.placelab.core.BeaconMeasurement} */
	public boolean acceptableMeasurement(Measurement m) {
		return (m instanceof BeaconMeasurement);
	}
	
	public static final long WIFI_MAX_DISTANCE=5000; /* meters */ 


	/**
	 * Like {@link #findBeacon(String, BeaconMeasurement, Coordinate, long)} but uses
	 * the default maxDistance of 500 meters which corresponds to WiFi beacons.
	 * @see #findBeacon(String, BeaconMeasurement, Coordinate, long)
	 */
	public Beacon findBeacon(String uniqueId, BeaconMeasurement meas, Coordinate currentPosition) {
		return findBeacon(uniqueId, meas, currentPosition, WIFI_MAX_DISTANCE);
	}
	
	/**
	 * Given the unique id for a {@link org.placelab.mapper.Beacon} find the most likely
	 * {@link org.placelab.mapper.Beacon} in this BeaconTracker's mapper given the 
	 * neighborhood of surrounding Beacons in a {org.placelab.core.BeaconMeasurement} and 
	 * the current position.  It is necessary to specify these things since MAC address 
	 * spoofing and manufacturing errors can make the unique ids less than perfectly unique.
	 * @param uniqueId the unique identifier for the Beacon you
	 * wish you look up.
	 * @param meas of which the beacon you are trying to look up is a part.
	 * @param currentPosition your best guess as to the current location
	 * @param maxDistance the furthest distance that Beacons of this type can expect to be heard
	 * for instance, for WiFi, 500 meters is a good approximation.
	 * @return the most likely Beacon for the given address, BeaconMeasurement, 
	 * and current position 
	 */
	public Beacon findBeacon(String uniqueId, BeaconMeasurement meas, Coordinate currentPosition,
			long maxDistance) {
		if (uniqueId == null) {
			return null;
		}
		
		LinkedList beacons = mapper.findBeacons(uniqueId);
		return pickBeacon(beacons, meas, currentPosition, maxDistance);
	}
	
	/**
	 * Like {@link #pickBeacon(LinkedList, BeaconMeasurement, Coordinate, long)} but uses
	 * the default maxDistance of 500 meters which corresponds to WiFi beacons.
	 */
	public static Beacon pickBeacon(LinkedList beacons, BeaconMeasurement meas, Coordinate currentPosition) {
		return pickBeacon(beacons, meas, currentPosition, WIFI_MAX_DISTANCE);
	}
	
	/**
	 * A convenience method for choosing the most likely beacon from a list of beacons
	 * returned from a {@link org.placelab.mapper.Mapper}.  Generally, you will just
	 * use the instance method findBeacon, rather than going here.
	 * @param beacons the list of beacons
	 * @param meas the measurement of which the beacon is a part (the neighborhood)
	 * @param currentPosition your best guess for the current position
	 * @param maxDistance the furthest distance that Beacons of this type can expect to be heard
	 * for instance, for WiFi, 500 meters is a good approximation.
	 * @return the most likely Beacon out of the list for the given address, BeaconMeasurement, 
	 * and current position 
	 */
	public static Beacon pickBeacon(LinkedList beacons, BeaconMeasurement meas, Coordinate currentPosition,
			long maxDistance) {
		if (beacons==null) return null;
		if (meas == null && currentPosition == null) {
		    return beacons.size() == 0 ? null : (Beacon) beacons.getFirst();
		}
		if (beacons.size() == 1) {
			return (Beacon) beacons.getFirst();
		}
		
		/* find the set of beacons for which there is an intersection with the neighborhood
		 * of this measurement
		 */
		Beacon closest = null;
		long closestDistance = (maxDistance > 0 ? maxDistance+1 : 0);
		for (Iterator it = beacons.iterator(); it.hasNext(); ) {
			Beacon beacon = (Beacon) it.next();
			if(beacon == null) continue;			
			if(beacon.getPosition().isNull()) continue;
			
			if (meas != null && !doesIntersect(meas, beacon)) continue;
			if (currentPosition == null || currentPosition.isNull()) return beacon;
			
			long distance = currentPosition.distanceFromInMeters(beacon.getPosition());
			if (maxDistance <= 0) {
				if (closest == null || distance < closestDistance) {
					closest = beacon;
					closestDistance = distance;
				}
			} else {
				if (distance > maxDistance) {
					continue;
				}
				if (distance < closestDistance) {
					closest = beacon;
					closestDistance = distance;
				}
			}
		}
		return closest;		
	}
	
	private static boolean doesIntersect(BeaconMeasurement meas, Beacon beacon) {
	    if(!(beacon instanceof NeighborhoodBeacon)) return true;
	    String[] nbrs = ((NeighborhoodBeacon)beacon).getNeighborhood();
		if (nbrs==null || nbrs.length==0) return true;
		
		for (int i=0; i < meas.numberOfReadings(); i++) {
			for (int j=0; j < nbrs.length; j++) {
				if (meas.getReading(i).getId().equals(nbrs[j])) return true;
			}
		}
		return false;
	}
	
	public void gotMeasurement(Spotter sender, Measurement m) {
	    updateEstimate(m);
	}
	
	public void spotterExceptionThrown(Spotter s,SpotterException ex) {
		ex.printStackTrace();
	}
}
