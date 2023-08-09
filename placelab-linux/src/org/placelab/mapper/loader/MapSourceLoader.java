/*
 * Created on Aug 24, 2004
 *
 */
package org.placelab.mapper.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.collections.List;
import org.placelab.core.TwoDCoordinate;
import org.placelab.mapper.Beacon;
import org.placelab.mapper.MapLoader;
import org.placelab.mapper.MapUtils;
import org.placelab.mapper.Mapper;
import org.placelab.util.StringUtil;


/**
 * 
 */
public class MapSourceLoader extends MapLoader implements Runnable {
	protected LinkedList sources;
	protected boolean deleteFirst;
	protected TwoDCoordinate coord1, coord2;
	
	protected Hashtable loadedAPs;
	protected boolean lastSource;
 	
	protected boolean done, die;
	protected StringBuffer currentStatus;
	protected String currentError;
	protected int beaconsLoaded;
	
	public final static String SOURCES_URL = "http://www.placelab.org/data/sources.txt";
	
	public MapSourceLoader(Mapper m, boolean delete) {
		super(m);
		sources = new LinkedList();
		deleteFirst = delete;
		loadedAPs = new Hashtable();
		done = die = false;
		currentStatus = new StringBuffer();
		currentError = null;
	}
	
	public void addSource(MapSource source) {
		sources.add(source);
	}
	
	public void setArea(TwoDCoordinate one, TwoDCoordinate two) {
		coord1 = one;
		coord2 = two;
	}
	
	protected void setStatus(String s) {
		synchronized(currentStatus) {
			currentStatus.setLength(0);
			currentStatus.append(s);
		}
	}
	
	public boolean isDone() {
		return done;
	}
	
	public void die() {
		die = true;
	}
	
	public String getCurrentStatus() {
		synchronized(currentStatus) {
			return currentStatus.toString();
		}
	}
	
	public String getError() {
		return currentError;
	}
	
	public int getBeaconCount() {
		return beaconsLoaded;
	}
	
	public void run() {
		setStatus("Opening map...");
		
		if (!mapper.isOpened() && !mapper.open()) {
			setStatus("Map failed to open.");
			done = true;
			return;
		}
		
		if (deleteFirst) {
			setStatus("Clearing map...");
			mapper.deleteAll();
		}
		
		mapper.startBulkPuts();
		
		Iterator iter = sources.iterator();
		while (iter.hasNext()) {
			MapSource source = (MapSource)iter.next();
			setStatus("Loading from " + source.getName());
			lastSource = !iter.hasNext();
			try {
				loadMap(source);
			} catch (MapSourceException e) {
				currentError = e.getMessage();
				setStatus("Loading error.");
				done = true;
				return;
			}
		}
		
		setStatus("Closing map...");
		mapper.endBulkPuts();
		mapper.close();
		
		setStatus("Done.");
		done = true;
	}
	
	public void loadMap(MapSource source) throws MapSourceException {
		Iterator iter = source.query(coord1, coord2);
		
		if (iter == null) {
			System.err.println(source.getName() + " returned null iterator");
			return;
		}
		
		while (iter.hasNext()) {
			String beacon = (String)iter.next();
			loadBeacon(source, beacon);
			if (die)
				break;
		}
	}
	
	public void loadBeacon(MapSource source, String s) {
		Beacon beacon = mapper.createBeacon(s);
		
		if (beacon == null || beacon.getId() == null) {
			System.err.println("Bad line: '" + s + "'");
			return;
		}
		
		MapSource previous = (MapSource)loadedAPs.get(beacon.getId());
		
		if (previous == null) {
			//never seen this ID in the mapper before
			mapper.putBeacon(beacon.getId(), beacon);
			if (!lastSource) loadedAPs.put(beacon.getId(), source);
			beaconsLoaded++;
		} else if (previous != null && previous == source) {
			// seen this ID and with this source so we add it
			if (mapper.overrideOnPut()) {
				LinkedList l = mapper.findBeacons(beacon.getId());
				l.add(beacon);
				mapper.putBeacons(beacon.getId(), l);
			} else {
				mapper.putBeacon(beacon.getId(), beacon);
			}
			beaconsLoaded++;
		}
		// else System.out.println("Skipping duplicate");
	}

	public static List getDefaultSources() {
		InputStream stream = MapUtils.getHttpStream(SOURCES_URL);
		if(stream == null) {
		    System.err.println("Cannot open URL: " + SOURCES_URL);
		    return null;
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		
		List sources = new LinkedList();
		try {
			for (;;) {
				String line = reader.readLine();
				if (line == null)
					break;
				
				String[] fields = StringUtil.split(line);
				if (fields.length < 2)
					continue;
				else if ("placelab".equals(fields[1]))
					sources.add(new PlacelabMapSource());
				else if ("wigle".equals(fields[1]))
					sources.add(new WigleMapSource());
				else if ("url".equals(fields[1]) && fields.length >= 3)
					sources.add(new URLMapSource(fields[0], fields[2]));
			}
		} catch (IOException e) {}
		
		return sources;
	}
}
