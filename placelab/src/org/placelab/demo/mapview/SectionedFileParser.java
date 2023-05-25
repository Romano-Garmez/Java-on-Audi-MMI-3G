/**
 * SectionedFileParser parses sectioned files of the form:
 * [section]
 * attribute=value
 * attribute=value
 * 
 * [other section]
 * attribute=value
 * 
 * It turns those files into Hashtables of (section => Hashtable of 
 * (attribute => value).  Subsections are not supported.
 */
package org.placelab.demo.mapview;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

public class SectionedFileParser {
	protected Hashtable sections;
	
	private int atLine;
	
	public SectionedFileParser(InputStream in) 
		throws IOException, SectionedFileFormatException 
	{
		sections = new Hashtable();
		atLine = 0;
		this.parse(new BufferedReader(new InputStreamReader(in)));
	}
	public SectionedFileParser(String path)
		throws IOException, SectionedFileFormatException
	{
		this(new FileInputStream(path));
	}
	
	public Hashtable getSection(String sectionName) {
		return (Hashtable)sections.get(sectionName);
	}
	public Hashtable allSections() {
		return sections;
	}
	
	
	private void parse(BufferedReader reader) 
		throws IOException, SectionedFileFormatException
	{
		String line;
		String sectionName = null;
		Hashtable currentSection = null;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(line.startsWith("#")) continue;
			if(line.length() == 0) continue;
			if(line.startsWith("[") && line.endsWith("]")) {
				if(sectionName != null) {
					sections.put(sectionName, currentSection);
				}
				sectionName = stripBrackets(line);
				if(sectionName.length() == 0) throwBadName();
				currentSection = new Hashtable();
			} else if(currentSection != null) {
				String[] keyValue = keyAndValue(line);
				currentSection.put(keyValue[0], keyValue[1]);
			} else {
				throwBadLine();
			}
			atLine++;
		}
		// put the last section
		if(sectionName != null) {
			sections.put(sectionName, currentSection);
		}
		reader.close();
	}
	
	private String stripBrackets(String line) 
		throws SectionedFileFormatException {
		try {
			return line.substring(1, line.length() - 1);
		} catch(IndexOutOfBoundsException iobe) {
			throwBadLine();
		}
		// never get here
		return " ";
	}
	private String[] keyAndValue(String line) 
		throws SectionedFileFormatException {
		String[] ret = new String[2];
		int middle = line.indexOf('=');
		if(middle == -1) {
			throwNoEquals();
		}
		try {
			ret[0] = line.substring(0, middle);
			ret[1] = line.substring(middle + 1, line.length());
			ret[0] = ret[0].trim();
			ret[1] = ret[1].trim();
		} catch(IndexOutOfBoundsException iobe) {
			throwBadLine();
		}
		return ret;
	}
	
	private void throwBadLine() throws SectionedFileFormatException {
		throw new SectionedFileFormatException(
				SectionedFileFormatException.MALFORMED_LINE_ERROR,
				atLine);
	}
	private void throwNoEquals() throws SectionedFileFormatException {
		throw new SectionedFileFormatException(
				SectionedFileFormatException.MISSING_EQUAL_ERROR,
				atLine);
	}
	private void throwBadName() throws SectionedFileFormatException {
		throw new SectionedFileFormatException(
			SectionedFileFormatException.BAD_SECTION_NAME_ERROR, 
			atLine);
	}
}