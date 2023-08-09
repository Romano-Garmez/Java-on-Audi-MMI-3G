/*
 * Created on Jun 28, 2004
 *
 */
package org.placelab.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.placelab.collections.LinkedList;
import org.placelab.mapper.loader.FileMapSource;
import org.placelab.mapper.loader.MapSourceLoader;
import org.placelab.mapper.loader.URLMapSource;
import org.placelab.mapper.loader.WigleMapSource;
import org.placelab.util.Cmdline;

/**
 * This utility class reloads the persistent Beacon cache from a file or URL.
 * This will completely erase the existing AP cache and then load the new access
 * points.
 */
public class MapLoader {
	protected Mapper mapper;

	/**
	 * Creates a new map loader that will name its database dbName. There can be
	 * multiple simultaneous AP cache's provided they have differnt names.
	 */
	public MapLoader(Mapper m) {
		mapper = m;
	}

	/**
	 * Creates a new map loader that will use the default database name
	 */
	public MapLoader() throws IOException {
		this(CompoundMapper.createDefaultMapper(true,false));
	}

	public Mapper getMapper() {
		return mapper;
	}
	/**
	 * Pass in either a URL or a file with a set of known APs as an argument to
	 * this program. The current public map should always be available at
	 * http://data.placelab.org/do-retrieve.php
	 */
	public static void main(String[] args) {
		String dataLoc;
		boolean doErase = true;
		String jdbcUrl = null;
		String jdbcDriver = null;
		
		Cmdline.parse(args);
		args = Cmdline.getStrayArgs();
		
		if (Cmdline.getArg("add") != null && Cmdline.getArg("add").equals("1")) {
			doErase = false;
		}
		String beaconType = Cmdline.getArg("beacontype");
		
		if (args == null || args.length < 1) {
			//	System.out.println("Usage: MapLoader <url or file>");
			//			return;
			System.out
					.println("Loading map from default location: http://www.placelab.org/data/do-retrieve.php?style=new");
			dataLoc = "http://www.placelab.org/data/do-retrieve.php?style=new";
		} else {
			dataLoc = args[0];
			System.out.println("Loading new map from: '" + dataLoc + "'");
		}
		if (!doErase) {
			System.out.println("Appending new Beacon data...");
		}
		if (beaconType != null) {
			System.out.println("Creating map with beacons of type "+beaconType);
		} else {
			System.out.println("Creating map with beacons of default type");
		}

		try {
			//MapLoader ml = new MapLoader();
		    MapSourceLoader ml	= new MapSourceLoader(CompoundMapper.createDefaultMapper(true,false), doErase);
		    String wiglename, wiglepass;
		    if((wiglename = Cmdline.getArg("wiglename")) != null && 
		           (wiglepass = Cmdline.getArg("wiglepass")) != null) {
		        ml.addSource(new WigleMapSource(wiglename, wiglepass));
		    } else if(Cmdline.getArg("usewigle") != null && Cmdline.getArg("usewigle").equalsIgnoreCase("true")){
		        ml.addSource(new WigleMapSource());
		    } else if (dataLoc.startsWith("http://")) { // Sure this is a kludge, sue me
				ml.addSource(new URLMapSource(dataLoc, dataLoc));
			} else {
				ml.addSource(new FileMapSource(dataLoc));
			}
		    
			if (doErase) {
				System.out.println("Emptying old map and creating new one...");
			}
			System.out.println("Loading new map...");
			new Thread(ml).start();
			boolean debug = true;
			String debugProperty = System
					.getProperty("placelab.MapLoader.nodebug");
			if (debugProperty != null
					&& (debugProperty.equals("1") || debugProperty
							.equalsIgnoreCase("true")))
				debug = false;
			
			while(!ml.isDone()) {
			    if(debug) {
				    if(ml.getError() != null) {
				        System.out.println("Error: " + ml.getError());
				    }
				    System.out.println("Status: " + ml.getCurrentStatus() + " Count: " + ml.getBeaconCount());
				    Thread.sleep(5000);
			    }
			}
			System.out.println("New map contains " + ml.getBeaconCount()
					+ " records");
		} catch (Exception ex) {
			System.out.println("Error loading map");
			ex.printStackTrace();
		}

	}

	/**
	 * Deletes all the known Beacons and returns how many elements were in the
	 * map.
	 */
	public void createNewMap() {
		mapper.deleteAll();
		if (!mapper.isOpened()) mapper.open();
	}
	
//	private Beacon createBeacon(String storageString) {
//		String[] sarr = StringUtil.split(storageString);
//		if (sarr.length == 4) {
//			/* this is the old format of the mapper db */
//			return new WiFiBeacon(sarr);
//		}
//		
//		/* assume we have key/value pairs
//		 */
//		HashMap map = Beacon.storageStringToHashMap(storageString);
//		if (map==null) return null;
//		String type = (String)map.get(BEACON_TYPE);
//		/* this assumes that the full class name is embedded in the type field */
//		Class klass = null;
//		try {
//			klass = (type != null ? Class.forName(type) : DEFAULT_BEACON_CLASS);
//		} catch (ClassNotFoundException e) {
//			klass = DEFAULT_BEACON_CLASS;
//		}
//		return Beacon.create(klass, map);
//	}

	/**
	 * Load up a new map from the given inputstream. Returns the number of new
	 * Beacons loaded
	 */
	public int loadMap(InputStream is) throws IOException {
		// This method does the bulk of the work in this class
		// nuke any old records in the persistent hash
		mapper.startBulkPuts();
		int k = 0, numAdded = 0;

		boolean debug = true;
		String debugProperty = System
				.getProperty("placelab.MapLoader.nodebug");
		if (debugProperty != null
				&& (debugProperty.equals("1") || debugProperty
						.equalsIgnoreCase("true")))
			debug = false;

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while (true) {
			String str = br.readLine();
			if (str == null) {
				break;
			}

			k++;
			Beacon b = mapper.createBeacon(str);
			if (b == null) {
				System.out.println("Error when loading table");
				System.out.println("The offending line was:" + str);
				continue;
			}
			if (debug && (k % 1000) == 0) {
				System.out.println(k + " " + b.getId());
			}
			
			/* you may end up with multiple beacons of the same id */
			LinkedList l = mapper.findBeacons(b.getId());
			if (l == null) {
				mapper.putBeacon(b.getId(), b);
			} else {
				l.add(b);
				mapper.putBeacons(b.getId(), l);
			}
			numAdded++;
		}
		mapper.endBulkPuts();
		return numAdded;
	}

}