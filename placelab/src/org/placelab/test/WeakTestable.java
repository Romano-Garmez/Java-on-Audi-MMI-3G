/*
 * Created on Sep 22, 2004
 * 
 */
package org.placelab.test;

/**
 * @author iesmith
 * 
 */
public interface WeakTestable extends TestableBase {
	//sadly this mimics rather than sharing with Testable
	//the phones can't touch anything involving double so this is
	//necessary, trust me, I tried a bunch of other schemes and got hosed.
	public void runTests(WeakTestResult result) throws Throwable;
}
