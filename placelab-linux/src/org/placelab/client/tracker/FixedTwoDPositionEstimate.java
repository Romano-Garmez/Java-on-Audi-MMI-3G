/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.client.tracker;


import org.placelab.core.Coordinate;
import org.placelab.core.FixedTwoDCoordinate;
import org.placelab.util.FixedPointLong;
import org.placelab.util.FixedPointLongException;

/**
 * An Estimate whose Coordinate is a {@link FixedTwoDCoordinate}.
 * 
 */
public class FixedTwoDPositionEstimate implements Estimate {
	protected FixedTwoDCoordinate position;
	/** flong representing error in estimate */
	protected long stdDevFlong;
	/** timestamp in millis */
	protected long timestamp;
	
	public FixedTwoDPositionEstimate() {	    
	}
	public FixedTwoDPositionEstimate(FixedTwoDPositionEstimate e) {
		this(e.getTimestamp(), new FixedTwoDCoordinate(e.position), e.stdDevFlong);
	}
	public FixedTwoDPositionEstimate(long timestamp, FixedTwoDCoordinate position, long stdDevFlong) {
		this.timestamp = timestamp;
		this.position = position;
		this.stdDevFlong = stdDevFlong;
	}
	public void construct(long timestamp, Coordinate position, String stdDevString)  {
	    if(!(position instanceof FixedTwoDCoordinate)) throw new RuntimeException("Cannot use FixedTwoDPositionEstimate with Coordinates of type other than FixedTwoDCoordinate");
	    this.timestamp = timestamp;
	    this.position = (FixedTwoDCoordinate) position;
	    try {
	        this.stdDevFlong = FixedPointLong.stringToFlong(stdDevString);
	    } catch(FixedPointLongException e) {
	        throw new IllegalArgumentException("Error parsing stdDevString " + e);
	    } 
	}
	public long getTimestamp() {return timestamp; }
    public Coordinate getCoord() { return position; }
    public FixedTwoDCoordinate getFlongPosition() { return position; }
    public long getStdDevFlong() { return stdDevFlong; }
    public void setStdDevFlong(long stdDevFlong) { this.stdDevFlong = stdDevFlong; }
    public String getStdDevAsString() { 
        try {
            return FixedPointLong.flongToString(stdDevFlong);
        } catch(FixedPointLongException fple) {
            return "ERR " + fple;
        }
    }
    public int getStdDevInMeters() {
        return FixedPointLong.intValue(stdDevFlong);
    }
    
    public String toString() { 
        return "PositionEstimate: time " + timestamp + 
        " pos " + position.toString() + 
        " (error " + getStdDevAsString() + ")";
    }
    
    public static FixedTwoDPositionEstimate intersect(FixedTwoDPositionEstimate fe[]) throws FixedPointLongException {
        boolean first=true;
        long north=0, south=0, east=0, west=0, minRadiusFlong=0;

        for(int i=0;i<fe.length;i++) {
            FixedTwoDCoordinate northeast, southwest;
            try {
                northeast = fe[i].position.translateFixed(fe[i].stdDevFlong, fe[i].stdDevFlong);
                southwest = fe[i].position.translateFixed(-fe[i].stdDevFlong, -fe[i].stdDevFlong);
            } catch(FixedPointLongException fple) {
                throw new FixedPointLongException("FixedTwoDPosition.intersect: "+fple);
            }
	        //System.out.println("current diagonal " + northeast.distanceFromAsString(southwest));
	        long curNorth = northeast.getLatitudeFlong();
	        long curEast = northeast.getLongitudeFlong();
	        long curSouth = southwest.getLatitudeFlong();
	        long curWest = southwest.getLongitudeFlong();
	        if(first) {
	            north = curNorth;
	            east = curEast;
	            south = curSouth;
	            west = curWest;
	            //minRadiusFlong = fe[i].stdDevFlong;
	            //System.out.println("First   box " + FixedPointLong.flongToString(curNorth) + "," + FixedPointLong.flongToString(curEast) + "," + FixedPointLong.flongToString(curSouth) + "," + FixedPointLong.flongToString(curWest));
	            first=false;
	        } else {
	            //System.out.println("Merging box " + FixedPointLong.flongToString(curNorth) + "," + FixedPointLong.flongToString(curEast) + "," + FixedPointLong.flongToString(curSouth) + "," + FixedPointLong.flongToString(curWest));
	            north = Math.min(north, curNorth);
	            east = Math.min(east, curEast);
	            south = Math.max(south, curSouth);
	            west = Math.max(west, curWest);
	            //minRadiusFlong = min(minRadiusFlong, fe[i].stdDevFlong);
	            //System.out.println("Result  box " + FixedPointLong.flongToString(north) + ","+FixedPointLong.flongToString(east)+","+FixedPointLong.flongToString(south)+","+FixedPointLong.flongToString(west));
	        }
        }
        if(first || north < south || east < west) {
            // this test needs to handle dateline issues
            // no intersection.  
            return null;            
        }
        
        FixedTwoDCoordinate estCentre = new FixedTwoDCoordinate(((north+south)/2L),((east+west)/2L));
        return new FixedTwoDPositionEstimate(System.currentTimeMillis(), estCentre, ((north-south)+(east-west))/2L); 
    }
}
