package org.placelab.demo.mapview;

//import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.swt.graphics.ImageData;
import org.placelab.core.PlacelabProperties;
import org.placelab.mapper.JDBMMapper;
import org.placelab.mapper.MapLoader;
import org.placelab.util.StringUtil;
import org.placelab.util.ZipUtil;

/**
 * This class reads a map wad and gets out all the maps and allows
 * accessing of all the maps and places in it.  Alternatively, it can
 * write a map wad.
 * 
 * <pre>
 * Hashtable Wads are zip files with the following structure:
 * + map.wad
 * 		- apcache.txt (a text file of the aps like from placelab.org)
 * 		- defaults.txt (a SectionedFile described below)
 * 		+ maps
 * 			- maps.index (a SectionedFile described below)
 * 			- mapname.jpg/gif/png OR mapname.tiger.zip (us census tiger format data)
 * 			- mapname.meta (a SectionedFile described below)
 * 		+ places
 * 			- places.index (a SectionedFile described below)
 * 			- place-set-name.txt (a SectionedFile described below)
 * 			- icons
 * 				- beer-icon.jpg/gif/png
 * 
 * maps.index has the following structure
 * [Maps]
 * mapname=mapname.meta
 * 
 * 
 * mapname.meta has the following structure
 * [Map]
 * # origin_lat and origin_lon are lower left corner values
 * origin_lat=47.349829
 * origin_lon=-122.98327
 * pixels_per_lat=100.01
 * pixels_per_lon=9994.39828
 * # OR you may specify the upper right corner's lat and lon
 * upper_right_lat=47.449829
 * upper_right_lon=-122.99327
 * 
 * image=images/mapimage1.jpg/gif/png
 * 
 * NEW: alternatively you can specify the following structure
 * for us census tiger data embedded in a mapwad:
 * [TigerMap]
 * data=maps/mapname.tiger.txt
 * 
 * The Tiger data is the type 1 and type 2 record files
 * concatenated together in that order.  This is likely
 * to change in the future.
 * 
 * 
 * places.index has the following structure
 * [Places]
 * place-set-name=place-set-name.txt
 * 
 * 
 * place-set-name.txt has the following structure
 * [place name]
 * # image references are relative to the root of the wad
 * icon_file=places/icons/beer_icon.jpg
 * lat=46.19829
 * lon=-122.8132
 * text=Some descriptive text
 * url=http://www.whitehouse.gov
 * type=radical/AP/etc
 * 
 * [other place name]
 * etc ...
 * 
 * defaults.txt has the following structure
 * [Map]
 * mapname=1
 * 
 * [Places]
 * placeset1=1
 * placeset2=1
 * placeset3=1
 * # ... and as many place sets as you would like to specify
 * </pre>
 * 
 * the defaults file specifies what the wad author thinks should
 * be the first set of things that people might want to see.  There
 * is no guarantee that they will be the first things displayed, it
 * is only a suggestion.  Also note that while you must specify
 * a map if you have a defaults file, you needn't specify any place
 * sets.  Defaults files are not required.
 * 
 * Note that while place set definition file references in places.index
 * are relative references, the image file references actually in the place 
 * set files are relative to the root of the wad.  This is done so that people
 * can put their images wherever they want in the wad, since ../ type
 * references are not allowed.
 * 
 */


public class WadData {
	// this is a Hashtable of maps
	protected Hashtable maps;
	// this is a Hashtable of Hashtables of places
	// where <place set name> => <place set> and a <place set> is
	// a hashtable where <place name> => <PlaceBacking object>
	protected Hashtable places;
	
	// I only want to have at most 1 copy of each image in memory at
	// once
	protected Hashtable imageResources;
	
	protected String defaultMap;
	protected Hashtable defaultPlaces;
	
	private static final String mapsDirKey = "maps/";
	private static final String placesDirKey = "places/";
	private static final String mapsIndexKey = "maps/maps.index";
	private static final String placesIndexKey = "places/places.index";
	private static final String apCacheKey = "apcache.txt";
	private static final String defaultsKey = "defaults.txt";
	private String myApCacheKey = apCacheKey;
	
	protected ZipFile wad;
	
	/* Load all the map data out of a given wad and into memory */
	public WadData(String pathToWad) 
		throws IOException, WadDataFormatException
	{
		this();
		wad = new ZipFile(normalizePath(pathToWad));

		String s = (String)PlacelabProperties.get("placelab.apcache");
		if ((s != null) && (s.length() > 0)) {
			myApCacheKey = s;
		}
			
		this.loadMaps();
		this.loadPlaces();
		this.loadDefaults();
	}
	
	// this does some business so that
	// a) wads in the placelab.mapwaddir are found
	// b) directories structured in the form of a wad
	// can be read
	private String normalizePath(String path)
		throws IOException
	{
		File file = new File(path);
		if(file.exists()) { 
			if(file.isDirectory()) {
				return fromDir(path);
			} else {
				return file.getAbsolutePath();
			}
		} else {
			// try to construct a file relative to the mapwad dir
			if(file.isAbsolute()) {
				throw new FileNotFoundException(path);
			} else {
				String path2 = PlacelabProperties.get("placelab.mapwaddir") +
					File.separator + path;
				file = new File(path2);
				if(file.exists()) {
					if(file.isDirectory()) {
						return fromDir(path2);
					} else {
						return file.getAbsolutePath();
					}
				} else {
					throw new FileNotFoundException("neither " + path2 +
					" nor " + path + "exists");
				}
			}
		}
	}
	
	private String fromDir(String path) 
		throws IOException
	{
		File file = new File(path);
		File tmp = File.createTempFile(file.getName(), ".zip");
		ZipUtil.dirToZip(new File(path), tmp);
		return tmp.getAbsolutePath();
	}

	/* create a new wad to be loaded for writing */
	public WadData() {
		maps = new Hashtable();
		places = new Hashtable();
		imageResources = new Hashtable();
	}
	
	/* methods related to reading map wads */
	
	private void loadDefaults() 
		throws IOException, WadDataFormatException
	{
		ZipEntry defaultsEntry = wad.getEntry(defaultsKey);
		if(defaultsEntry == null || defaultsEntry.isDirectory()) {
			// its ok not to have any defaults
			defaultMap = null;
			defaultPlaces = null;
			return;
		}
		InputStream defStream = wad.getInputStream(defaultsEntry);
		SectionedFileParser defParser = null;
		try {
			defParser = new SectionedFileParser(defStream);
		} catch (SectionedFileFormatException sffe) {
			throw new WadDataFormatException(defaultsKey,
				WadDataFormatException.BAD_RESOURCE_ERROR,
				sffe, "");
		}
		try {
			defaultMap = (String)defParser.getSection("Map").keys().nextElement();
		} catch (NoSuchElementException nsee) {
			throw new WadDataFormatException(defaultsKey,
				WadDataFormatException.BAD_RESOURCE_ERROR,
				nsee, "defaults.txt must specify a default map");
		}
		if(getMap(defaultMap) == null) {
			throw new WadDataFormatException(defaultsKey,
				WadDataFormatException.BAD_RESOURCE_ERROR,
				"defaults.txt specified a map that doesn't exist"
				+ " in the map wad");
		}
		Hashtable defPlaceNames = defParser.getSection("Places");
		if(defPlaceNames != null) {
			defaultPlaces = new Hashtable();
			Enumeration e = defPlaceNames.keys();
			while(e.hasMoreElements()) {
				String placeName = (String)e.nextElement();
				Hashtable placeSet = this.getPlaceSet(placeName);
				if(placeSet == null) {
					throw new WadDataFormatException(defaultsKey,
						WadDataFormatException.BAD_RESOURCE_ERROR,
						"defaults.txt specified a place set that doesn't"
						+ " exist in the map wad");
				}
				defaultPlaces.put(placeName, placeSet);
			}
		} else {
			// there is no requirement to specify default places
			// a default map is sufficient
			defaultPlaces = null;
		}		
	}
	
	private void loadMaps() throws IOException, WadDataFormatException {
		ZipEntry mapIndexEntry = wad.getEntry(mapsIndexKey);
		if(mapIndexEntry == null || mapIndexEntry.isDirectory()) {
			// it is not an error to not have any maps in a wad
			return;
		}
		InputStream mapIndexStream = wad.getInputStream(mapIndexEntry);
		SectionedFileParser indexParser = null;
		try {
			indexParser = new SectionedFileParser(mapIndexStream);
		} catch(SectionedFileFormatException sffe) {
			throw new WadDataFormatException(mapsIndexKey,
					WadDataFormatException.BAD_RESOURCE_ERROR,
					sffe, "");
		}
		Hashtable mapsSection;
		if((mapsSection = indexParser.getSection("Maps")) == null) {
			throw new WadDataFormatException(mapsIndexKey,
					WadDataFormatException.BAD_RESOURCE_ERROR, 
					"No Maps Section");
		}
		Enumeration e = mapsSection.keys();
		while(e.hasMoreElements()) {
			String mapName = (String)e.nextElement();
			String metaPath = mapsDirKey + (String)mapsSection.get(mapName);
			ZipEntry metaEntry = wad.getEntry(metaPath);
			if(metaEntry == null || metaEntry.isDirectory()) {
				throw new WadDataFormatException(mapsIndexKey, metaPath,
						WadDataFormatException.MISSING_RESOURCE_ERROR, metaPath + 
						" doesn't exist, yet it was referenced in " +
						mapsIndexKey);
			}
			MapBacking newMap = loadMapFromEntry(metaEntry, mapName);
			maps.put(mapName, newMap);
		}           
	}
	
	private MapBacking loadMapFromEntry(ZipEntry entry, String name) 
		throws IOException, WadDataFormatException {
		// assume that the entry is good, since that should be checked
		// before i get here
		InputStream in = wad.getInputStream(entry);
		SectionedFileParser parser = null;
		try {
			parser = new SectionedFileParser(in);
		} catch (SectionedFileFormatException sffe) {
			throw new WadDataFormatException(mapsIndexKey,
					entry.getName(),
					WadDataFormatException.BAD_RESOURCE_ERROR,
					sffe, "");
		}
		Hashtable mapHash;
		if((mapHash = parser.getSection("Map")) == null) {
		    if((mapHash = parser.getSection("TigerMap")) == null) {
				throw new WadDataFormatException(mapsIndexKey,
						entry.getName(), 
						WadDataFormatException.BAD_RESOURCE_ERROR,
						"You must have either a Map section or a TigerMap section");
		    } else {
		        // parse as a TigerMap
		        String dataPath = getStringOrFail(mapHash, "data", mapsIndexKey, entry.getName());
				if(!containsResource(dataPath)) {
					throw new WadDataFormatException(entry.getName(),
							dataPath, WadDataFormatException.MISSING_RESOURCE_ERROR, "");
				}
				throw new RuntimeException("Tiger functionality has been temporarily disabled.");
		        //ALM return new TigerMapBacking(name, dataPath, this);
		    }
		}
		String imagePath = getStringOrFail(mapHash, "image", mapsIndexKey,
				entry.getName());
		// test that the image at least exists
		if(!containsResource(imagePath)) {
			throw new WadDataFormatException(entry.getName(),
					imagePath, WadDataFormatException.MISSING_RESOURCE_ERROR, "");
		}
		String imageName = basename(imagePath);
		double originLat = getDoubleOrFail(mapHash, "origin_lat", 
				mapsIndexKey, entry.getName());
		double originLon = getDoubleOrFail(mapHash, "origin_lon",
				mapsIndexKey, entry.getName());
		// the two origins scheme is preferable to the pixels_per_lat/lon
		// scheme, since it is easier on the end user, despite the fact that
		// it is less efficient.
		if(mapHash.containsKey("upper_right_lat")) {
			double upperRightLat = getDoubleOrFail(mapHash, "upper_right_lat",
				mapsIndexKey, entry.getName());
			double upperRightLon = getDoubleOrFail(mapHash, "upper_right_lon",
				mapsIndexKey, entry.getName());
			return new BitmapMapBacking(imageName, imagePath, this, name, originLat,
				originLon, upperRightLat, upperRightLon);
		} else {
			double pixelsPerLat = getDoubleOrFail(mapHash, "pixels_per_lat",
					mapsIndexKey, entry.getName());
			double pixelsPerLon = getDoubleOrFail(mapHash, "pixels_per_lon",
					mapsIndexKey, entry.getName());
	
			return new BitmapMapBacking(imageName, imagePath, this, originLat,
					originLon, pixelsPerLat, pixelsPerLon, name);
		}
	}
	
	private void loadPlaces() throws IOException, WadDataFormatException {
		ZipEntry  placeIndexEntry = wad.getEntry(placesIndexKey);
		if(placeIndexEntry == null || placeIndexEntry.isDirectory()) {
			// there is no requirement that any places be defined
			return;
		}
		InputStream indexStream = wad.getInputStream(placeIndexEntry);
		SectionedFileParser indexParser = null;
		try {
			indexParser = new SectionedFileParser(
				indexStream);
		} catch (SectionedFileFormatException sffe) {
			throw new WadDataFormatException(placesIndexKey,
					WadDataFormatException.BAD_RESOURCE_ERROR,
					sffe, "");
		}
		Hashtable placeSection;
		if((placeSection = indexParser.getSection("Places")) == null) {
			throw new WadDataFormatException(placesIndexKey,
					WadDataFormatException.BAD_RESOURCE_ERROR, 
					"No Places Section");
		}
		Enumeration e = placeSection.keys();
		while(e.hasMoreElements()) {
			String placeSetName = (String)e.nextElement();
			String setPath = placesDirKey + placeSection.get(placeSetName);
			loadPlaceSet(placeSetName, setPath);
		}
	}
	
	private void loadPlaceSet(String placeSetName, String setPath) 
		throws WadDataFormatException, IOException {
		Hashtable placeSet = new Hashtable();
		String placeFilePath = setPath;
		ZipEntry placeFileEntry = wad.getEntry(placeFilePath);
		if(placeFileEntry == null) {
			throw new WadDataFormatException(placesIndexKey,
					placeFilePath,
					WadDataFormatException.MISSING_RESOURCE_ERROR, "");
		}
		InputStream placeFile = wad.getInputStream(placeFileEntry);
		SectionedFileParser p = null;
		try {
			p = new SectionedFileParser(placeFile);
		} catch(SectionedFileFormatException sffe) {
			throw new WadDataFormatException(placesIndexKey,
					placeFilePath,
					WadDataFormatException.BAD_RESOURCE_ERROR,
					sffe, "");
		}
		Hashtable setContents = p.allSections();
		Enumeration e = setContents.keys();
		while(e.hasMoreElements()) {
			String placeName = (String)e.nextElement();
			Hashtable placeHash = (Hashtable)setContents.get(placeName);
			String placeType;
			String placeUrl;
			String placeText;
			double placeLat;
			double placeLon;
			String placeImageName;
			String placeImagePath;
			placeImagePath = getStringOrFail(placeHash, "icon_file",
					placesIndexKey, placeFilePath);
			// test that the image at least exists
			if(!containsResource(placeImagePath)) {
				throw new WadDataFormatException(setPath,
						placeImagePath, 
						WadDataFormatException.MISSING_RESOURCE_ERROR, "");
			}
			placeImageName = basename(placeImagePath);
			placeLat = getDoubleOrFail(placeHash, "lat", placesIndexKey,
					placeFilePath);
			placeLon = getDoubleOrFail(placeHash, "lon", placesIndexKey,
					placeFilePath);
			placeType = getStringOrBlank(placeHash, "type");
			placeText = getStringOrBlank(placeHash, "text");
			placeUrl = getStringOrBlank(placeHash, "url");
			PlaceBacking place = new PlaceBacking(placeType,
					placeName, placeUrl, placeText, placeLat, placeLon,
					placeImageName, placeImagePath, this);
			placeSet.put(placeName, place);
		}
		places.put(placeSetName, placeSet);
	}
	
	/* helpers for parsing the sectioned files */
	private String getStringOrFail(Hashtable inHash, 
								   String forKey,
								   String referencedFrom,
								   String entry) 
		throws WadDataFormatException {
		if(inHash.containsKey(forKey)) {
			return (String)inHash.get(forKey);
		} else {
			throw new WadDataFormatException(referencedFrom,
					entry, WadDataFormatException.BAD_RESOURCE_ERROR, "");
		}
	}
	private double getDoubleOrFail(Hashtable inHash,
								   String forKey,
								   String referencedFrom,
								   String entry) 
		throws WadDataFormatException {
		try {
			return Double.parseDouble(getStringOrFail(inHash, forKey,
						referencedFrom, entry));
		} catch (NumberFormatException nfe) {
			throw new WadDataFormatException(referencedFrom, entry,
					WadDataFormatException.BAD_RESOURCE_ERROR, "");
		}
	}
	private String getStringOrBlank(Hashtable inHash, String forKey) {
		String ret;
		ret = (String)inHash.get(forKey);
		if(ret != null) return ret;
		else return "";
	}
	
	/**
	 * Returns an ImageData for the file at path in the wad
	 */
	public ImageData getImageData(String path) throws IOException {
		ImageData data = (ImageData)imageResources.get(path);
		if(data == null) {
			ZipEntry e = wad.getEntry(path);
			if(e != null) {
				data = new ImageData(wad.getInputStream(e));
				imageResources.put(path, data);
			}
		}
		return data;
	}
	
	/* methods for dealing with the ap cache */
	
	/**
	 * Find out whether the map wad contains an ap cache or not 
	 */
	public boolean containsAPCache() {
		return containsResource(myApCacheKey);
	}
	
	/**
	 * Uses JDBMMapLoader to load the ap cache into the WifiMapper
	 */
	public JDBMMapper loadAPCacheIntoMapper(String dbPath) 
		throws IOException, WadDataFormatException 
	{
		if(!containsAPCache() || (dbPath == null)) {
			return null;
		}
		MapLoader mapLoader;
		mapLoader = new MapLoader(new JDBMMapper(dbPath));
		mapLoader.createNewMap();
		mapLoader.loadMap(wad.getInputStream(wad.getEntry(myApCacheKey)));
		return (JDBMMapper)mapLoader.getMapper();
	}
	
	/**
	 * Loads the ap cache into a special place set accessable under
	 * the place set name APs.  This is used so we can display the
	 * aps on maps like we do with places.
	 * @param useIcon you must provide an ImageData to use as an
	 * 	icon for the place, because every place must have an icon, and
	 * 	I surely don't have one laying around.
	 * @return the place set for the aps
	 */
	public Hashtable loadAPCacheAsPlaces(ImageData useIcon) 
		throws IOException, WadDataFormatException
	{
		if(!containsAPCache()) {
			throw new WadDataFormatException(myApCacheKey,
				WadDataFormatException.MISSING_RESOURCE_ERROR,
				"cannot load the ap cache if it doesn't exist.");
		}
		Hashtable apSet = new Hashtable();
		BufferedReader br = new BufferedReader(new InputStreamReader(
			wad.getInputStream(wad.getEntry(myApCacheKey))));
		String line;
		while((line = br.readLine()) != null) {
			String tokens[] = StringUtil.split(line);
			if(tokens.length != 4) {
				// its too draconian to ask that the ap caches
				// have every line be perfect, because they just don't
				// in practice.  No, I have no idea why.
				continue;
			}
			double apLat;
			double apLon;
			try {
				apLat = Double.parseDouble(tokens[0]);
				apLon = Double.parseDouble(tokens[1]);
			} catch(NumberFormatException nfe) {
				continue;
			}
			String name = tokens[2];
			// mac address isn't really interesting for a place
			// but I use it as the key in the hashtable, since that
			// way it doesn't get the same ap twice
			String mac = StringUtil.canonicalizeBSSID(tokens[3]);
			PlaceBacking apPlace = new PlaceBacking("AP",
				name, "", "wireless access point: " + name, 
				apLat, apLon, "ap icon", useIcon);
			apSet.put(mac, apPlace);
		}
		places.put("APs", apSet);
		return apSet;
	}
	
	/* methods for accessing/mutating the WadData */
	public MapBacking getMap(String mapName) {
		return (MapBacking)maps.get(mapName);
	}
	public Hashtable getMaps() {
		return maps;
	}
	public void putMap(BitmapMapBacking map) {
		maps.put(map.getName(), map);
	}
	public Enumeration getPlaceSetNames() {
		return places.keys();
	}
	/**
	 * @return a Hashtable where place set names =>
	 * Hashtables where place names => PlaceBackings
	 */
	public Hashtable allPlaceSets() {
		return this.places;
	}
	public Hashtable getPlaceSet(String setName) {
		return (Hashtable)places.get(setName);
	}
	public PlaceBacking getPlace(String setName, String placeName) {
		Hashtable set = getPlaceSet(setName);
		if(set != null) {
			return (PlaceBacking)set.get(placeName);
		} else {
			return null;
		}
	}
	public void putPlaceSet(String setName, Hashtable set) {
		places.put(setName, set);
	}
	public void addPlace(String setName, PlaceBacking place) {
		Hashtable set = getPlaceSet(setName);
		if(set != null) {
			set.put(place.name, place);
		}
	}
	/**
	 * Returns the MapBacking specified as the default for the wad
	 * or null if no MapBacking is specified
	 */
	public MapBacking getDefaultMap() {
		if(defaultMap == null) return null;
		else return getMap(defaultMap);
	}
	/**
	 * Returns a Hashtable containing the default place sets 
	 * (Hashtables of PlaceBackings) for the
	 * wad or null if no default place sets are specified.
	 */
	public Hashtable getDefaultPlaceSets() {
		return defaultPlaces;
	}
	
	/* methods for writing WadData */
	
	/**
	 * Writes everything that is currently in the WadData out to
	 * a map wad at path.  A note on how images are written: all images
	 * are written out to an entry in the zip with the filename
	 * chosen by the wadRelativeImagePath attribute of the item being written.  If I have
	 * already written that image file, I do nothing.  Despite the fact that
	 * it would not increase the wad file size as a result of having two copies
	 * of the same image and making the copies ensures that two different images
	 * with the same name could coexist, this would defeat my attempt to only
	 * have at most one copy of each image in memory.
	 */
	/*public void write(String path) throws IOException {
		ZipOutputStream out = new ZipOutputStream(
				new FileOutputStream(path));
		
		// don't want to write the same entries twice
		HashSet entriesWritten = new HashSet();
		
		Properties mapIndex = new Properties();
		Properties placesIndex = new Properties();
		
		Enumeration m = maps.elements();
		while(m.hasMoreElements()) {
			MapBacking map = (MapBacking)m.nextElement();
			
			// since the names are already unique in the Hashtable
			// i know I haven't already written a .meta file with this
			// name
			String mapMetaPath = mapsDirKey + map.name + ".meta";
			Properties mapMeta = map.getProperties();
			ZipEntry metaEntry = new ZipEntry(mapMetaPath);
			out.putNextEntry(metaEntry);
			mapMeta.store(out, map.name);
			out.closeEntry();
			
			// now do the map image
			String imagePath = map.getWadRelativeImagePath();
			// i must check that I haven't already written this image
			if(!(entriesWritten.contains(imagePath))) { 
				ZipEntry imageEntry = new ZipEntry(imagePath);
				saveImage(out, imageEntry, map.getImageResource());
			}
			// add the entry for this map to the index
			mapIndex.put(map.name, imagePath);
		}
		
		// put the map index
		ZipEntry mapIndexEntry = new ZipEntry(mapsIndexKey);
		out.putNextEntry(mapIndexEntry);
		mapIndex.store(out, "Hashtable Index");
		out.closeEntry();
		
		// now do the places
		
	}*/
	
	
	
	
//	/* little helpers */
//	private void saveImage(ZipOutputStream out, ZipEntry at, ImageData image)
//		throws IOException {
//		ImageLoader imageWriter = new ImageLoader();
//		imageWriter.data = new ImageData[1];
//		imageWriter.data[0] = image;
//		out.putNextEntry(at);
//		try {
//			imageWriter.save(out, image.type);
//		} catch(SWTException swte) {
//			throw new IOException("couldn't write image " +
//					at.getName() + "with error: " + swte.toString());
//		}
//		out.closeEntry();
//	}

	private String basename(String path) {
		return path.substring(path.lastIndexOf('/') + 1,
					   		path.length() - 1);
	}
	private boolean containsResource(String path) {
		ZipEntry entry = wad.getEntry(path);
		return !(entry == null || entry.isDirectory());
	}
	public InputStream getArbitraryResource(String path) throws IOException {
		if(!containsResource(path)) return null;
		return wad.getInputStream(wad.getEntry(path));
	}
}
