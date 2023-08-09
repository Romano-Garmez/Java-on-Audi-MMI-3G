/*
 * Created on Sep 22, 2004
 * 
 */
package org.placelab.test;

/**
 * @author iesmith
 * 
 */
public abstract class TestResult extends WeakTestResult {

	public static double EPSILON = 0.0000001;

	/** Call this assertion to see if a test did the expected thing **/
	public void assertTrueDouble(Testable testable, double expected, double actual, String msg) {
		double delta = expected - actual;
		if (delta < 0) {
			delta *= -1;
		}
		if (delta > EPSILON) {
			fail(testable, "["+expected+"!="+actual+"]:"+msg);
		} else {
			++successes;
		}
	}

	/** Call this assertion to see if a test did the expected thing **/
	public void assertTrueDouble(Testable testable, double d1, double d2, double epsilon, String msg) {
		//System.out.println("HI : " + d1 + ", " + d2);
	    if(Double.isNaN(d1) != Double.isNaN(d2)) {
	        fail(testable,"["+d1+"!="+d2+"]:"+msg);
	        //System.out.println("Failed on NaN");
	        return;
	    }
	
	    double diff=d1-d2;
		if (Math.abs(diff)>=epsilon) {
			fail(testable,"["+d1+"!="+d2+"]:"+msg);
		} else {
			++successes;
		}
	}

	public boolean skipTestBecauseNoNetwork(int number) {
		return false;
	}
}
