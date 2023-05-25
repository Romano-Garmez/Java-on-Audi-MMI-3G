package org.placelab.test;

import org.placelab.util.ZipUtilTests;


public class UtilTests extends Harness {

	public void setupAllTests(String[] argv) {
		addTest(new StringUtilTests());
		addTest(new ZipUtilTests());
	}

}
