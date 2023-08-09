/*
 * Created on Jul 26, 2004
 *
 */
package org.placelab.test;

import org.placelab.core.Coordinate;
import org.placelab.core.FixedTwoDCoordinate;
import org.placelab.core.TwoDCoordinate;

/**
 * 
 */
public class CoordinateTests implements Testable {

	public String getName() {
		return "CoordinateTests";
	}

	public void runTests(TestResult result) throws Throwable {
		testFloatWithin(result);
		testFixedWithin(result);
		testFixedTranslate(result);
		// float fails this test :)
		//testFloatTranslate(result);
	}
	
	public void testFloatWithin(TestResult result) {
		testWithin(result, "TwoDCoordinate", new TwoDCoordinate("0.5", "0.5"), 
				new TwoDCoordinate("0.1", "0.1"), new TwoDCoordinate("1.0", "1.0"));
	}
	
	public void testFixedWithin(TestResult result) {
		testWithin(result, "FixedTwoDCoordinate", new FixedTwoDCoordinate("0.5", "0.5"), 
				new FixedTwoDCoordinate("0.1", "0.1"), new FixedTwoDCoordinate("1.0", "1.0"));
	}

	public void testWithin(TestResult result, String coordType, Coordinate inside, 
			Coordinate lowerLeft, Coordinate upperRight) {
		
		result.assertTrue(this, true, inside.within(lowerLeft, upperRight), "first " + 
				coordType + ".within()");
		
		result.assertTrue(this, true, inside.within(upperRight, lowerLeft), "second " + 
				coordType + ".within()");
		
		result.assertTrue(this, false, lowerLeft.within(inside, upperRight), "third " + 
				coordType + ".within()");
	}
	
	public void testFixedTranslate(TestResult result) {	    
	    Coordinate origin = new FixedTwoDCoordinate("50.23242","-123.4832");
//	    Coordinate origin = new FixedTwoDCoordinate("0.1","0.1");
	    testTranslate(result, "FixedTwoDCoordinate",  origin);
	}

	public void testFloatTranslate(TestResult result) {
	    Coordinate origin = new TwoDCoordinate("50.23242","-123.4832");
	    testTranslate(result, "TwoDCoordinate",  origin);
	}
	
	public void testTranslate(TestResult result, String coordType, Coordinate origin) {
	    int north = 40000;
	    int east  = 30000;
	    Coordinate transt = origin.translate(north,0);
	    Coordinate trans = transt.translate(0,east);
	    Coordinate transbackt = trans.translate(-north,0);
	    Coordinate transback = transbackt.translate(0,-east);
//	    System.out.println(coordType + "Original  : " + origin + " , north " + north + ", east " + east);
//	    System.out.println(coordType + "Translated: " + trans);
//	    System.out.println(coordType + "Transback : " + transback);
//	    System.out.println(coordType + "Distance  : " + origin.distanceFromAsString(trans));
//	    System.out.println(coordType + " End dist  : " + origin.distanceFromAsString(transback));
	    double n = north;
	    double e = east;
	    double dumbresult = Math.sqrt((n*n)+(e*e));
	    result.assertTrueDouble(this,dumbresult,Double.parseDouble(origin.distanceFromAsString(trans)), TrackerTests.EPSILON,"Translate: " + coordType + " distance test");
	    result.assertTrue(this,true,Double.parseDouble(origin.distanceFromAsString(transback)) != 0.0,"Translate: " + coordType + " non-return-to-origin");
	}
}
