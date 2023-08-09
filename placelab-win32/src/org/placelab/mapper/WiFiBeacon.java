/*
 * Created on Jun 28, 2004
 *
 */
package org.placelab.mapper;

import org.placelab.collections.HashMap;
import org.placelab.core.Coordinate;
import org.placelab.core.Types;
import org.placelab.util.StringUtil;

/**
 * 
 */
public class WiFiBeacon extends Beacon implements NeighborhoodBeacon {
	private String id;
	private String ssid;
	private Coordinate position;
	private String[] neighbors;
	private HashMap keys;

	public WiFiBeacon() {
		keys = new HashMap();
		keys.put(Types.TYPE,Types.WIFI);
	}
	/* create a new beacon from the old format tab-separated storage string */

	public WiFiBeacon(String[] sarr) {
		keys = new HashMap();
		keys.put(Types.TYPE,Types.WIFI);
		setId(StringUtil.canonicalizeBSSID(sarr[3]));
		setSsid(sarr[2]);
		Coordinate c = Types.newCoordinate(sarr[0], sarr[1]);
		setPosition(c);
	}
	
	public String getType() { return Types.WIFI; }

	/**
	 * @return Returns the humanReadableName.
	 */
	public String getSsid() {
		return ssid;
	}

	/**
	 * @param val The humanReadableName to set.
	 */
	public void setSsid(String val) {
		ssid = val;
		keys.put(Types.HUMANREADABLENAME,val);
	}

	/**
	 * @return Returns the position.
	 */
	public Coordinate getPosition() {
		return position;
	}

	/**
	 * @param pos The position to set.
	 */
	public void setPosition(Coordinate pos) {
		position = pos;
		keys.put(Types.LATITUDE,pos.getLatitudeAsString());
		keys.put(Types.LONGITUDE,pos.getLongitudeAsString());
	}

	/**
	 * @return Returns the uniqueId.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param uniqueId
	 *            The uniqueId to set.
	 */
	public void setId(String uniqueId) {
		id = uniqueId;
		keys.put(Types.BSSID,uniqueId);	
	}
	
	public String[] getNeighborhood() {
		return neighbors;
	}
	
	public void setNeighborhood(String[] nbrs) {
		neighbors = nbrs;
	}
	
	public HashMap getKeyValues() {
		return keys;
	}
	
	public void fromHashMap(HashMap keyValues) {
		id = (String) keyValues.get(Types.ID);
		ssid = (String) keyValues.get(Types.HUMANREADABLENAME);
		Coordinate c = Types.newCoordinate(keyValues);
		position = c;
		String nbrs = (String) keyValues.get(Types.NEIGHBORHOOD);
		if (nbrs==null) neighbors = null;
		else neighbors = StringUtil.split(nbrs, '|');
		keys = keyValues;
	}

	public HashMap toHashMap() {
		return keys;
//		HashMap map = new HashMap();
//		map.put(Types.ID, id);
//		map.put(Types.TYPE, Types.WIFI);
//		if (ssid != null) map.put(Types.HUMANREADABLENAME, ssid);
//		map.put(Types.LATITUDE, position.getLatitudeAsString());
//		map.put(Types.LONGITUDE, position.getLongitudeAsString());
//		if (neighbors != null) map.put(Types.NEIGHBORHOOD, StringUtil.join(neighbors, '|'));
//		return map;
	}
	
	public int getMaximumRange() {
	    return 100; 
	}
	
//	public void fromHashMap(HashMap keyValues) {
//		System.out.println("MAKING ONE");
//		super.fromHashMap(keyValues);
//		String bucketSizeStr = (String)keyValues.get("BS");
//		String maxIdxStr = (String)keyValues.get("MAX");
//		int bucketSize = -1;
//		int maxIdx = -1;
//		try {
//			bucketSize = Integer.parseInt(bucketSizeStr);
//			maxIdx = Integer.parseInt(maxIdxStr);
//		} catch (Exception ex) {;}
//		
//		if ((bucketSize < 0) || (maxIdx < 0)) {
//			System.out.println("Ahhhhh!!!!");
//		}
//	}
	
}
