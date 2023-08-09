package org.placelab.test;

/**
 * 
 * This is how to do a test of a specific class. This is a
 * simple test to show off how to use the testing infrastructure.
 */
public class ExampleTest implements Testable {

	public String getName() {
		return "ExampleTest";
	}
	
	public void runTests(TestResult testResult) {
		testFailure(testResult);
		testException(testResult);
		testPass(testResult);
	}

	public void testFailure(TestResult testResult) {
		//uncomment to see a call to fail and watch the
		//change in the summary
		//testResult.fail(this,"we are totally boned!");
	}

	public void testException(TestResult testResult) {
		//uncomment to see what happens when you throw
		//an exception while testing
		//note: the test framework stops testing a given
		//      Testable at the point of the first exception
		//throw new IllegalArgumentException("Example of what "+
		//	"will happen if you get an exception");
	}
	
	public void testPass(TestResult testResult) {
		Example exampleObj=new Example();
		
		
		//change 2+2 to 2+3 to see what happens!
		testResult.assertTrue(this, 4,exampleObj.add(2,2),
			"you better get this right");
	}
}
