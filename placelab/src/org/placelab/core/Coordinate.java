/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.core;

import org.placelab.collections.HashMap;

/**
 * A Coordinate represents a point on Earth.  It does not
 * expose any methods with doubles, because not all Placelab
 * platforms support floating point math.
 */
public interface Coordinate {
    /**
     * Constructs a Coordinate from String representations of
     * lat and lon as doubles in hh.ddddd format
     */
	public void constructFromStrings(String lat, String lon);
	/**
	 * Constructs a Coordinate from the NMEA specification for Coordinates
	 * which is a hemisphere and a lat and lon in hhmm.sssss format.
	 */
	public void constructFromNMEA(String latNMEA, String latHem, String lonNMEA, String lonHem);
	/**
	 * Constructs from a HashMap.  
	 * The HashMap should have the following structure:
	 * <pre>
	 * Types.LATITUDE=latitude as a String in hh.dddddd format
	 * Types.LONGITUDE=longitude as a String in hh.ddddd format
	 * <pre>
	 * @see Types#newCoordinate(HashMap)
	 */
	public void constructFromMap(HashMap map);
	public Coordinate createClone();
	
	/**
	 * The Null coordinate is a Coordinate which does not map to anywhere at all.
	 * Returns true if this coordinate is the Null coordinate.
	 */
	public boolean isNull();

    // geometric ops
	
	/**
	 * c1 and c2 define opposing corners of a rectangular region.
	 * This method returns true if this Coordinate falls in that
	 * region.
	 */
	public boolean within(Coordinate c1, Coordinate c2);
	
	/** 
	 * Provides a new Coordinate at a position translated from an existing one
	 * @param north the number of meters to translate north
	 * @param east the number of meters to translate east
	 * @return a new Coordinate which is translated from the existing one according to the parameters
	 */
	public Coordinate translate(int north, int east);

	public String toString();
	public String getLatitudeAsString();
	public String getLongitudeAsString();
	public String distanceFromAsString(Coordinate other);
	public int distanceFromInMeters(Coordinate other);
	
	public String getLatitudeNMEA();
	public String getLongitudeNMEA();
	public String getLatitudeHemisphereNMEA();
	public String getLongitudeHemisphereNMEA();
}
