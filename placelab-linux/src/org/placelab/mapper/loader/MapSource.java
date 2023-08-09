/*
 * Created on Aug 20, 2004
 *
 */
package org.placelab.mapper.loader;

import org.placelab.collections.Iterator;
import org.placelab.core.Coordinate;

/**
 * 
 */
public interface MapSource {
	
	public String getName();
	
	public boolean isDefault();
	
	public Iterator query(Coordinate one, Coordinate two) throws MapSourceException;
}
