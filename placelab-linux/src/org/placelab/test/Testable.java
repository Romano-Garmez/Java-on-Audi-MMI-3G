package org.placelab.test;

/**
 * All tests implement this interface 
 *
 */
public interface Testable extends TestableBase{
	public void runTests(TestResult result) throws Throwable;
}
