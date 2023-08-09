/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.client.tracker;

import org.placelab.core.Coordinate;
import org.placelab.core.ThreeDCoordinate;

/**
 * An Estimate whose Coordinate is a {@link org.placelab.core.ThreeDCoordinate}
 * 
 */
public class ThreeDPositionEstimate implements Estimate {
	protected ThreeDCoordinate position;
	protected double stdDev;
	protected long timestamp;
	
	public ThreeDPositionEstimate() {	    
	}
	public ThreeDPositionEstimate(ThreeDPositionEstimate e) {
		this(e.getTimestamp(), new ThreeDCoordinate(e.position), e.stdDev);
	}
	public ThreeDPositionEstimate(long timestamp, ThreeDCoordinate position, double stdDev) {
		this.timestamp = timestamp;
		this.position = position;
		this.stdDev = stdDev;
	}
	public void construct(long timestamp, Coordinate position, String stdDevString) {
	    if(!(position instanceof ThreeDCoordinate)) throw new RuntimeException("Cannot use ThreeDPositionEstimate with Coordinates of type other than ThreeDCoordinate");
		this.timestamp = timestamp;
		this.position = (ThreeDCoordinate)position;
		this.stdDev = Double.parseDouble(stdDevString);
	}
	public long getTimestamp() {return timestamp;}
    public Coordinate getCoord() { return position; }
    public ThreeDCoordinate getThreeDPosition() { return position; }
    public double getStdDev() { return stdDev; }
    public String getStdDevAsString() { return "" + stdDev; }
    public int getStdDevInMeters() { return (int) stdDev; }
    public String toString() { return "Position at time " + timestamp + " estimated as " + position.toString() + " (stdDev " + stdDev + ")"; }
}
