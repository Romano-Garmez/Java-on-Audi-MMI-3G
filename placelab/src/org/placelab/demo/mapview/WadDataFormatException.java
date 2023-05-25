package org.placelab.demo.mapview;

public class WadDataFormatException extends Exception {
	public static String MISSING_RESOURCE_ERROR =
		"A needed resource was not found in the map wad";
	public static String BAD_RESOURCE_ERROR =
		"A needed resource in the map wad was corrupted";
	
	private String referencedFrom;
	private String badEntryLocation;
	private String failCode;
	private Throwable resourceProblem;

	public WadDataFormatException(String referencedFrom,
								  String badEntryLocation,
								  String failCode,
								  Throwable resourceProblem,
								  String message)
	{
		super(message);
		this.referencedFrom = referencedFrom;
		this.badEntryLocation = badEntryLocation;
		this.failCode = failCode;
		this.resourceProblem = resourceProblem;
	}
	public WadDataFormatException(String referencedFrom,
								  String badEntryLocation,
								  String failCode,
								  String message) {
		this(referencedFrom, badEntryLocation,
				failCode, null, message);
	}
	public WadDataFormatException(String badEntryLocation,
								  String failCode,
								  String message) {
		this("format specification", badEntryLocation, failCode, null, message);
	}
	public WadDataFormatException(String badEntryLocation,
								  String failCode,
								  Throwable resourceProblem,
								  String message) {
		this("format specification", badEntryLocation, failCode, 
				resourceProblem, message);
	}	
	
	public String failCode() {
		return failCode;
	}
	public String badEntryLocation() {
		return badEntryLocation;
	}
	public String referencedFrom() {
		return referencedFrom;
	}
	
	public String getMessage() {
		return (super.getMessage() == null ? "" : super.getMessage())
		+ "failCode: " + failCode() + " for resource: " +
		badEntryLocation() + " referenced from: " + referencedFrom()
		+ (resourceProblem == null ? "" : 
		"with problem: " + resourceProblem.toString());
	}
}
