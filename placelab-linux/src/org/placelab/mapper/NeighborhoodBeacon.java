package org.placelab.mapper;

/**
 * NeighborhoodBeacons know the unique id's of the other
 * Beacons that were also seen when they were sighted.
 * This allows for increased accuracy through fingerprinting.
 */
public interface NeighborhoodBeacon {
    /**
     * Gets an array of unique ids for the neighboring
     * Beacons seen in the same scan as this Beacon.
     * The array does not include the id for this Beacon.
     */
	public String[] getNeighborhood();
    /**
     * Sets the array of unique ids for the neighboring
     * Beacons seen in the same scan as this Beacon.
     * The array does not include the id for this Beacon.
     */
	public void setNeighborhood(String[] n); 
}
