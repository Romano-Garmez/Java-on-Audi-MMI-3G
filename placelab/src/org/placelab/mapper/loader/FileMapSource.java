/*
 * Created on Aug 25, 2004
 *
 */
package org.placelab.mapper.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.placelab.collections.Iterator;
import org.placelab.core.Coordinate;

/**
 * 
 */
public class FileMapSource extends StreamMapSource {
	private FileInputStream stream;
	
	public FileMapSource(String path) throws FileNotFoundException  {
		super("File: " + new File(path).getName(), false);
		
		stream = new FileInputStream(path);
	}
	
	public boolean isDefault() {
		return stream != null;
	}

	public Iterator query(Coordinate one, Coordinate two) {
		// XXX: is not selective based on coordinates
		return stream == null ? null : new StreamIterator(stream);
	}
	
}
