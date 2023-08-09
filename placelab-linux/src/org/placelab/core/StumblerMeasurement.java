/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.core;

/**
 * A Measurement produced by reading a portion of a log by a {@link org.placelab.spotter.LogSpotter}
 */
public class StumblerMeasurement extends BeaconMeasurement implements PositionMeasurement {
	private Coordinate coord;
	
	public StumblerMeasurement(long timestampInMillis, Coordinate c) {
		super(timestampInMillis);
		coord = c;
	}
	public StumblerMeasurement(long timestampInMillis, Coordinate c, BeaconReading[] readings) {
		super(timestampInMillis, readings);
		coord = c;
	}
	
	public void setPosition(Coordinate c) { coord = c; }
	public Coordinate getPosition() { return coord; }
	
    /* (non-Javadoc)
     * @see org.placelab.core.PositionMeasurement#getType()
     */
    public String getType() {
        return "STUMBLER";
    }
	
}
