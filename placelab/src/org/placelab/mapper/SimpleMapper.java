package org.placelab.mapper;

import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.core.Coordinate;

/**
 * A stub/do-nothing Mapper meant to be subclassed
 */
public abstract class SimpleMapper implements Mapper {
	public Beacon findBeacon(String id) {
		LinkedList b = findBeacons(id);
		if ((b==null) || (b.size() == 0)) return null;
		return ((Beacon)((LinkedList)b).getFirst());
	}
	
	public boolean putBeacons(String id, LinkedList list) {
		return false;
	}
	public boolean putBeacon(String id, Beacon beacon) {
		return false;
	}
	public Beacon createBeacon(String keyValuePairs) {
		return null;
	}

	public boolean close() {
		return false;
	}
	public boolean deleteAll() {
		return false;
	}
	public void endBulkPuts() {
	}
	public boolean isOpened() {
		return true;
	}
	public boolean open() {
		return false;
	}
	public void startBulkPuts() {
	}
	public Iterator iterator() {
		return null;
	}
	public Iterator query(Coordinate c1, Coordinate c2) {
		return null;
	}
}
