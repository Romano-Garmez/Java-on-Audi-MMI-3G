package org.placelab.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.placelab.test.TestResult;
import org.placelab.test.Testable;

public class ZipUtilTests implements Testable {

	private String testDir;
	private String testFile1 = "test1.txt";
	private String testFile1Msg = "hi";
	private String testFile1Path;
	private String testSubDir = "subdir";
	private String testSubDirPath;
	private String testFile2 = "test2.txt";
	private String testFile2Path;
	private String testFile2Msg = "bye";

	public String getName() {
		return "ZipUtilTests";
	}

	public void runTests(TestResult result) throws Throwable {
		File testDirFile = File.createTempFile("ziptest", null);
		testDirFile.delete();
		if(!testDirFile.mkdir()) {
			result.fail(this, "(fats) I couldn't make a temporary directory " +
			" for testing at " + testDirFile.getAbsolutePath());
			return;
		}
		testDirFile.deleteOnExit();
		testDir = testDirFile.getAbsolutePath();
		testFile1Path = testDir + File.separator + testFile1;
		if(!echoToPath(testFile1Msg, testFile1Path)) {
			result.fail(this, "(fats) I couldn't make a temporary file " +
			" for testing at " + testFile1Path);
			return;
		}
		testSubDirPath = testDir + File.separator + testSubDir;
		File testSubDirFile = new File(testSubDirPath);
		testSubDirFile.deleteOnExit();
		if(!(new File(testSubDirPath)).mkdir()) {
			result.fail(this, "(fats) I couldn't make a temporary directory " +
			" for testing at " + testSubDirPath);
			return;
		}
		testFile2Path = testSubDirPath + File.separator + testFile2;
		if(!echoToPath(testFile2Msg, testFile2Path)) {
			result.fail(this, "(fats) I couldn't make a temporary file " +
			" for testing at " + testFile2Path);
			return;
		}
		File testZip = File.createTempFile("ziptest", "zip");
		testZip.deleteOnExit();
		ZipUtil.dirToZip(testDirFile, testZip);
		verifyZip(result, testZip);
	}
	
	private void verifyZip(TestResult result, File zip) 
		throws IOException {
		ZipFile in = new ZipFile(zip);
		ZipEntry e1 = in.getEntry(testFile1);
		ZipEntry e2 = in.getEntry(testSubDir + "/" + testFile2); 
		result.assertTrue(this, true, e1 != null, 
			"verifyZip testFile1 entry existance check");
		result.assertTrue(this, true, e2 != null, 
			"verifyZip testFile2 entry existance check");
		verifyEntry(result, in.getInputStream(e1), testFile1Msg,
			"testFile1");
		verifyEntry(result, in.getInputStream(e2), testFile2Msg,
			"testFile2");
	}
	
	private void verifyEntry(TestResult result, 
							InputStream in, String msg,
							String name) 
			throws IOException{
		BufferedReader reader = new BufferedReader(
			new InputStreamReader(in));
		StringBuffer contents = new StringBuffer();
		char[] buffer = new char[4096];
		int read;
		while((read = reader.read(buffer, 0, 4096)) != -1) {
			contents.append(buffer, 0, read);
		}
		result.assertTrue(this, msg, contents.toString(),
			"verifyZip " + name + " contents check");
	}
	
	private boolean echoToPath(String msg, String path) 
		throws FileNotFoundException, IOException {
		File file = new File(path);
		file.createNewFile();
		file.deleteOnExit();
		PrintWriter pr = new PrintWriter(new BufferedOutputStream(
			new FileOutputStream(file)));
		if(pr.checkError()) {
			pr.close();
			return false;
		}
		pr.print(msg);
		pr.close();
		return !pr.checkError();
	}
}
