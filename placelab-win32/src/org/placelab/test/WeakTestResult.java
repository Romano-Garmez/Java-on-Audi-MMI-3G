/*
 * Created on Sep 22, 2004
 * 
 */
package org.placelab.test;

/**
 * @author iesmith
 * 
 */
public abstract class WeakTestResult {

	protected int failures = 0;
	protected int successes = 0;
	protected int errors = 0;
	public abstract void print(String msg);
	public abstract void exceptionExtra(Throwable t);
	/** Call this if a test has failed, whatHappened should be a description of the failure. **/
	public void fail(TestableBase whoFailed, String whatHappened) {
		String result ="FAILURE: "+ whoFailed.getName()+": "+
			whatHappened;
		print(result);
		++failures;
	}
	/** Call this when your test catches an error or an exception **/
	public void errorCaught(TestableBase whoFailed, Throwable t) {
		++errors;
		print("EXCEPTION " + whoFailed.getName()+": " + t.getMessage());
		exceptionExtra(t);
	}
	public void summary() {
		print("--- TEST RESULT SUMMARY ---");
		if ((errors==0) && (failures==0)) {
			print("All tests passed ("+successes+").");
		} else {
			print("" + successes + " tests passed");
			print("" + failures+" tests failed");
			print("" + errors +" errors were caught");
		}
	}
	/** Call this assertion to see if a test did the expected thing **/
	public void assertTrue(TestableBase testable, int expected, int actual, String msg) {
			
		if (expected!=actual) {
			fail(testable, "["+expected+"!="+actual+"]:"+msg);
		} else {
			++successes;
		}
	}
	/** Call this assertion to see if a test did the expected thing **/
	public void assertTrue(TestableBase testable, String expected, String actual, String msg) {
			
		if (!expected.equals(actual)) {
			fail(testable, "["+expected+"!="+actual+"]:"+msg);
		} else {
			++successes;
		}
	}
	/** Call this assertion to see if a test did the expected thing **/
	public void assertTrue(TestableBase testable, boolean expected, boolean actual, String msg) {
			
		if (expected != actual) {
			fail(testable, "["+expected+"!="+actual+"]:"+msg);
		} else {
			++successes;
		}
	}
	/** Call this assertion to see if a test did the expected thing **/
	public void notEqual(TestableBase testable, String string1, String string2, String msg) {
		if (string1.equals(string2)) {
			fail(testable, "["+string1+"=="+string2+"]:"+msg);
		} else {
			++successes;
		}
	}
	/** Call this assertion to see if a test did the expected thing **/
	public void sameObject(TestableBase testable, Object obj1, Object obj2, String msg) {
		if (obj1!=obj2) {
			fail(testable, "["+obj1+" NOT SAME "+obj2+"]:"+msg);
		} else {
			++successes;
		}
	}
	/** Call this assertion to see if a test did the expected thing **/
	public void differentObjects(TestableBase testable, Object obj1, Object obj2, String msg) {
		if (obj1==obj2) {
			fail(testable, "["+obj1+" SAME "+obj2+"]:"+msg);
		} else {
			++successes;
		}
	}
	/** Call this assertion to see if a test did the expected thing **/
	public void isNull(TestableBase testable, Object o, String msg) {
		if (o!=null) {
			fail(testable, "["+o+" IS NOT NULL]:"+msg);
		} else {
			++successes;
		}
	}
	/** Call this assertion to see if a test did the expected thing **/
	public void equalObjects(TestableBase testable, Object o1, Object o2, String msg) {
	
		if (!o1.equals(o2)) {
			fail(testable, "["+o1+" NOT equal() TO "+o2+"]:"+msg);
		} else {
			++successes;
		}
	}
	
}
