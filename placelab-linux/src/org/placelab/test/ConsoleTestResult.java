package org.placelab.test;

import java.io.PrintWriter;



/**
 * All tests produce a TestResult.
 *
 */
public class ConsoleTestResult extends TestResult {

	private PrintWriter pw;
	private boolean printHTML;

	public static final String SHOW_ERROR_TRACE="irs.showErrorTrace";

/** The test results are written to pw and the results are either simple HTML if printHTML is true, or plain text. **/	
	public ConsoleTestResult(PrintWriter pw, boolean printHTML) {
		this.pw = pw;
		this.printHTML = printHTML;
	}
	
	public ConsoleTestResult(PrintWriter pw, boolean printHTML, boolean haveNetwork_NOTUSED) {
		this(pw,printHTML);
	}

	public void print(String msg) {
		if (printHTML) {
			pw.println("<font face=Curier>" + msg + "</font><br>");
		} else {
			pw.println(msg);
		}
	}

	public void exceptionExtra(Throwable t) {
		if (true || System.getProperty(SHOW_ERROR_TRACE)!=null) {
			t.printStackTrace(System.err);
		}

	}
}

