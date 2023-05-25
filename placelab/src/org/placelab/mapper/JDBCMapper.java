/*
 * Created on Aug 9, 2004
 *
 */
package org.placelab.mapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.core.Coordinate;
import org.placelab.core.PlacelabProperties;
import org.placelab.core.TwoDCoordinate;
import org.placelab.core.Types;
import org.placelab.util.StringUtil;

/**
 * A Mapper that uses a JDBC database to store Beacons.
 * The default db location is specified by the system property
 * "placelab.jdbc_url".
 * 
 */
public class JDBCMapper extends AbstractMapper {
	protected String url;
	protected Connection connection;
	protected boolean bulkPuts;
	protected int putCount;
	
	public final static String TABLE_NAME = "placelabbeacons";
		
	/**
	 * Create a new JDBCMapper for the given database.  It will create
	 * a table to store the Beacons in if it doesn't already exist, and
	 * will open a connection to the database at that time.
	 * @param url the jdbc:url url for the database, including username and password
	 * @param driver the jdbc driver class to use (see your database docs)
	 * @param shouldCache whether or not to cache access Beacons in memory
	 * @throws ClassNotFoundException if your jdbc driver couldn't be found 
	 * @throws SQLException if the database was angry about something
	 */
	public JDBCMapper(String url, String driver, boolean shouldCache) throws ClassNotFoundException, SQLException  {
		super(shouldCache);
		try {
//			System.out.println("Driver is " + driver);
//			System.out.println("URL is " + url);
//			System.out.println("ITS" + PlacelabProperties.get("hsqldb.cache_scale"));
			Class.forName(driver).newInstance();
		} catch (Exception e) {
			throw new ClassNotFoundException(e.getMessage());
		}
		this.url = url == null ? PlacelabProperties.get(PlacelabProperties.JDBC_DB_PROPERTY) : url;
		open(true);
	}
	
	protected LinkedList findBeaconsImpl(String id) {
		try {
			Statement s = connection.createStatement();//);
//			ResultSet.TYPE_SCROLL_INSENSITIVE,
//					ResultSet.CONCUR_READ_ONLY);
			String q = "SELECT storage FROM " + TABLE_NAME + 
					" WHERE " + Types.ID + "='" + id + "'";
			ResultSet rs = s.executeQuery(q);
			
			LinkedList list = new LinkedList();
			ResultSetMetaData meta = rs.getMetaData();
			while (rs.next()) {
				Beacon b = createBeacon(rs.getString(1));
				list.add(b);
			}
			rs.close();
			s.close();
			return list;
		} catch (SQLException e) {
//			System.err.println("findBeaconsImpl failed: " + e.getMessage());
			e.printStackTrace();
//			System.exit(1);
			return null;
		}
	}

	protected boolean putBeaconsImpl(String id, LinkedList beacons) {
		Iterator iter = beacons.iterator();
		while (iter.hasNext()) {
			Beacon b = (Beacon)(iter.next());
			HashMap map = b.toHashMap();
			String type = (String)map.get(Types.TYPE);
			String lat = (String)map.get(Types.LATITUDE);
			String lon = (String)map.get(Types.LONGITUDE);
			String storage = StringUtil.hashMapToStorageString(map);
			storage = storage.replace('\'','_');
			executePutBeacon(id, type, lat, lon,storage);
		}
		return true;
	}
	
	protected boolean executePutBeacon(String id, String type, String lat, String lon, String storageStr) {
		try {
			if (type == null) {
				type = "WIFI"; // slap Anthony
			}
			Statement st = connection.createStatement();
			String q = "INSERT INTO " + TABLE_NAME + " " +
			" VALUES ('" + id + "','" + type + "', " + lat + "," + lon + ", '" + storageStr + "')";
			st.execute(q);
			if (!bulkPuts || ++putCount % 1000 == 0) {
//				System.out.println("Available Mem: " + Runtime.getRuntime().freeMemory());
//				st.execute("CHECKPOINT");
			}
			st.close();
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}

	public boolean open(boolean createTable) throws SQLException {
		if (isOpened())
			return true;
		System.out.println("URL is " + url);
		connection = DriverManager.getConnection(url);
		try {
			if (createTable) {
				Statement s = connection.createStatement();
				connection.setAutoCommit(true);
				System.out.println(s.executeUpdate("CREATE  TABLE " + TABLE_NAME
						+ "(" + Types.ID + " VARCHAR(32) NOT NULL, "
						+ Types.TYPE + " VARCHAR(16) NOT NULL, "
						+ Types.LATITUDE + " DOUBLE DEFAULT 0.0, "
						+ Types.LONGITUDE + " DOUBLE DEFAULT 0.0," 
						+ "storage VARCHAR(255) NOT NULL)"));
				System.out.println(s.executeUpdate("CREATE INDEX index1 on " + TABLE_NAME + 
						"(" + Types.ID + ")"));
				System.out.println(s.executeUpdate("CREATE INDEX index2 on " + TABLE_NAME + 
						"(" + Types.LATITUDE + "," + Types.LONGITUDE + ")"));
				s.close();
				connection.commit();
			}
		} catch (SQLException e) {
			// this can fail quietly, the table likely already exists
			return true;
		}
		return true;
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
		try {
			final Statement s = connection.createStatement();
			String q = "select storage from " + TABLE_NAME + " where "
					+ Types.LATITUDE + " < " + lat2 + " AND " + Types.LATITUDE
					+ " > " + lat1 + " AND " + Types.LONGITUDE + " < " + lon2
					+ " AND " + Types.LONGITUDE + " > " + lon1 + " order by " + Types.ID;
			System.out.println("Executing query");
			final ResultSet rs = s.executeQuery(q);
			final ResultSetMetaData meta = rs.getMetaData();
			Iterator it = new Iterator() {
				boolean closed = false;
				Beacon nextBeacon = loadNext();
				
				private void close() throws SQLException {
					if (!closed) {
						closed = true;
						rs.close();
						s.close();
					}
				}
				
				public boolean hasNext() {
					return nextBeacon != null;
				}
				
				public Object next() {
					Beacon rv = nextBeacon;
					nextBeacon = loadNext();
					return rv;
				}

				public Beacon loadNext() {
					try {
						if (rs.next()) {
							return createBeacon(rs.getString(1));
						} else {
							close();
							return null;
						}
					} catch (SQLException ex) {
						ex.printStackTrace();
						return null;
					}
				}

				public void remove() {
					throw new UnsupportedOperationException("remove() not supported for iterators");
				}
			};
			
//			System.out.println("IT HAS " + it.hasNext());
			return it;
		} catch (SQLException e) {
			System.err.println("failed " + e.getMessage());
			return new HashMap().keySet().iterator(); // empty
		}
	}
	
	public boolean open() {
		try {
			return open(true);
		} catch (SQLException e) {
			return false;
		}
	}

	public boolean close() {
		try {
			if (connection != null) connection.close();
		} catch (SQLException e) {}
		
		connection = null;
		
		return true;
	}

	public boolean deleteAll() {
		boolean wasOpen = isOpened();
		try {
			if (!wasOpen && !open(false))
				return false;
			Statement s = connection.createStatement();
			s.execute("DROP TABLE " + TABLE_NAME);
			s.close();
			connection.commit();
			close();
			return !wasOpen || open(true);
		} catch (SQLException e) {
			return false;
		}
	}

	public boolean isOpened() {
		return connection != null;
	}

	public void startBulkPuts() {
		bulkPuts = true;
	}

	public void endBulkPuts() {
		bulkPuts = false;
		try {
			if (connection != null) {
				connection.commit();
			}
		} catch (SQLException e) {}
	}

	public boolean overrideOnPut() { return false; };
	
}
