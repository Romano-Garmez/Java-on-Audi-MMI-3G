/*
 * Created on Aug 2, 2004
 *
 */
package org.placelab.mapper;

import org.placelab.collections.HashMap;
import org.placelab.core.Coordinate;
import org.placelab.core.Types;

/**
 * 
 */
public class UnknownBeacon extends Beacon {
	private HashMap map;
	
	public void fromHashMap(HashMap map) {
		this.map = map;
	}

	public HashMap toHashMap() {
		HashMap m = new HashMap();
		m.putAll(map);
		return m;
	}

	public String getId() {
		return (String)map.get(Types.ID);
	}

	public String getType() {
		String type = (String)map.get(Types.TYPE);
		return (type != null ? type : Types.UNKNOWN);
	}

    public Coordinate getPosition() {
    	String lat = (String)map.get(Types.LATITUDE);
    	String lon = (String)map.get(Types.LONGITUDE);
    	return (lat != null && lon != null ? Types.newCoordinate(lat, lon) : null);
    }

    public int getMaximumRange() {
        return 0;
    }
}
