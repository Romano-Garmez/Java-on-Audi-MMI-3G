/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.client.tracker;

import org.placelab.core.Coordinate;
import org.placelab.core.TwoDCoordinate;

/**
 * An Estimate whose Coordinate is a {@link org.placelab.core.TwoDCoordinate}
 * 
 * 
 */
public class TwoDPositionEstimate implements Estimate {
	protected TwoDCoordinate position;
	protected double stdDev;
	protected long timestamp;
	
	public TwoDPositionEstimate() {	    
	}
	public TwoDPositionEstimate(TwoDPositionEstimate e) {
		this(e.getTimestamp(), new TwoDCoordinate(e.position), e.stdDev);
	}
	public TwoDPositionEstimate(long timestamp, TwoDCoordinate position, double stdDev) {
		this.timestamp = timestamp;
		this.position = position;
		this.stdDev = stdDev;
	}
	public void construct(long timestamp, Coordinate position, String stdDevString) {
	    if(!(position instanceof TwoDCoordinate)) throw new RuntimeException("Cannot use TwoDPositionEstimate with Coordinates of type other than TwoDCoordinate");
		this.timestamp = timestamp;
		this.position = (TwoDCoordinate)position;
		this.stdDev = Double.parseDouble(stdDevString);
	}
	public long getTimestamp() {return timestamp;}
    public Coordinate getCoord() { return position; }
    public TwoDCoordinate getTwoDPosition() { return position; }
    public double getStdDev() { return stdDev; }
    public String getStdDevAsString() { return "" + stdDev; }
    public int getStdDevInMeters() { return (int) stdDev; }
    public String toString() { return "Position at time " + timestamp + " estimated as " + position.toString() + " (stdDev " + stdDev + ")"; }
}
