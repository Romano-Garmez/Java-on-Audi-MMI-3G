package org.placelab.core;



/**
 * A Measurement that already knows its location as a Coordinate
 */
public interface PositionMeasurement {
	public Coordinate getPosition();
	public long getTimestamp();
	public String getType();
}
