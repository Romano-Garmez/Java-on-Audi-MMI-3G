/**
 * For these tests to succeed, you'll need to have my test map wads
 * in testdata/SectionedFileParserTests/.  You can set where I look for the testdata
 * directory by setting the test_data_dir key in your placelab.ini
 */

package org.placelab.demo.mapview;

import java.io.IOException;

import org.placelab.test.TestResult;
import org.placelab.test.Testable;

public class SectionedFileParserTests implements Testable {

	String missingBracketFile;
	String missingEqualFile;
	String emptySectionFile;
	String noSectionsFile;
	String noSpacesBetweenSectionsFile;
	String commentedFile;
	String plainFile;
	String noValueFile;
	String noNameFile;
	
	public SectionedFileParserTests() {

	}
	
	public String getName() {
		return "SectionedFileParserTests";
	}

	public void runTests(TestResult result) throws Throwable {
		String testDataDir;
		testDataDir = DemoTestsUtil.getTestDataSubDir(this, result, 
			"SectionedFileParserTests");
		if(testDataDir == null) return;
		missingBracketFile = testDataDir + "sfpt1.txt";
		missingEqualFile = testDataDir + "sfpt2.txt";
		emptySectionFile = testDataDir + "sfpt3.txt";
		noSectionsFile = testDataDir + "sfpt4.txt";
		noSpacesBetweenSectionsFile = testDataDir + "sfpt5.txt";
		commentedFile = testDataDir + "sfpt6.txt";
		plainFile = testDataDir + "sfpt7.txt";
		noValueFile = testDataDir + "sfpt8.txt";
		noNameFile = testDataDir + "sfpt9.txt";
		failTests(result);
		succeedTests(result);
	}
	
	// tests things that should fail
	private void failTests(TestResult result) {
		missingBracket(result);
		missingEqual(result);
		noValue(result);
		noName(result);
	}
	private void noName(TestResult result) {
		try {
			SectionedFileParser p = new SectionedFileParser(noNameFile);
		} catch(SectionedFileFormatException sffe) {
			result.equalObjects(this, sffe.failCode(),
					SectionedFileFormatException.BAD_SECTION_NAME_ERROR,
					"Missing name error code check");
			result.assertTrue(this, sffe.failLine(), 0,
					"Missing name error line check");
			return;
		} catch(IOException ioe) {
			result.errorCaught(this, ioe);
			result.print("Error in noName: " +
				ioe.toString());
			return;
		}
		result.fail(this, "Missing name not caught");
	}
	private void noValue(TestResult result) {
		try {
			SectionedFileParser p = new SectionedFileParser(noValueFile);
		} catch(SectionedFileFormatException sffe) {
			result.equalObjects(this, sffe.failCode(),
					SectionedFileFormatException.MISSING_EQUAL_ERROR,
					"Missing value error code check");
			result.assertTrue(this, sffe.failLine(), 1,
					"Missing value error line check");
			return;
		} catch(IOException ioe) {
			result.errorCaught(this, ioe);
			result.print("Error in noValue: " +
				ioe.toString());
			return;
		}
		result.fail(this, "Missing value not caught");
	}
	private void missingBracket(TestResult result) {
		try {
			SectionedFileParser p = new SectionedFileParser(missingBracketFile);
		} catch(SectionedFileFormatException sffe) {
			result.equalObjects(this, sffe.failCode(),
					// this missing bracket is read as a MALFORMED_LINE_ERROR since
					// its seen as just a stray line
					SectionedFileFormatException.MALFORMED_LINE_ERROR,
					"Missing bracket error code check");
			result.assertTrue(this, sffe.failLine(), 0,
					"Missing bracket error line check");
			return;
		} catch(IOException ioe) {
			result.errorCaught(this, ioe);
			result.print("Error in missingBracket: " +
				ioe.toString());
			return;
		}
		result.fail(this, "Missing bracket not caught");
	}
	private void missingEqual(TestResult result) {
		try {
			SectionedFileParser p = new SectionedFileParser(missingEqualFile);
		} catch(SectionedFileFormatException sffe) {
			result.equalObjects(this, sffe.failCode(),
					SectionedFileFormatException.MISSING_EQUAL_ERROR,
					"Missing equal error code check");
			result.assertTrue(this, sffe.failLine(), 1,
					"Missing equal error line check");
			return;
		} catch(IOException ioe) {
			result.print("Error in missingEqual: " +
				ioe.toString());
			result.errorCaught(this, ioe);
			return;
		}
		result.fail(this, "Missing equal not caught");
	}
	

	// tests things that should succeed
	private void succeedTests(TestResult result) {
		emptySection(result);
		noSections(result);
		noSpacesBetweenSections(result);
		plain(result);
		commented(result);
	}
	private void emptySection(TestResult result) {
		try {
			SectionedFileParser p = new SectionedFileParser(emptySectionFile);
			result.assertTrue(this,
					p.getSection("Empty").size(), 0,
					"empty section check");
		} catch(Exception e) {
			result.errorCaught(this, e);
			result.print("Error in emptySection: " +
				e.toString());
		}
	}
	private void noSections(TestResult result) {
		try {
			SectionedFileParser p = new SectionedFileParser(noSectionsFile);
			result.assertTrue(this,
					p.allSections().size(), 0,
					"no sections check");
		} catch(Exception e) {
			result.errorCaught(this, e);
			result.print("Error in noSections: " +
				e.toString());
		}
	}
	private void noSpacesBetweenSections(TestResult result) {
		try {
			SectionedFileParser p = new SectionedFileParser(noSpacesBetweenSectionsFile);
			result.assertTrue(this,
					(String)p.getSection("First").get("key"), "value",
					"no spaces between sections check 1");
			result.assertTrue(this,
					(String)p.getSection("Second").get("1"), "2",
					"no spaces between sections check 2");
		} catch(Exception e) {
			result.errorCaught(this, e);
			result.print("Error in noSpacesBetweenSections: " +
				e.toString());
		}
	}
	private void plain(TestResult result) {
		try {
			SectionedFileParser p = new SectionedFileParser(plainFile);
			result.assertTrue(this,
					(String)p.getSection("First").get("key"), "value",
					"plain file check 1");
			result.assertTrue(this,
					(String)p.getSection("Second").get("1"), "2",
					"plain file check 2");
		} catch(Exception e) {
			result.errorCaught(this, e);
			result.print("Error in plain: " +
				e.toString());
		}
	}
	private void commented(TestResult result) {
		try {
			SectionedFileParser p = new SectionedFileParser(commentedFile);
			result.assertTrue(this,
					(String)p.getSection("First").get("key"), "value",
					"commented file check 1");
			result.assertTrue(this,
					(String)p.getSection("Second").get("1"), "2",
					"commented file check 2");
		} catch(Exception e) {
			result.errorCaught(this, e);
			result.print("Error in commented: " +
				e.toString());
		}
	}
	
}
