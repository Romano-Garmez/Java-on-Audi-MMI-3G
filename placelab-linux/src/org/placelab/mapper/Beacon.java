package org.placelab.mapper;

import org.placelab.collections.HashMap;
import org.placelab.core.Coordinate;
import org.placelab.util.StringUtil;

/**
 * A beacon is a generalized access point for a cellular network (like 802.11 or
 * Bluetooth or GSM). In the case of 802.11, humanReadableName maps to the SSID
 * and uniqueID maps to BSSID. All beacons must have a known location.
 *  
 */
public abstract class Beacon {
	public static Beacon create(Class klass, HashMap map) {
//		if (!Beacon.class.isAssignableFrom(klass)) {
//			throw new IllegalArgumentException("klass must be a subclass of Beacon");
//		}
		
		try {
			Class c = Class.forName("org.placelab.mapper.Beacon");
			if(!c.isAssignableFrom(klass))
				throw new IllegalArgumentException("klass must be a subclass of Beacon");
		} catch(ClassNotFoundException e) {
			throw new RuntimeException("Could not load class in Beacon");
		}
		
		Beacon beacon = null;
		try {
			beacon = (Beacon) klass.newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
		beacon.fromHashMap(map);
		return beacon;
	}
	public String toString() {
		return StringUtil.hashMapToStorageString(toHashMap());
	}

	/**
	 * Loads the Beacon from a HashMap where values are keyed
	 * by the appropriate fields for the Beacon type in 
	 * {@link org.placelab.core.Types}
	 */
	public abstract void fromHashMap(HashMap map);
	/**
	 * Stores the Beacon into a HashMap where values are keyed
	 * by the appropriate fields for the Beacon type in 
	 * {@link org.placelab.core.Types}
	 */
	public abstract HashMap toHashMap();
	/**
	 * Gets the unique id for this Beacon as a String
	 */
	public abstract String getId();
	/**
	 * Gets the type of this Beacon as a String.  Known
	 * Beacon types are listed in {@link org.placelab.core.Types}
	 */
	public abstract String getType();
	/**
	 * Gets the position for where this Beacon resides in the world
	 */
	public abstract Coordinate getPosition();
	/**
	 * Gets a sensible maximum range,
	 * further than which the Beacon won't be detectable
	 * 
	 */
	public abstract int getMaximumRange();
}
