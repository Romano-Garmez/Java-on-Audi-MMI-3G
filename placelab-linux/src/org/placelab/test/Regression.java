package org.placelab.test;

import java.io.PrintWriter;
import java.util.Date;

import org.placelab.core.PlacelabProperties;
import org.placelab.demo.mapview.DemoTests;
import org.placelab.jsr0179.JSRLocationAPITests;
import org.placelab.stumbler.StumblerTests;
import org.placelab.util.FileSynchronizer;
import org.placelab.util.NumUtil;

/**
 * The root regression test. Running this class will test all of the major components in the system.
 *
 */
public class Regression extends Harness {
	public void setupAllTests(String[] argv) {
	}

	public static void main(String[] argv) {
		NumUtil.seedRand(new Date().getTime()); // different seed every time
		// Make sure we have the right data files
		FileSynchronizer fs = new FileSynchronizer("http://seattle.intel-research.net/projects/placelab/data/regression", PlacelabProperties.get("placelab.dir"));
		fs.synch(false);
		// run the tests
		setupAllKnownTests(argv);
		runTheTests(new ConsoleTestResult(new PrintWriter(System.out, true),false, true));
		
		System.exit(1);
	}

	public static void setupAllKnownTests(String[] argv) {
		//put list of package harnesses here
		new TestFrameworkSelfTest().setupAllTests(argv);
		new CoreTests().setupAllTests(argv);
		new TrackerTests().setupAllTests(argv);
		new DemoTests().setupAllTests(argv);
		new UtilTests().setupAllTests(argv);
		new StumblerTests().setupAllTests(argv);
		new GlyphTests().setupAllTests(argv);
		new EventSystemTests().setupAllTests(argv);
		new JSRLocationAPITests().setupAllTests(argv);
	}

}
