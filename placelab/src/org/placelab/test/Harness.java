package org.placelab.test;

import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;

/**
 * This class hold test and iterates through them executing their tests. See {@link org.placelab.test.Regression} for an example of how to 
 * run a test.
 *
 */
public abstract class Harness {
	protected static LinkedList allTestables = new LinkedList();
	
	public static void runTheTests(TestResult result) {
		Iterator iter=allTestables.iterator();
		while (iter.hasNext()) {
			Testable testObj = (Testable) iter.next();
			try {
				testObj.runTests(result);
			} catch (Throwable t) {
				result.errorCaught(testObj,t);
			}
		}
		result.summary();
	}
	
	public static void clearTests() {
		allTestables.clear();
	}
	

	public abstract void setupAllTests(String[] argv);
	
	public static void addTest(Testable testable) {
		allTestables.add(testable);
	}
}
