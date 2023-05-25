package org.placelab.mapper;

import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.core.Coordinate;



/**
 * Mappers find the {@link Beacon} object associated with a given id.
 * If a beacon is not known, the findBeacon() method will return null.
 * <p>
 * Mappers typically represent the on-disk cache of Beacons in keeping
 * with the placelab philosophy of off-line location computation.
 * 
 * @see org.placelab.client.tracker.BeaconTracker
 */
public interface Mapper {
    /**
     * Opens the Mapper.  You must call this method before querying the mapper.
     * @return whether or not the Mapper was successfully opened.
     */
	public boolean open();
	
	/**
	 * Closes the Mapper.  Closed Mappers can no longer be queried.
	 * @return whether or not the Mapper was successfully closed.
	 */
	public boolean close();
	
	/**
	 * Deletes all Beacon records in the Mapper.  This is typically used by
	 * MapLoaders prior to reloading the Mapper with data.
	 * @return whether or not the Mapper was successfully emptied.
	 */
	public boolean deleteAll();
	
	public boolean isOpened();
	
	/**
	 * Returns the first Beacon found that has a matching id, or null if
	 * no Beacon is returned.
	 * @param id the unique identifier for the Beacon
	 */
	public Beacon findBeacon(String id);
	
	/**
	 * Find all Beacons in the Mapper that match the given id.
	 * @param id the unique identifier for the Beacon
	 * @return a list of all Beacons matching the id in the Mapper
	 * that may be empty if no Beacons match.
	 */
	public LinkedList findBeacons(String id);
	
	/**
	 * c1 and c2 define opposing corners of a rectangular area in 
	 * which you wish to get an Iterator over all Beacons in that
	 * area.
	 */
	public Iterator query(Coordinate c1, Coordinate c2);
	
	/**
	 * Implementation dependent method to unpack a Beacon from the Mapper
	 */
	public Beacon createBeacon(String keyValuePairs);
	
	/**
	 * Load a new single Beacon into the Mapper
	 * @param id the unique id for the Beacon
	 * @param beacon a new Beacon to be put into the Mapper
	 * @return whether or not the Beacon was successfully added
	 */
	public boolean putBeacon(String id, Beacon beacon);
	
	/**
	 * Put a list of Beacons for one unique id into the Mapper
	 * @param id the unique id to key the Beacons to
	 * @param beacons the Beacons to add
	 * @return whether or not the put succeeded
	 */
	public boolean putBeacons(String id, LinkedList beacons);

	/**
	 * Signals to the Mapper that it is about to be loaded in bulk.
	 * Some implementations can use this to lock their underlying
	 * database and speed up loading.  You should not query the
	 * Mapper while using bulk loading.
	 * @see #endBulkPuts()
	 */
	public void startBulkPuts();
	
	public void endBulkPuts();
	
	public boolean overrideOnPut();
}
