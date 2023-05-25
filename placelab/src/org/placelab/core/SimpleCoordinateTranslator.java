/*
 * Created on Jul 13, 2004
 *
 */
package org.placelab.core;


/**
 * 
 *
 */
public class SimpleCoordinateTranslator extends CoordinateTranslator {
	/**
	 * A point in the world is chosen to be the origin (0,0) of the meter-based
	 * coordinate frame. By default the origin is the location of the Intel
	 * Research lab in Seattle. These coords can be changed to be something
	 * nearby your location for convenience sake. The reason to pick something
	 * close to home is because the x and y coordinates in meters will be small
	 * (like 150, or 1500 rather than 43987593457) and it make it easier to
	 * reason about the correctness of what you're doing.
	 *  
	 */
	private static final double X_TRANSLATION = 237.682;
	private static final double Y_TRANSLATION = 47.656;
	private static final double LAT_METER = 109379.0;
	private static final double LON_METER = 74969.0;
	/* WARNING: If you think you need to change the above constants to public, you are
	 * probably not using the right accessor methods
	 */

	
	
	// GPS for Richmond Memorial Hospital in NY, NY
	//		private static final double X_TRANSLATION=(360.0-74.08544);
	//		private static final double Y_TRANSLATION=(40.58351);

	// GPS for the Intel Research lab in Berkeley
	//		private static final double X_TRANSLATION=(360.0-122.26780);
	//		private static final double Y_TRANSLATION=(37.87042);

	public void move(TwoDCoordinate c, double xMeters, double yMeters) {
		c.moveTo(moveLatitudeBy(c, yMeters), moveLongitudeBy(c, xMeters));
	}
	private double moveLatitudeBy(TwoDCoordinate c, double meters) {
		return c.getLatitude() + meters/LAT_METER;
	}

	public double moveLongitudeBy(TwoDCoordinate c, double meters) {
		return c.getLongitude() + meters/LON_METER;
		
	}

	public double distance(TwoDCoordinate c1, TwoDCoordinate c2) {
		double x1 = longitudeToMeters(c1.getLongitude()),
		x2 = longitudeToMeters(c2.getLongitude()),
		y1 = latitudeToMeters(c1.getLatitude()),
		y2 = latitudeToMeters(c2.getLatitude());
		
		return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	}

	public double distance(ThreeDCoordinate c1, ThreeDCoordinate c2) {
		double x1 = longitudeToMeters(c1.getLongitude()),
		x2 = longitudeToMeters(c2.getLongitude()),
		y1 = latitudeToMeters(c1.getLatitude()),
		y2 = latitudeToMeters(c2.getLatitude()),
		z1 = c1.getElevation(),
		z2 = c2.getElevation();
		return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2) + (z1-z2)*(z1-z2));
	}

	public double xDistance(TwoDCoordinate c1, TwoDCoordinate c2) {	
		double x1 = longitudeToMeters(c1.getLongitude()),
		x2 = longitudeToMeters(c2.getLongitude());
		return x1-x2;
	}
	
	public double yDistance(TwoDCoordinate c1, TwoDCoordinate c2) {
		double y1 = latitudeToMeters(c1.getLatitude()),
		y2 = latitudeToMeters(c2.getLatitude());
		return y1-y2;
	}
	
	public double zDistance(ThreeDCoordinate c1, ThreeDCoordinate c2) {
		double z1 = c1.getElevation(),
		z2 = c2.getElevation();
		return z1-z2;
	}
	
	public double metersToLatitudeUnits(TwoDCoordinate reference, double meters) {
		return moveLatitudeBy(reference, meters) - reference.getLatitude();
	}
	public double metersToLongitudeUnits(TwoDCoordinate reference, double meters) {
		return moveLongitudeBy(reference, meters) - reference.getLongitude();
	}

	private double latitudeToMeters(double latitude) {
		return (latitude - Y_TRANSLATION) * LAT_METER;
	}
	private double longitudeToMeters(double longitude) {
		if (longitude < 0.0)
			longitude += 360.0;
		return (longitude - X_TRANSLATION) * LON_METER;		
	}
}
