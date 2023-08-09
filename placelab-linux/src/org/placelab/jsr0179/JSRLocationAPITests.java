
package org.placelab.jsr0179;

import org.placelab.test.Harness;
public class JSRLocationAPITests extends Harness {

	public void setupAllTests (String[] argv) {
		addTest(new FSLandmarkStoreTest());
	}
}
