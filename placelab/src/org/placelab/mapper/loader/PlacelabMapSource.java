/*
 * Created on Aug 20, 2004
 *
 */
package org.placelab.mapper.loader;

import java.io.InputStream;

import org.placelab.collections.Iterator;
import org.placelab.core.Coordinate;
import org.placelab.core.TwoDCoordinate;
import org.placelab.mapper.MapUtils;

/**
 * 
 */
public class PlacelabMapSource extends StreamMapSource {
	public final static String BASE_URL = "http://www.placelab.org/data/do-retrieve.php";
	
	public PlacelabMapSource() {
		super("Placelab.org", true);
	}
	
	public Iterator query(Coordinate one, Coordinate two) {
		TwoDCoordinate c1 = (TwoDCoordinate)one;
		TwoDCoordinate c2 = (TwoDCoordinate)two;
		String query = "left=" + Math.min(c1.getLongitude(), c2.getLongitude()) + 
				"&right=" + Math.max(c1.getLongitude(), c2.getLongitude()) +
				"&top=" + Math.max(c1.getLatitude(), c2.getLatitude()) +
				"&bottom=" + Math.min(c1.getLatitude(), c2.getLatitude());
		InputStream is = MapUtils.getHttpStream(BASE_URL + "?style=new&" + query);
		if(is == null) {
		    System.err.println("Cannot open URL: " + BASE_URL + "?style=new&" + query);
		    return null;
		}
		return queryImpl(is);
	}
}
