/*
 * Created on Jul 13, 2004
 *
 */
package org.placelab.core;

/**
 * Utility methods for translating Coordinates
 */
public abstract class CoordinateTranslator {
	public abstract void move(TwoDCoordinate c, double xMeters, double yMeters);
	
	/**
	 * Returns the Euclidean distance in meters between two coordinates
	 */
	public abstract double distance(TwoDCoordinate c1, TwoDCoordinate c2); // in meters
	
	/**
	 * Returns a signed distance in meters in the x direction between two coordinates.  It represents c1.longitude - c2.longitude in meters.  
	 */
	public abstract double xDistance(TwoDCoordinate c1, TwoDCoordinate c2); // in meters
	
	/**
	 * Returns a signed distance in meters in the y direction between two coordinates.  It represents c1.latitude - c2.latitude in meters.
	 */
	public abstract double yDistance(TwoDCoordinate c1, TwoDCoordinate c2); // in meters
	
	/**
	 * Returns a signed distance in meters in the z direction between two coordinates.  It represents c1.elevation - c2.elevation in meters.
	 */
	public abstract double zDistance(ThreeDCoordinate c1, ThreeDCoordinate c2); // in meters
	
	public abstract double metersToLatitudeUnits(TwoDCoordinate reference, double meters);
	public abstract double metersToLongitudeUnits(TwoDCoordinate reference, double meters);
	
	public static CoordinateTranslator T=create();
	private static CoordinateTranslator create() {
		return new SimpleCoordinateTranslator();
	}
}
