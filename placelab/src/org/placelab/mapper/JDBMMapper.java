package org.placelab.mapper;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import jdbm.JDBMEnumeration;
import jdbm.JDBMHashtable;
import jdbm.JDBMRecordManager;

import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.collections.UnsupportedOperationException;
import org.placelab.core.Coordinate;
import org.placelab.core.PlacelabProperties;
import org.placelab.core.TwoDCoordinate;
import org.placelab.core.Types;
import org.placelab.util.StringUtil;

/**
 * This class uses a persistent cache of known beacons to fill in the beacon fields of BeaconMeasurements.
 */
public class JDBMMapper extends AbstractMapper {
	/** The name of the database file that will be creates to hold the AP data **/
	private String dbName=null;
	private JDBMRecordManager recman=null;
	private JDBMHashtable hashtable=null;
	
	/** The name of the system property that is looked in for the database name **/
	//public static final String DB_PROPERTY = "placelab.mapperdb";
	
	private static final String AP_TABLE_NAME = "aps";
	
	/** Create a JDBMMapper using the default database name **/
	public JDBMMapper() throws IOException {
		this(null, true);
	}
	public JDBMMapper(boolean shouldCache) throws IOException {
		this(null, shouldCache);
	}
	public JDBMMapper(String dbName) throws IOException {
		this(dbName, true);
	}
	public JDBMMapper(String dbName, boolean shouldCache) throws IOException {
		super(shouldCache);
		this.dbName = (dbName==null ? PlacelabProperties.get(PlacelabProperties.JDBM_DB_PROPERTY) : dbName);
		open();
	}
	
	public boolean isOpened() { return hashtable!=null; }
	
	public boolean open() {
		if (isOpened()) return true;

		try {
			// open up the map
			recman = new JDBMRecordManager(dbName);
			hashtable = recman.getHashtable(AP_TABLE_NAME);	
			hashtable.dispose();
			recman.close();
			// not sure why, but sometimes the first open fails, but the secons one always
			// succeeds. (Voodoo applied)
			recman = new JDBMRecordManager(dbName);
			hashtable = recman.getHashtable(AP_TABLE_NAME);
			//System.out.println("map opened with size " + size());
		} catch (IOException ex) {
			close();
			return false;
		}
		return true;
	}
	
	public boolean close() {
		if (hashtable != null) {
			try {
				hashtable.dispose();
			} catch (IOException e) {
				// ignore this error
			}
			hashtable = null;
		}
		if (recman != null) {
			try {
				recman.close();
			} catch (IOException e) {
				// ignore this error
			}
			recman = null;
		}
		return true;
	}
	
//	/** Debugging call that prints the entire beacon map to the console **/
//	public synchronized void print_entire_map() {
//		try {
//			JDBMEnumeration keys = hashtable.keys();
//			for (Object key = keys.nextElement(); 
//			     keys.hasMoreElements();
//			     key = keys.nextElement()) {
//				Object val   = hashtable.get(key);
//				String key_s = (String)key;
//				String val_s = (String)val;
//				System.out.println("Key: " + 
//						   ((key_s == null) ? 
//						    "NULL" : key_s) +
//						   " Value: " +
//						   ((val_s == null) ? 
//						    "NULL" : val_s));
//			}
//		} catch (IOException ex) {
//			ex.printStackTrace();
//			System.exit(1); //### Ian said to do this
//		}
//	}
	
	/** Look to see if the reading's measurement is for a known AP.  If so return it, otherwise it returns UNKNOWN_BEACON **/
	protected synchronized LinkedList findBeaconsImpl(String uniqueId) {
		// JWS: for a unique ID, could use class name concatenated with
		// reading's ID, separated by a colon (which is illegal char in class name)
		if (uniqueId == null) {
			return null;
		}
		String val;
		try {
			val = (String) hashtable.get(uniqueId);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1); //### Ian said to do this
			return null;
		}
		return getBeaconsFromStorageString(val);
	}
	
	


	/**
	 * makes addition go much faster, use this for bulk loading
	 */
	public synchronized void startBulkPuts() {
		recman.disableTransactions();
	}
	public synchronized void endBulkPuts() {
	}

	/**
	 * returns the number of Beacons in the map
	 */
	public int size() {
		int rv = 0;
		try {
			JDBMEnumeration enum;
			try {
				enum = hashtable.keys();
			} catch (EOFException ex) {
				return 0;
			}
		 	while (enum.hasMoreElements()) {
		 		Object key = enum.nextElement();
		 		rv++;
		 	}
		} catch (IOException ex) {
			ex.printStackTrace();
			return -1;
		}		 
	return rv;
	}

	/**
	 * Use this with caution, it wipes out the persistent map.
	 */
	public synchronized boolean deleteAll() {
		boolean wasOpened = isOpened();
		if (wasOpened) {
			if (!close()) return false;
		}
		new File(dbName + ".db").delete();
		new File(dbName + ".lg").delete();
		if (wasOpened) {
			if (!open()) return false;
		}
		return true;
	}
	
	/**
	 * add a new list of beacons to the map. Returns true on success
	 */
	protected boolean putBeaconsImpl(String id, LinkedList beacons) {
		StringBuffer sb=new StringBuffer();
		for (Iterator it=beacons.iterator(); it.hasNext(); ) {
			Beacon b = (Beacon) it.next();
			HashMap map = b.toHashMap();
			
			/* add the beacon type to the HashMap */
			if (map.get(Types.TYPE) == null) {
				map.put(Types.TYPE, b.getType());
			}
			String str = StringUtil.hashMapToStorageString(map);
			sb.append(str);
			if (it.hasNext()) sb.append('\n');
		}
		if (!putBeaconsImpl(id, sb.toString())) return false;
		return true;
	}
	
	private synchronized boolean putBeaconsImpl(String id, String storageString) {
		try {
			hashtable.put(id, storageString);
			return true;
		} catch (IOException ex) {
			System.out.println("Had a problem with: " + ex.getMessage() + " '" + id);
			ex.printStackTrace(System.err);
			// are we going to do error logging? Run Levels?
			System.exit(1);
			return false;
		}
	}
	
	
	
	private class JDBMIterator implements Iterator {
		private JDBMEnumeration enum;
		private Iterator beaconListIter;
		private Beacon nextBeacon;
		
		JDBMIterator() {
			nextBeacon = null;
			if (hashtable != null) {
				try {
					enum = hashtable.values();
					nextBeacon = getNext();
				} catch (IOException e) {
					enum = null;
				}	
			}
			else
				enum = null;
		}

		public boolean hasNext() {
			return (nextBeacon != null);
		}

		public Object next() {
			if (nextBeacon == null) return null;
			Object rv = nextBeacon;
			nextBeacon = getNext();
			return rv;
		}

		public void remove() {
			throw new UnsupportedOperationException("remove() not supported for JDBMMapper iterators");
		}
		
		/* skip the beacon type table */
		private Beacon getNext() {
			try {
				if (enum==null || (beaconListIter==null && !enum.hasMoreElements())) return null;
			} catch (IOException e) {
				return null;
			}
			
			if (beaconListIter==null || !beaconListIter.hasNext()) {
				try {
					beaconListIter = null;
					while (enum.hasMoreElements()) {
						Object o = enum.nextElement();
						if (o != null) {
							LinkedList list = getBeaconsFromStorageString((String)o);
							if (list != null) {
								beaconListIter = list.iterator();
								if (beaconListIter.hasNext()) break;
								else beaconListIter = null;
							}
						}
					}
				} catch (IOException e) {
				}
			}
			if (beaconListIter != null) return (Beacon) beaconListIter.next();
			return null;
		}
	}
	
	public Iterator iterator() {
		return new JDBMIterator();
	}

	private class BoundedIterator implements Iterator {
		private Iterator it = null;
		private Object next = null;
		double lat1,  lon1, lat2, lon2;
		
		public BoundedIterator(Iterator it,double lat1, double lon1, double lat2, double lon2) {
			this.it = it;
			this.lat1 = lat1;
			this.lon1 = lon1;
			this.lat2 = lat2;
			this.lon2 = lon2;
			loadNext();
		}
		
		private void loadNext() {
			while (true) {
				if (!it.hasNext()) {
					return; // all done, no more
				}
				Beacon b = (Beacon)it.next();
				if (inside(b)) {
					next = b;
					return;
				}
			}
		}
		
		public boolean hasNext() {
			return next != null;
		}
		
		public Object next() {
			Object rv = next;
			next = null;
			loadNext();
			return rv;
		}
		
		private boolean inside(Beacon b) {
			if (!(b.getPosition() instanceof TwoDCoordinate))  {
				return false;
			}
			double lat = ((TwoDCoordinate)b.getPosition()).getLatitude();
			double lon = ((TwoDCoordinate)b.getPosition()).getLongitude();
			return (lat1 <= lat) && (lat2 >= lat) && (lon1 <= lon) && (lon2 >= lon);
		}

		public void remove() { // no idea what this does...
			throw new UnsupportedOperationException("remove() not supported for BoundedMapper iterators");
		}
	}
	
	public Iterator query(Coordinate c1, Coordinate c2) {
		double lat1,  lon1,  lat2,  lon2;
		double td;
		if ((!(c1 instanceof TwoDCoordinate)) || (!(c2 instanceof TwoDCoordinate)))  {
			return new HashMap().keySet().iterator(); // empty
		}
		lat1 = ((TwoDCoordinate)c1).getLatitude();
		lon1 = ((TwoDCoordinate)c1).getLongitude();
		lat2 = ((TwoDCoordinate)c2).getLatitude();
		lon2 = ((TwoDCoordinate)c2).getLongitude();
		// make sure lat lon in right orders
		if (lat1 > lat2) {
		  td = lat1;
		  lat1 = lat2;
		  lat2 = td;
		}
		if (lon1 > lon2) {
		  td = lon1;
		  lon1 = lon2;
		  lon2 = td;
		}
		// return an iterator that filters on these lat and lons
		return new BoundedIterator(iterator(),lat1,lon1,lat2,lon2);
	}
	

	public String getDbName() { return dbName; }
}
