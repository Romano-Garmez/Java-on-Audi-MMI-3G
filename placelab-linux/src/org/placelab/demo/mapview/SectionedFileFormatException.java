package org.placelab.demo.mapview;

public class SectionedFileFormatException extends Exception {
	public static String MALFORMED_LINE_ERROR =
		"A line didn't have the expected format";
	public static String MISSING_EQUAL_ERROR =
		"A key value pair didn't have an equals sign separating them";
	public static String BAD_SECTION_NAME_ERROR = 
		"A section name wasn't up to snuff";
	
	
	private String failCode;
	private int failLine;
	
	public SectionedFileFormatException(String failCode,
										int failLine) {
		this.failCode = failCode;
		this.failLine = failLine;
	}
	
	public SectionedFileFormatException(String failCode,
										int failLine,
										String message) {
		super(message);
		this.failCode = failCode;
		this.failLine = failLine;
	}
	
	public String failCode() {
		return failCode;
	}
	public int failLine() {
		return failLine;
	}
	/*public String toString() {
		return super.toString() + " failCode: " + failCode() + " failLine: " + failLine();
	}*/
	public String getMessage() {
		return (super.getMessage() == null ? "" : super.getMessage()) + "failCode: " + failCode() +
		" failLine: " + failLine();
	}
	
	
}
