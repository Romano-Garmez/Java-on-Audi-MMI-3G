/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.core;

import org.placelab.collections.HashMap;


/**
 * 
 *
 */
public class ThreeDCoordinate extends TwoDCoordinate {
	// assumed to be above sea level
	private double elevation;
	// used to tell if the elevation is known or guessed
	private boolean isElevationKnown = false;
	public static final ThreeDCoordinate NULL=new ThreeDCoordinate(Double.NaN, Double.NaN, Double.NaN);

	public ThreeDCoordinate() {
		super();
		elevation = Double.NaN;
	}

	public ThreeDCoordinate(double lat, double lon, double altitude) {
		super(lat, lon);
		elevation = altitude;
		isElevationKnown = true;
	}

	public ThreeDCoordinate(ThreeDCoordinate c) {
		super(c.getLatitude(), c.getLongitude());
		elevation = c.getElevation();
		isElevationKnown = c.getIsElevationKnown();
	}

	public void constructFromMap(HashMap map) {
		setLatitude(java.lang.Double.parseDouble((String)map.get(Types.LATITUDE)));
		setLongitude(java.lang.Double.parseDouble((String)map.get(Types.LONGITUDE)));
		String temp = (String)map.get(Types.ELEVATION);
		if(temp == null) {
			elevation = 0.0;
		}
		else {
			elevation = java.lang.Double.parseDouble(temp);
			isElevationKnown = true;
		}
	}

	public double getElevation() {
		return elevation;
	}

	public boolean getIsElevationKnown() {
		return isElevationKnown;
	}

	public boolean isNull() {
		return (super.isNull() ? (elevation == Double.NaN || elevation == 0.0) : false);
	}

	public String toString() {
          return super.toString() + "," + elevation;
        }

	public int distanceFromInMeters(Coordinate c2) {
	    return (int)distanceFrom((ThreeDCoordinate)c2);
	}
	public String distanceFromAsString(Coordinate c2) {
	    return ""+distanceFrom((ThreeDCoordinate)c2);
	}
	public double distanceFrom(ThreeDCoordinate c2) {
		return (CoordinateTranslator.T.distance(this, c2));
	}
	public double zDistanceFrom(ThreeDCoordinate c2) {
		return (CoordinateTranslator.T.zDistance(this, c2));
	}
}
