/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.core;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A BeaconReading represents a sighting of a single wireless
 * beacon from a Spotter that spots for wireless Beacons.  Spotters
 * typically return {@link BeaconMeasurement} objects that have zero or more
 * BeaconReadings in them.
 */
public interface BeaconReading {
	/** 
	 * @return a globally unique ID for this reading
	 */
	public String getId();

	/** 
	 * @return the type of reading that this is (WIFI, BLUETOOTH, GSM, etc)
	 */
	public String getType();
	
	/** 
	 * @return an int 0-100 depending on the signal strength, or -1 if unsupported
	 */		
	public int getNormalizedSignalStrength();
	
	public String toLogString();
	public String toShortString();
	public void toCompressedBytes(DataOutputStream dos) throws IOException;
	
	/**
	 * @return a human readable name (if any) for this reading
	 */
	public String getHumanReadableName();
	
	/** 
	 * returns true if the id for this beacon is  within the range of acceptable values,
	 * for instance if it is not all zeroes.
	 */
	public boolean isValid();
}
