package org.placelab.midp;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordFilter;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.collections.UnsupportedOperationException;
import org.placelab.core.Coordinate;
import org.placelab.core.Types;
import org.placelab.mapper.AbstractMapper;
import org.placelab.mapper.Beacon;
import org.placelab.util.StringUtil;

/**
 * Stores known beacons on a mobile phones Record Management Store. 
 */
public class RMSMapper extends AbstractMapper {
	public static final String RECORDSTORE_NAME = "placelab-beacons";
	protected boolean opened = false;

	protected String rmsName;

	protected RecordStore rms;

	/**
	 * Construct an RMSMapper object using the default record store name
	 */
	public RMSMapper() {
		this(RECORDSTORE_NAME);
	}

	/**
	 * Construct an RMSMapper object
	 * @param _rmsName name of the record store to use
	 */
	public RMSMapper(String _rmsName) {
		super(true); //should cache
		this.rmsName = _rmsName;
	}

	/**
	 * @return number of beacons in RMS
	 */
	public synchronized int numBeacons() {
	    try {
	        int ret;
		    if(opened) {
		        ret= rms.getNumRecords();
		    } else {
		        open();
		        ret = rms.getNumRecords();
		        close();
		    }
		    return ret;
	    } catch(RecordStoreException rse) {
	        return -1;
	    }
	}

	/**
	 * Opens a connection to the named record store in the RMS.
	 * Creates a record store if nonexistant already.
	 * @return boolean true if open was successful, false otherwise
	 */
	public synchronized boolean open() {
		try {
			rms = RecordStore.openRecordStore(rmsName, true,
					RecordStore.AUTHMODE_PRIVATE, true);
			opened = (rms != null);
		} catch (RecordStoreException rse) {
			close();
			return false;
		}
		return opened;
	}

	/**
	 * Close the record store
	 * @return true if close was successful, false otherwise
	 */
	public synchronized boolean close() {
		if (rms != null) {
			try {
				rms.closeRecordStore();
				opened = false;
			} catch (RecordStoreException rse) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return true if record store is open, false otherwise
	 */
	public boolean isOpened() {
		return opened;
	}

	/**
	 * Deletes the entire record store from the RMS.
	 */
	public synchronized boolean deleteAll() {
		boolean wasOpened = opened;
		try {
			//close this down
			if (wasOpened) {
				if (!close())
					return false;
			}
			RecordStore.deleteRecordStore(rmsName);

			//reopen it
			if (wasOpened) {
				if (!open())
					return false;
			}
		} catch (RecordStoreException rse) {
			rse.printStackTrace();
			return false;
		}
		return true;
	}

	/* Add a new beacon to the RMS */
	protected synchronized boolean putBeaconsImpl(String id, LinkedList beacons) {
		StringBuffer sb = new StringBuffer();
		if(beacons == null || id == null) return false;
		for (Iterator it = beacons.iterator(); it.hasNext();) {
			Beacon b = (Beacon) it.next();
			if(b == null) continue;
			HashMap map = b.toHashMap();

			/* add the beacon type to the HashMap */
			if (map.get(Types.TYPE) == null) {
				map.put(Types.TYPE, b.getType());
			}
			sb.append(StringUtil.hashMapToStorageString(map));
			if (it.hasNext())
				sb.append('\n');
		}
		if (!putBeaconRaw(sb.toString()))
			return false;

		return true;
	}

	public synchronized boolean putBeaconRaw(String storageString) {
		String rmsString = storageString;
		try {
			byte[] b = rmsString.getBytes();
			rms.addRecord(b, 0, b.length);
			return true;
		} catch (RecordStoreException rse) {
			return false;
		}
	}

	public class IDFilter implements RecordFilter {
		private String searchString;
		
		public IDFilter(String id) {
			this.searchString = Types.ID+'='+id;
		}
		
		public boolean matches(byte[] candidate) {
			return StringUtil.match(new String(candidate),"|",searchString);
		}
			
	}

	/* search the RMS looking for the id */
	protected synchronized LinkedList findBeaconsImpl(String uniqueId) {
		if (uniqueId == null || rms == null) {
			return null;
		}

		try {
			IDFilter filter = new IDFilter(uniqueId);
			RecordEnumeration re = rms.enumerateRecords(filter, null, false);
			if(re == null || !re.hasNextElement()) return null;
			byte[] beaconBytes = re.nextRecord();
			String storageString = new String(beaconBytes).toString();
			return getBeaconsFromStorageString(storageString);
		} catch (RecordStoreException rse) {
			return null;
		}
	}

		
	public void startBulkPuts() {
		//unused
	}

	public void endBulkPuts() {
		//unused
	}

	private class RMSIterator implements Iterator {
		private RecordEnumeration enum;
		private Iterator beaconListIter;

		private Beacon nextBeacon;

		public RMSIterator() {
			try {
				if (!opened)
					open();
				//only want beacons, not the type map
				if(rms == null) return;
				enum = rms.enumerateRecords(null, null, false);
				nextBeacon = getNext();
			} catch (RecordStoreNotOpenException rsnoe) {
				enum = null;
			}
		}

		public boolean hasNext() {
			return (nextBeacon != null);
		}

		public Object next() {
			if (nextBeacon == null)
				return null;
			Object rv = nextBeacon;
			nextBeacon = getNext();
			return rv;
		}

		public Beacon getNext() {
			if (enum == null
					|| (beaconListIter == null && !enum.hasNextElement()))
				return null;

			if (beaconListIter == null || !beaconListIter.hasNext()) {
				try {
					beaconListIter = null;
					while (enum.hasNextElement()) {
						byte[] b = enum.nextRecord();
						String s = new String(b);
						LinkedList list = getBeaconsFromStorageString(s);
						if (list != null) {
							beaconListIter = list.iterator();
							if (beaconListIter.hasNext())
								break;
							else
								beaconListIter = null;
						}
					}
					
				} catch (RecordStoreException rse) {
				}
			}

			if (beaconListIter != null)
				return (Beacon) beaconListIter.next();
			return null;

		}

		public void remove() {
			throw new UnsupportedOperationException("remove() not supported for RMSMMapper iterators");
		}
	}

	public Iterator iterator() {
		return new RMSIterator();
	}

	public Iterator query(Coordinate c1, Coordinate c2) {
		return new BoundedIterator(c1, c2);
	}
	private class BoundedIterator implements Iterator {
		private Coordinate c1, c2;
		private Iterator it;
		private Beacon nextBeacon;
		public BoundedIterator(Coordinate c1, Coordinate c2) {
			this.c1 = c1;
			this.c2 = c2;
			this.it = iterator();
			nextBeacon = getNext();
		}
		
		public boolean hasNext() {
			return (nextBeacon != null);
		}
		public Object next() {
			Object o = nextBeacon;
			nextBeacon = getNext();
			return o;
		}
		public void remove() {
			throw new UnsupportedOperationException("remove() not supported for RMSMMapper iterators");
		}
		private Beacon getNext() {
			while (it.hasNext()) {
				Beacon b = (Beacon) it.next();
				if(b == null) continue;
				Coordinate c = b.getPosition();
				if(c == null) continue;
				if (c.within(c1,c2)) return b;
			}
			return null;
		}
	}
}