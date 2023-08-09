package org.placelab.mapper;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.placelab.core.PlacelabProperties;
import org.placelab.core.Types;

/**
 * A Mapper that uses Hsql to store Beacons.
 * The default path for the Hsql datastore is 
 * in placelab.datadir/hsqlmap
 * 
 */
public class HsqlMapper extends JDBCMapper  {
	
	public static String DEFAULT_NAME = "hsqlmap";
	
	public HsqlMapper(String path, boolean shouldCache) throws ClassNotFoundException, SQLException {
		super("jdbc:hsqldb:" + path,"org.hsqldb.jdbcDriver",shouldCache);
	}
	
	public HsqlMapper(boolean shouldCache) throws ClassNotFoundException, SQLException {
		this(PlacelabProperties.get("placelab.datadir")+ "/" + DEFAULT_NAME,shouldCache);
	}

	public HsqlMapper() throws ClassNotFoundException, SQLException {
		this(true);
	}
	
	
	// override open
	public boolean open(boolean createTable) throws SQLException {
		if (isOpened())
			return true;
		connection = DriverManager.getConnection(url,"sa","");
		Statement s = null;
		try {
			s = connection.createStatement();
			s.execute("SET AUTOCOMMIT TRUE");
			if (createTable) {
				s.execute("CREATE CACHED TABLE " + TABLE_NAME
						+ "(" + Types.ID + " VARCHAR(32) NOT NULL, "
						+ Types.TYPE + " VARCHAR(16) NOT NULL, "
						+ Types.LATITUDE + " DOUBLE, "
						+ Types.LONGITUDE + " DOUBLE," 
						+ "storage VARCHAR(255) NOT NULL)");
				s.executeUpdate("CREATE INDEX index1 on " + TABLE_NAME + 
						"(" + Types.ID + ")");
				s.executeUpdate("CREATE INDEX index2 on " + TABLE_NAME + 
						"(" + Types.LATITUDE + "," + Types.LONGITUDE + ")");
				s.close();
				s = null;
				connection.commit();
			}
		} catch (SQLException e) {
			// this can fail quietly, the table likely already exists
			try {
				if (s != null) s.close();
			} catch (Exception ex) {;}
//			e.printStackTrace();
			return true;
		}
		return true;
	}

	public void endBulkPuts() {
		Statement s = null;
		super.endBulkPuts();
		try {
			if (connection != null) {
				// trying the compaction
				s = connection.createStatement();
				//System.out.println("Checkpointing database...(may be slow)");
				s.execute("CHECKPOINT");
				//System.out.println("Compacting database... (may be really slow)");
				s.execute("SHUTDOWN COMPACT");
				s.close();
				s = null;
				connection.close();
				connection = null;
				//System.out.println("Reopening");
				open();
				//System.out.println("done");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (SQLException e1) { ; }
			}
		}
	}
	
	
}

