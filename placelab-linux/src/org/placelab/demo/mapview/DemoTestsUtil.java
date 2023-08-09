package org.placelab.demo.mapview;

import java.io.File;

import org.placelab.core.PlacelabProperties;
import org.placelab.test.TestResult;
import org.placelab.test.Testable;

public class DemoTestsUtil {

	public static String getTestDataSubDir(Testable testable,
											TestResult result, 
											String subDir) {
		String testDataDir = PlacelabProperties.get("placelab.testdir");
		File test = new File(testDataDir);
		if(testDataDir.length() == 0 || !test.exists()) {
			result.print("DemoTests: I didn't find my test data from " +
					"looking in Placelab.Properties, checking in your working" +
					" directory ...");
			testDataDir = "testdata";
		}
		testDataDir = testDataDir + File.separator + subDir + 
			File.separator;
		test = new File(testDataDir);
		if(!test.isDirectory()) {
			result.fail(testable, "cannot find my test data directory which I " +
					"expected to be at " + testDataDir + ".  " + 
					"You can set where I look for it by setting " +
					"\"placelab.testdir\" in your placelab.ini file.  The " +
					"test files themselves should be in the testdata " +
					"directory in cvs.");
			return null;
		}
		return testDataDir;
	}

}
