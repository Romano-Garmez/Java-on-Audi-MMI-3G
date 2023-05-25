package org.placelab.test;


/**
 * 
 *
 */
public class CoreTests extends Harness {
	
	public void setupAllTests(String[] IGNORED) {
		addTest(new CoordinateTests());
		addTest(new JDBMQuickMapLoaderTest());
		addTest(new PlacelabStumblerLogSpotterTests());
		addTest(new JDBMMapLoaderTest());
		addTest(new FixedPointLongTest());
	}

}
