package org.placelab.mapper;

import org.placelab.collections.HashMap;
import org.placelab.collections.HashSet;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.collections.Set;
import org.placelab.collections.UnsupportedOperationException;
import org.placelab.core.Coordinate;
import org.placelab.core.PlacelabProperties;

/**
 * A Mapper that draws upon multiple mappers to find Beacons.
 * The precedence for which Beacon is chosen when multiple
 * Mappers know of the Beacon is determined by the order in 
 * which the sub-Mappers were added.
 */
public class CompoundMapper extends SimpleMapper {
	public LinkedList mappers;
	
	public CompoundMapper() {
		mappers = new LinkedList();
	}
	
	/**
	 * Adds a Mapper at lower precedence than all other Mappers
	 * in the CompoundMapper.
	 * @param m
	 */
	public void addMapper(Mapper m) {
		mappers.add(m);
	}
	/**
	 * Adds a Mapper and gives it the highest precedence
	 * for Beacons where multiple Mappers find the same Beacon.
	 */
	public void addMapperAtHead(Mapper m) {
		mappers.addFirst(m);
	}
	
	public LinkedList findBeacons(String id) {
		for (Iterator it = mappers.iterator(); it.hasNext(); ) {
			Mapper m = (Mapper)it.next();
			LinkedList list = m.findBeacons(id);
			if ((list != null) && (list.size() > 0)) {
				return list;
			}
		}
		return null;
	}
	private class CMIterator implements Iterator {
		Set iters;
		Iterator iteriter;
		Iterator beaconiter=null;
		Beacon next = null;
		HashMap seen = new HashMap();
		public CMIterator(Set iters) {
			this.iters = iters;
			iteriter = iters.iterator();
			loadNext();
		}
		public void loadNext() {
			while (true) { 
				if ((beaconiter != null) && (beaconiter.hasNext())) {
					next = (Beacon)beaconiter.next();
					if (next == null) {
						return;
					}
					if (seen.get(next.getId().toLowerCase()) == null) {
						seen.put(next.getId().toLowerCase(),"yup");
						return;
					} else {
						next = null;
					}
				} else {
					beaconiter = null;
					if (iteriter.hasNext()) {
						// grab another iterator
						beaconiter = (Iterator)iteriter.next();
					} else {
						return; //its all done
					}
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

		public void remove() {
			throw new UnsupportedOperationException("remove() not supported for JDBMMapper iterators");
		}		
	}

	public Iterator query(Coordinate c1, Coordinate c2) {
		HashSet hs = new HashSet();
		for (Iterator it = mappers.iterator(); it.hasNext();) {
			Mapper m = (Mapper)it.next();
			hs.add(m.query(c1,c2));
		}
		return new CMIterator(hs);
	}
	
	/**
	 * This method returns a Mapper (not necessarily a CompoundMapper) according to the
	 * default set in {@link org.placelab.core.PlacelabProperties}.  Set the property
	 * placelab.mapper to JDBM for a JDBMMapper or Hsql for an HsqlMapper.  In either
	 * case, the Mappers will load from the default Mapper location for their type, 
	 * also specified in PlacelabProperties.  See the documentation for the individual
	 * Mapper types for the name of that property.
	 * @see JDBMMapper
	 * @see HsqlMapper
	 */
	public static Mapper createDefaultMapper(boolean exitOnError, boolean shouldCache) {
		Mapper rv = null;
		
		String mapperToUse = PlacelabProperties.get("placelab.mapper");
		
		try {
			if ("JDBM".equalsIgnoreCase(mapperToUse)) {
				System.out.println("Making a JDBMMapper");
				rv = new JDBMMapper(shouldCache);
			} else if ("Hsql".equalsIgnoreCase(mapperToUse)) {
				System.out.println("Making an HsqlMapper");
				rv = new HsqlMapper(shouldCache);
			} else if ("wigle".equalsIgnoreCase(mapperToUse)) {
				System.out.println("Making a WigleMapper");
				rv = new WigleMapper();
			} else {
				System.out.println("Making an HSQLMapper by default");
				rv = new HsqlMapper(shouldCache);
			}
 
		} catch (Exception ex) {
			ex.printStackTrace();
			if (exitOnError) {
				System.exit(1);
			}
		}

	return rv;
		/*
		CompoundMapper rv = new CompoundMapper();
		// first lets add a JDBM mapper
		try {
			rv.addMapper(new JDBMMapper(shouldCache));
		} catch (IOException ex) {
			ex.printStackTrace();
			System.out.println("Failed to create JDBMMapper"); 
			if (exitOnError) {
				System.exit(1);
			}
		}
		
		// add a jdbc mapper if one has been specified
		String jdbcdriver = System.getProperty("placelab.jdbcmapper.driver");
		String jdbcurl = System.getProperty("placelab.jdbcmapper.url");
		if ((jdbcdriver != null) && (jdbcurl != null)) {
			try {
				rv.addMapper(new JDBCMapper(jdbcurl,jdbcdriver,shouldCache));
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("Failed to create JDBCMapper with\n" + 
						"placelab.jdbcmapper.driver = " + jdbcdriver + "\n" +
						"placelab.jdbcmapper.url = " + jdbcurl + "\n");
				if (exitOnError) {
					System.exit(1);
				}
			}
		}
		if (rv.mappers.size() == 1) {
			return (Mapper)rv.mappers.get(0);
		} else {
			return rv;
		}
		*/
	}

	/* (non-Javadoc)
	 * @see org.placelab.mapper.Mapper#overrideOnPut()
	 */
	public boolean overrideOnPut() {
		return false;
	}

}