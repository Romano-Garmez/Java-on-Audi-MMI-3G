/*
 * Created on Aug 11, 2004
 *
 */
package org.placelab.mapper;

import java.io.IOException;

import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.core.Coordinate;
import org.placelab.core.TwoDCoordinate;


/**
 * A Mapper that does lookups on wigle.net for beacons.
 * Since it queries wigle.net for each (new) lookup, it is
 * is very slow, and further makes it such that the location
 * computation is no longer completely client side.  Still,
 * it is useful for comparisons of accuracy and coverage between
 * wigle and other sources of beacon data.
 * @see WigleDownloader
 * 
 */
public class WigleMapper extends AbstractMapper {
	private WigleDownloader downloader;
	
	public WigleMapper() throws IOException {
		super(true);
		
		downloader = new WigleDownloader();
		downloader.authenticate();
	}
	
	protected LinkedList findBeaconsImpl(String id) {
		Iterator iter = downloader.query("netid=" + id.toUpperCase());
		if (!iter.hasNext()) return null;
		
		StringBuffer buffer = new StringBuffer();
		while (iter.hasNext()) {
			buffer.append(iter.next().toString());
			if (iter.hasNext()) buffer.append("\n");
		}
		
		LinkedList rv = getBeaconsFromStorageString(buffer.toString());
		return rv;
	}

	protected boolean putBeaconsImpl(String id, LinkedList beacons) {
		throw new UnsupportedOperationException("WigleMapper does not support put operations");
	}

	public boolean open() {
		return true;
	}

	public boolean close() {
		return true;
	}

	public boolean deleteAll() {
		throw new UnsupportedOperationException("WigleMapper is not mutable");
	}

	public boolean isOpened() {
		return true;
	}

	public void startBulkPuts() {}

	public void endBulkPuts() {}
	
	public Iterator query(Coordinate c1, Coordinate c2) {
		return new WigleMapperIterator(downloader.query((TwoDCoordinate)c1, (TwoDCoordinate)c2));
	}
	
	private class WigleMapperIterator implements Iterator {
		private Iterator iter;
		
		public WigleMapperIterator(Iterator iterator) {
			iter = iterator;
		}

		public boolean hasNext() {
			return iter.hasNext();
		}

		public Object next() {
			return createBeacon(iter.next().toString());
		}
		
		public void remove() {
			iter.remove();
		}
	}
}
