/*
 * Created on Jun 29, 2004
 *
 */
package org.placelab.mapper;

import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.core.Coordinate;
import org.placelab.core.Types;
import org.placelab.util.StringUtil;

/**
 * Provides convenience methods for packing and unpacking Beacons
 * in a Mapper, for caching Beacons in memory when they are used,
 * and for storing Beacons of multiple types.  Only Mapper implementers
 * should be concerned with this class.
 */
public abstract class AbstractMapper implements Mapper {
	/** A storage space for Beacons that have already been parsed.
	 */
	private HashMap cache=null;
	private HashMap beaconTypeToClassMap = null;
	
	public static Class UNKNOWN_BEACON_CLASS;
	
	static{
		try {
			UNKNOWN_BEACON_CLASS = Class.forName("org.placelab.mapper.UnknownBeacon");
		} catch(ClassNotFoundException e) {
			throw new RuntimeException("Could not load class in AbstractMapper");
		}
	}
	
	public AbstractMapper(boolean shouldCache) {
		if (shouldCache) {
			cache = new HashMap();
		} else {
			cache = null;
		}
		
		addBeaconClass(new WiFiBeacon());
		addBeaconClass(new BluetoothBeacon());
		addBeaconClass(new GSMBeacon());
	}
	
	public void addBeaconClass(String type, String className) {
		if (beaconTypeToClassMap == null) {
			beaconTypeToClassMap = new HashMap();
		}
		beaconTypeToClassMap.put(type, className);
	}
	public void addBeaconClass(Beacon beacon) {
		addBeaconClass(beacon.getType(), beacon.getClass().getName());
	}
	
	public Beacon findBeacon(String id) {
		LinkedList b = findBeacons(id);
		if (b==null || b.isEmpty()) return null;
		return ((Beacon)((LinkedList)b).getFirst());
	}
	
	public LinkedList findBeacons(String id) {
		if (id == null) {
			return null;
		}

		if (cache != null) {
			Object cached = cache.get(id);
			if (cached != null) {
				if (cached instanceof String && ((String)cached).equals("null")) {
					return null;
				} else {
					return (LinkedList)cached;
				}
			}
		}
		
		LinkedList beacons = findBeaconsImpl(id);
		if (cache != null) {
			if (beacons == null) {
				cache.put(id, "null");
			} else {
				cache.put(id, beacons);
			}
		}
		return beacons;		
	}
	
	/* The following methods are used by MapLoaders to create new Mapper databases */
	public boolean putBeacon(String id, Beacon beacon) {
		/* invalidate the cache entry for this beacon */
		if(cache != null) cache.remove(id);
		LinkedList beacons = new LinkedList();
		beacons.add(beacon);
		return putBeaconsImpl(id, beacons);
	}
	
	public boolean putBeacons(String id, LinkedList beacons) {
		/* invalidate the cache entry for this beacon */
		if(cache != null) cache.remove(id);
		return putBeaconsImpl(id, beacons);
	}

	
//	protected String getBeaconType(Beacon b) {
//		if (beaconClassToTypeMap==null) {
//			beaconClassToTypeMap = new HashMap();
//		}
//		if (beaconTypeToClassMap==null) {
//			beaconTypeToClassMap = new HashMap();
//		}
//		String type = (String) beaconClassToTypeMap.get(b.getClass());
//		if (type == null) {
//			lastBeaconTypeId++;
//			type = ""+lastBeaconTypeId;
//			beaconClassToTypeMap.put(b.getClass(), type);
//			beaconTypeToClassMap.put(type, b.getClass().getName());
//		}
//		return type;
//	}
	private Class getBeaconClass(String type) {
		if (beaconTypeToClassMap==null || type==null) {
			return UNKNOWN_BEACON_CLASS;
		}
		String className = (String) beaconTypeToClassMap.get(type);
//		System.out.println("Hey!!!" + type);
		if (className == null) return UNKNOWN_BEACON_CLASS;
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			return UNKNOWN_BEACON_CLASS;
		}
	}

	public Beacon createBeacon(String storageString) {
		/* assume we have key/value pairs
		 */
	    if(storageString == null) return null;
		HashMap map = StringUtil.storageStringToHashMap(storageString);
		if (map==null || map.isEmpty()) {
		    // try looking for old format
			String[] sarr = StringUtil.split(storageString);
			if (sarr != null && sarr.length == 4) {
				/* this is the old format of the mapper db */
				return new WiFiBeacon(sarr);
			}
			return null;
		}
		String type = (String)map.get(Types.TYPE);
		return Beacon.create(getBeaconClass(type), map);
	}
	
	/**
	 * Unpacks a list of Beacons using {@link Mapper#createBeacon(String)}
	 * stored separated by newlines.
	 */
	protected LinkedList getBeaconsFromStorageString(String storage) {
		if (storage == null) return null;
		// jws - redundant?
//		if (storage.indexOf('\n') < 0) {
//			Beacon b = createBeacon(storage);
//			if (b==null) return null;
//			LinkedList l = new LinkedList();
//			l.add(b);
//			return l;
//		}
		String[] lines = StringUtil.split(storage, '\n');
		LinkedList list = new LinkedList();
		for (int i=0; i < lines.length; i++) {
			Beacon b = createBeacon(lines[i]);
			if (b == null) return null;
			list.add(b);
		}
		return list;
	}

	public abstract Iterator query(Coordinate c1, Coordinate c2);
	
	protected abstract LinkedList findBeaconsImpl(String id);
	protected abstract boolean putBeaconsImpl(String id, LinkedList beacons);
	public boolean overrideOnPut() { return true; }
}
