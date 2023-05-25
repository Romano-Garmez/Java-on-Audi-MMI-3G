package org.placelab.example;

import org.placelab.core.TwoDCoordinate;


/**
 * A sample program that shows how to use 2DCoordinates
 */
public class CoordinateExample {

	public static void main(String[] args) {
		TwoDCoordinate c1,c2,c3;
		
		// The coordinates of Intel Research Seattle
		c1 = new TwoDCoordinate(47.656,-122.318);
		
		// The coordinates of Intel Research Berkeley
		c2 = new TwoDCoordinate(37.87042,-122.26780);
		
		double distance = c1.distanceFrom(c2);
				
		System.out.println("IRS is at ("+c1.getLatitude()+","+c1.getLongitude()+")");
		System.out.println("IRB is at ("+c2.getLatitude()+","+c2.getLongitude()+")");
		System.out.println("IRS and IRB are " + (int)distance/1000 +
		                   " km or " + (int)distance/1609 + 
		                   " miles apart");
		
		// Move c2 5000 meters north
		c2.moveBy(0, 5000);
		
		System.out.println("The point 5 km north of IRB has lat=" + 
		       c2.getLatitude() + " lon=" + c2.getLongitude());
		       
	}
}
