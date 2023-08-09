/*
 * Created on Aug 11, 2004
 *
 */
package org.placelab.test;

import org.placelab.util.StringUtil;

/**
 * 
 */
public class StringUtilTests implements Testable {

	
	public String getName() {
		return "StringUtilTest";
	}

	public void runTests(TestResult result) throws Throwable {
		testDefault(result);
		testBasicChar(result);
		testLimitChar(result);
		testSingleChar(result);
		testBasicString(result);
		testLimitString(result);
		testMatch(result);
	}

	public void testDefault(TestResult result) {
		// make sure \t stays the default split char
		String testString = "xx \t yy";
		String[] actual = StringUtil.split(testString);
		String[] expected = new String[2];
		expected[0] = "xx ";
		expected[1] = " yy";
		assertSplit(result, expected, actual, "default");
	}
	
	public void testBasicChar(TestResult result) {
		String testString = "xx yy";
		String[] actual = StringUtil.split(testString, ' ');
		String[] expected = new String[2];
		expected[0] = "xx";
		expected[1] = "yy";
		assertSplit(result, expected, actual, "basic char");
	}
	
	public void testLimitChar(TestResult result) {
		String testString = "xx yy zz";
		String[] actual = StringUtil.split(testString, ' ', 2);
		String[] expected = new String[2];
		expected[0] = "xx";
		expected[1] = "yy zz";
		assertSplit(result, expected, actual, "char limit");
	}
	
	public void testSingleChar(TestResult result) {
		String testString = "xx";
		String[] actual = StringUtil.split(testString);
		String[] expected = new String[1];
		expected[0] = "xx";
		assertSplit(result, expected, actual, "single");
	}
	
	public void testBasicString(TestResult result) {
		String testString = "yyy xy xxx";
		String[] actual = StringUtil.split(testString, "xy");
		String[] expected = new String[2];
		expected[0] = "yyy ";
		expected[1] = " xxx";
		assertSplit(result, expected, actual, "basic string");
	}
	
	public void testLimitString(TestResult result) {
		String testString = "xxx xy yyy xy zzz";
		String[] actual = StringUtil.split(testString, "xy", 2);
		String[] expected = new String[2];
		expected[0] = "xxx ";
		expected[1] = " yyy xy zzz";
		assertSplit(result, expected, actual, "limit string");
	}
	
	public void assertSplit(TestResult result, String[] expected, String[] actual, String desc) {
		result.assertTrue(this, expected.length, actual.length, "split:" + desc + ": incorrect length");
		
		for (int i = 0; i < expected.length; i++) {
			result.assertTrue(this, expected[i], actual[i], "split:" + desc + ": actual[" + i + "] is wrong");
		}
	}
	
	public void testMatch(TestResult result) {
		String testString = " xxx xy yyy xy zzz";
		result.assertTrue(this,true,StringUtil.match(testString, " ", "zzz"),"String match 1");
		result.assertTrue(this,true,StringUtil.match(testString, " ", "xxx"),"String match 2");
		result.assertTrue(this,false,StringUtil.match(testString, " ", "xyz"),"String match 3");
		result.assertTrue(this,true,StringUtil.match(testString, "xy", " yyy "),"String match 4");
		result.assertTrue(this,false,StringUtil.match(testString, "xy", " zz"),"String match 5");
	}
}
