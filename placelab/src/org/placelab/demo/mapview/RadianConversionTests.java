package org.placelab.demo.mapview;

import org.placelab.test.TestResult;
import org.placelab.test.Testable;

/**
 * 
 *
 */
public class RadianConversionTests  implements Testable {

	public String getName() {
		return "CoordSysTests";
	}
	
	public void runTests(TestResult result) {
		testPiChart(result);
	}
	
	public void testPiChart(TestResult result) {
		
		double ROUNDING_ERROR_FUDGE=0.00000000001;
		
		
		try {
			MapUtil.piToEightHeadings(-1.0);
			result.fail(this,"shouldn't work for values less than zero" )	;
		} catch (IllegalArgumentException e) {
		}
		try {
			MapUtil.piToEightHeadings((2.0*Math.PI)+1.0);
			result.fail(this,"shouldn't work for values > than 2PI" )	;
		} catch (IllegalArgumentException e) {
		}
		try {
			MapUtil.piToEightHeadings((2.0*Math.PI));
			result.fail(this,"shouldn't work for value ==  2PI" )	;
		} catch (IllegalArgumentException e) {
		}
		
		
		result.assertTrue(this,1,MapUtil.piToEightHeadings(0.0),
			"should be getting 1 for heading on 0");
		result.assertTrue(this,1,MapUtil.piToEightHeadings(0.00005),
			"should be getting 1 for heading near 0");
		result.assertTrue(this,2,MapUtil.piToEightHeadings((Math.PI/8.0)+ROUNDING_ERROR_FUDGE),
			"dividing line btw 1 and 2, should be INCLUSIVE of left edge");
		result.assertTrue(this,1,MapUtil.piToEightHeadings((Math.PI/8.0)-0.005),
			"dividing just under line");
		result.assertTrue(this,2,MapUtil.piToEightHeadings((Math.PI/8.0)+0.005),
			"dividing just over line");
		result.assertTrue(this,2,MapUtil.piToEightHeadings((3.0*Math.PI/8.0)-0.0001),
			"other end of 2");
		result.assertTrue(this,8,MapUtil.piToEightHeadings((15.0*Math.PI/8.0)-0.001),
			"near the end of 8");
		result.assertTrue(this,1,MapUtil.piToEightHeadings((2.0*Math.PI)-0.0001),
			"near the end of all it flips over");
	}
}
