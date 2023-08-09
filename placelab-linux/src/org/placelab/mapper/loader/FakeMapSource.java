/*
 * Created on Aug 20, 2004
 *
 */
package org.placelab.mapper.loader;

import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.core.Coordinate;

/**
 * For testing purposes
 * 
 */
public class FakeMapSource implements MapSource {
	protected String name;
	
	public FakeMapSource(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public boolean isDefault() {
		return false;
	}


	public Iterator query(Coordinate one, Coordinate two) {
		LinkedList list = new LinkedList();
		return null;
	}	
}
