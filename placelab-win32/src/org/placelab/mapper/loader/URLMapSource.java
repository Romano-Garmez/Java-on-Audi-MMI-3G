/*
 * Created on Aug 25, 2004
 *
 */
package org.placelab.mapper.loader;

import java.io.InputStream;

import org.placelab.collections.Iterator;
import org.placelab.core.Coordinate;
import org.placelab.mapper.MapUtils;

/**
 * 
 */
public class URLMapSource extends StreamMapSource {
	protected String url;
	
	public URLMapSource(String name, String source) {
		super(name, false);
		url = source;
	}
	
	public Iterator query(Coordinate one, Coordinate two) {
		// XXX: doesn't pay attention to the coordinate constraints
	    InputStream is = MapUtils.getHttpStream(url);
	    if(is == null) {
	        System.err.println("Cannot open URL: " + url);
	        return null;
	    }
		return queryImpl(is);
	}

}
