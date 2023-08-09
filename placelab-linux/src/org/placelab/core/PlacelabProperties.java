package org.placelab.core;

/**
 * 
 *
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;


/**
 * A helper class that holds the preferences for this Place Lab instantiation.
 */
public class PlacelabProperties {
    public static final String JDBM_DB_PROPERTY = "placelab.mapperdb";
	public static final String JDBC_DB_PROPERTY = "placelab.jdbc_url";

    private static boolean loaded = false;

	/**
	 *  Invoke this method to explicitly load the defaults from the
	   placelab.ini file.  You should not usually need to call 
	   this function. 
	  */
	private synchronized static void load() {
		if (loaded) return;
		loaded = true;

		/* first check if there is a system property called
		 * placelab.ini which points to the .ini file */
		String filename = System.getProperty("placelab.ini");
		if (filename == null) {
			/* load the default filename: $HOME/placelab.ini */
			String home = System.getProperty("user.home");
			if (home == null) home = "";
			filename = home + File.separator + "placelab.ini";
		}

		Properties iniProp = new Properties();
		if (filename != "") {
			try {
				InputStream is = new FileInputStream(filename);
				iniProp.load(is);
				is.close();
			} catch ( IOException _ex ) {
				// silently ignore any file-open errors
			}
			
			for (Enumeration e=iniProp.keys(); 
			     e.hasMoreElements() ; ) {
				String key = (String) e.nextElement();
				setDefault(key, iniProp.getProperty(key));
			}
		}

		loadDefaults();
	}

	private static void loadDefaults() {
		String home = System.getProperty("user.home");
		if (home == null) home = "";
		setDefault("placelab.dir", home + File.separator + "placelabdata");
		new File(get("placelab.dir")).mkdirs();
		setDefault("placelab.logdir", 
			   get("placelab.dir") + File.separator + "logs");
		new File(get("placelab.logdir")).mkdirs();
		setDefault("placelab.imagedir", 
			   get("placelab.dir") + File.separator + "images");
		new File(get("placelab.imagedir")).mkdirs();
		setDefault("placelab.datadir", 
			   get("placelab.dir") + File.separator + "data");
		new File(get("placelab.datadir")).mkdirs();
		setDefault("placelab.tmpdir", 
			   get("placelab.dir") + File.separator + "tmp");
		new File(get("placelab.tmpdir")).mkdirs();
		setDefault("placelab.testdir", 
			   get("placelab.dir") + File.separator + "regression");
		new File(get("placelab.testdir")).mkdirs();
		setDefault(JDBM_DB_PROPERTY,
			   get("placelab.datadir") + File.separator + 
			   "mapper");
		setDefault(JDBC_DB_PROPERTY, "jdbc:cloudscape:" +
				   get("placelab.datadir").replace('\\', '/') + 
				   "/mapper;create=true");
		setDefault("placelab.mapwaddir",
			get("placelab.dir") + File.separator + "mapwads");
		new File(get("placelab.mapwaddir")).mkdirs();
		setDefault("placelab.standalone_spotter",
			   get("placelab.datadir") + File.separator + 
			   "spotter");
		setDefault("placelab.mapperpack_url",
				"http://www.placelab.org/data/mapper-zip.php");
		setDefault("placelab.mapperpack_md5_url",
		"http://www.placelab.org/data/mapper-md5.php");
		if(!get("os.name").equalsIgnoreCase("Windows CE")) {
			setDefault("placelab.mapper", "HSQL");
		} else {
			setDefault("placelab.mapper", "JDBM");
		}
	}

	public synchronized static void setDefault(String key, String value) {
		/* make sure you load the ini file before setting 
		   any defaults */
		if (!loaded) load();

		if (System.getProperty(key) == null) {
			System.setProperty(key, value);
		}
	}
	public static void set(String key, String value) {
		System.setProperty(key, value);
	}
	public synchronized static String get(String key) {
		if (!loaded) load();

		String val = System.getProperty(key);
		return ((val == null) ? "" : val);
	}
	
	/** 
	 * write the current placelab defaults set to the placelab.ini 
	 * only items beginning with placelab will be written.
	 */
	public synchronized static void synchronize() {
		/* first check if there is a system property called
		 * placelab.ini which points to the .ini file */
		String filename = System.getProperty("placelab.ini");
		if (filename == null) {
			/* load the default filename: $HOME/placelab.ini */
			String home = System.getProperty("user.home");
			if (home == null) home = "";
			filename = home + File.separator + "placelab.ini";
		}
		Hashtable allProps = System.getProperties();
		Properties myProps = new Properties();
		Enumeration e = allProps.keys();
		while(e.hasMoreElements()) {
		    String key = (String)e.nextElement();
		    if(key.toLowerCase().startsWith("placelab")) {
		        myProps.put(key, allProps.get(key));
		    }
		}
		try {
            myProps.store(new FileOutputStream(filename),
                    "PlacelabProperties");
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch(IOException e2) {
            e2.printStackTrace();
        }
	}

}
