package org.placelab.mapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.placelab.core.PlacelabProperties;
import org.placelab.util.Cmdline;
import org.placelab.util.FileSynchronizer;
import org.placelab.util.ZipUtil;

/**
 * This MapLoader checks the website for an up to date
 * mapper.db and if a newer one exists, it downloads it
 * and replaces the current mapper.db with it.
 * 
 */
public class JDBMQuickMapLoader extends MapLoader {
	private static String pathKey = "placelab.mapperpack_url";
	private static String md5Key = "placelab.mapperpack_md5_url";
	
	public String url;
	public String md5url;
	
	public JDBMQuickMapLoader() throws IOException {
		this(PlacelabProperties.get(pathKey),
			PlacelabProperties.get(md5Key),
			PlacelabProperties.get(PlacelabProperties.JDBM_DB_PROPERTY));
	}
	
	public JDBMQuickMapLoader(String url, String md5url, String mapperLoc) throws IOException {
		super(new JDBMMapper(mapperLoc));
		this.url = url;
		this.md5url = md5url;
	}
	
	/**
	 * Check the website for whether or not our mapper.db is the latest one
	 * If it is, return true and do nothing
	 * If it isn't, replace it with the current one and return false
	 */
	public boolean doIt() throws IOException {
		if(md5matches()) return true;
		// otherwise do replacement
		JDBMMapper jmap = (JDBMMapper) getMapper();
		String name = jmap.getDbName();
		jmap.deleteAll();
		jmap.close();
		File temp = File.createTempFile("mapper", "zip");
		suckFile(url, temp.getAbsolutePath());
		// now extract the mapper.db and mapper.lg
		ZipUtil.extractFile(temp, "mapper.db", new File(name + ".db"));
		ZipUtil.extractFile(temp, "mapper.lg", new File(name + ".lg"));
		// note that I don't depend on this when checking for updates
		// since the regular JDBMMapLoader might have been run which
		// doesn't generate these, and the mapper.db may be up to date
		ZipUtil.extractFile(temp, "mapper.md5", new File(name + ".md5"));
		temp.delete();
		jmap.open();
		return false;
	}
	
	public static void suckFile(String fromURL, String toLocation)
		throws IOException 
	{
		InputStream in = MapUtils.getHttpStream(fromURL);
		if(in == null) throw new IOException("Cannot open URL: " + fromURL);
		FileOutputStream out = new FileOutputStream(toLocation);
		ZipUtil.pipeStreams(out, in);
		out.flush();
		out.close();
		in.close();
	}
	
	public boolean md5matches() throws IOException {
	    InputStream is;
	    is=MapUtils.getHttpStream(md5url);
	    if(is == null) {
	        throw new IOException("Cannot open URL: " + md5url);
	    } 
		BufferedReader web = new BufferedReader(new InputStreamReader(is));
		StringBuffer webResult = new StringBuffer();
		String line;
		while((line = web.readLine()) != null) {
			webResult.append(line);
		}
		String webMD5 = webResult.toString().trim();
		String localMD5 = null;
		try {
			localMD5 = FileSynchronizer.loadFileHash(((JDBMMapper)getMapper()).getDbName() + ".db");
		} catch (Exception e) {
			// rather not have just plain Exception
			throw new IOException(e.getMessage());
		}
		if(localMD5 == null) return false;
		return localMD5.equalsIgnoreCase(webMD5);
	}
	
	
	public static void main(String[] args) {
		try {
			Cmdline.parse(args);
			JDBMQuickMapLoader loader;
			if(Cmdline.getArg("help") != null) {
				System.out.println("Usage: java JDBMQuickMapLoader " +
						"[--count] [--url=<url>] [--md5url=<md5url>] " +
						"[--mapperdb=<mapperdb data directory>]");
				System.exit(0);
			}
			String mapperDB = PlacelabProperties.get(PlacelabProperties.JDBM_DB_PROPERTY);
			String url = PlacelabProperties.get(pathKey);
			String md5url = PlacelabProperties.get(md5Key);
			if(Cmdline.getArg("mapperdb") != null) {
				mapperDB = Cmdline.getArg("mapperdb");
			}
			if(Cmdline.getArg("url") != null) {
				url = Cmdline.getArg("url");
			}
			if(Cmdline.getArg("md5url") != null) {
				md5url = Cmdline.getArg("md5url");
			}
			loader = new JDBMQuickMapLoader(url, md5url, mapperDB);
			if(Cmdline.getArg("count") != null)
				System.out.println("Old db size is " + ((JDBMMapper)loader.getMapper()).size());
			System.out.println("Loading new map from " + loader.url);
			if(loader.doIt()) {
				System.out.println("Your mapper.db is up to date");
			} else {
				if(Cmdline.getArg("count") != null)
					System.out.println("New db size is " + ((JDBMMapper)loader.getMapper()).size());
				else
					System.out.println("Update complete");
			}
		} catch (Exception e) {
			System.err.println("Exception caught");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
