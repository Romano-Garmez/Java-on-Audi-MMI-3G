package org.placelab.jsr0179;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.location.AddressInfo;
import javax.microedition.location.Landmark;
import javax.microedition.location.LandmarkStore;
import javax.microedition.location.QualifiedCoordinates;

import org.placelab.core.PlacelabProperties;
import org.placelab.util.StringUtil;

/**
 * Filesystem-based implementation of a JSR-179 LandmarkStore.
 * 
 * NOTE: The way this store works isn't completely consistent with how you would
 * expect the LandmarkStore to operate in regards to categories.
 * 
 * In the spec, a landmark exists in one place but can be associated with one or
 * more categories. In this implementation, a landmark is physically tied to a
 * category, and is stored in a category file. It exists in the store as many
 * times as the categories it belongs to.
 * 
 */

public class FSLandmarkStore extends LandmarkStore {

	private static final String CAT_EXT = ".cat.txt";
	
	private File root;
	
	public FSLandmarkStore () throws Exception {

		String pdir = PlacelabProperties.get("placelab.dir");
		String ldir = PlacelabProperties.get("placelab.landmarks.dir");
		
		if (ldir.equals("")) {
			// default to something withing placelab.dir
			
			if (pdir.equals(""))
				throw new Exception("placelab.landmarks.dir not set and neither is placelab.dir");
			
			root = new File((new File(pdir)).getAbsolutePath() + "/landmarks");
		} else {
			root = new File(ldir);
		}
		
		if (!root.exists() || !root.isDirectory()) {
			if (!root.mkdirs())
				throw new Exception("Directory " + root.getAbsolutePath() + " did not exist.  I tried to create it but failed.");
		}
		
	}
	
	public void addCategory (String categoryName) throws IOException {
		File newCat = new File(root, categoryName + CAT_EXT);
		if (!newCat.createNewFile()) {
			throw new IOException("Cannot create file: " + newCat.getAbsolutePath());
		}
	}
	
	public void addLandmark (Landmark landmark, String category) throws IOException {
		// check to see if it already exists
	
		try {
			getCategoryFile(category);
		} catch (IOException e) {
			addCategory(category);
		}
		
		for (Enumeration e = getLandmarks(category, landmark.getName()); (e != null) && e.hasMoreElements(); ) {
			Landmark l = (Landmark) e.nextElement();
			if (l.getName().equals(landmark.getName())) {
				deleteLandmark(landmark);
			}
		}
		
		PrintWriter out = new PrintWriter( new FileOutputStream (getCategoryFile(category).getAbsolutePath(), true));
		out.println(landmarkToString(landmark));
		out.close();
		
	}
	
	public void deleteCategory (String categoryName) throws IOException {
		getCategoryFile(categoryName).delete();
	}
	
	public void deleteLandmark (Landmark landmark) throws IOException {
		for (Enumeration e = getCategories(); e.hasMoreElements(); ) {
			String category = (String) e.nextElement();
			
			Enumeration matches = getLandmarks(category);
			
			if (matches == null)
				continue;
			
			for (;matches.hasMoreElements();) {
				Landmark l = (Landmark) matches.nextElement();
				
				if (landmark.getName().equals(l.getName())) {
					File tmp = File.createTempFile("category", "tmp");
					File cat = getCategoryFile(category);
					
					BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(cat)));
					PrintWriter out = new PrintWriter(new FileOutputStream(tmp.getAbsolutePath()));
					
					for (;;) {
						String line = in.readLine();
						if (line == null)
							break;
						
						String parts[] = StringUtil.split(line, '|');
						String name = StringUtil.percentUnescape(parts[0]);
						
						if (name.equals(landmark.getName()))
							continue;
						
						out.println(line);
					}
					
					in.close();
					out.close();
					
					cat.delete();
					
					tmp.renameTo(cat);
				}
				
			}
			
			
			 
		}
	}
	
	
	public Enumeration getCategories () {
		File[] cats = root.listFiles();
		
		Vector dirs = new Vector();
		
		for (int i=0;i<cats.length;i++) {
			if (cats[i].isFile() && cats[i].getAbsolutePath().endsWith(CAT_EXT)) {
				String file = cats[i].getName();
				dirs.addElement(file.substring(0, file.indexOf(CAT_EXT)));
			}
		}
		
		return dirs.elements();
	}
	
	public Enumeration getLandmarks() throws IOException {
		
		Vector landmarks = new Vector();
		
		for (Enumeration cats = getCategories(); cats.hasMoreElements();) {
			Enumeration e = getLandmarks((String) cats.nextElement());
			
			if (e == null)
				continue;
			
			for (;e.hasMoreElements();) 
				landmarks.addElement((Landmark) e.nextElement());
		}
		
		if (landmarks.size() == 0)
			return null;
		
		return landmarks.elements();
	}
	
	private Enumeration getLandmarks (String category) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(getCategoryFile(category).getAbsolutePath())));
		
		Vector matches = new Vector();
		
		for (;;) {
			String line = br.readLine();
			if (line == null)
				break;

			matches.addElement(landmarkFromString(line));
		}
		
		if (matches.size() == 0)
			return null;
		
		br.close();
		
		return matches.elements();
	}

	public Enumeration getLandmarks (String category, double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(getCategoryFile(category).getAbsolutePath())));
		
		Vector matches = new Vector();
		
		for (;;) {
			String line = br.readLine();
			if (line == null)
				break;

			Landmark landmark = landmarkFromString(line);
			
			QualifiedCoordinates qc = landmark.getQualifiedCoordinates();
			
			if ( (qc.getLatitude() < minLatitude) || (qc.getLatitude() > maxLatitude))
				continue;
			
			if ( (qc.getLongitude() < minLongitude) || (qc.getLongitude() > maxLongitude))
				continue;
			
			matches.addElement(landmark);
		}
		
		if (matches.size() == 0)
			return null;
		
		br.close();
		
		return matches.elements();
	}
	
	public Enumeration getLandmarks (String category, String name) throws IOException {
		
		// match categories
		Vector categories = new Vector();
		for (Enumeration e = getCategories(); e.hasMoreElements(); ) {
			String cat = (String) e.nextElement();
			
			if (category == null) {
				categories.addElement(category);
			} else if (cat.indexOf(category) == -1) {
				categories.addElement(cat);
			}
		}
		
		
		Vector matches = new Vector();
		
		for (Enumeration e = categories.elements(); e.hasMoreElements(); ) {
			String cat = (String) e.nextElement();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(getCategoryFile(cat).getAbsolutePath())));
			
			for (;;) {
				String line = br.readLine();
				if (line == null)
					break;

				if (name == null) {
					matches.addElement(landmarkFromString(line));
				} else {
					String[] parts = StringUtil.split(line, '|');
					
					if  ( (parts == null) || (parts.length == 0) )
						continue;
					
					if (parts[0].toLowerCase().indexOf(name.toLowerCase()) == -1)
						continue;
					
					matches.addElement(landmarkFromString(line));	
				}
			}
			
			br.close();
		}
		
		if (matches.size() == 0)
			return null;
	
		return matches.elements();
	}
	
	public void removeLandmarkFromCategory(Landmark lm, String category) throws IOException {
		// just ignore category - you can't have the same landmark in more than one class
		deleteLandmark(lm);
	}
	
	protected static Landmark landmarkFromString (String str) {
		String[] parts = StringUtil.split(str, '|');
		
		String name = StringUtil.percentUnescape(parts[0]);
		String desc = StringUtil.percentUnescape(parts[1]);
		String[] coors = StringUtil.split(parts[2], ',');
		String[] addrInfo = StringUtil.split(parts[3], '=');
		
		QualifiedCoordinates qc = new QualifiedCoordinates(
				Double.parseDouble(coors[0]),	// lat
				Double.parseDouble(coors[1]), // lon
				Float.parseFloat(coors[2]),	// alt
				Float.parseFloat(coors[3]),	// horiz accuracy
				Float.parseFloat(coors[4])	// vertical accuracy
		);
		
		AddressInfo ai = new AddressInfo();
		
		for (int i=0;i<addrInfo.length;i++) {
			addrInfo[i] = StringUtil.percentUnescape(addrInfo[i]);
		}
		
		ai.setField(AddressInfo.BUILDING_FLOOR, addrInfo[0]);
		ai.setField(AddressInfo.BUILDING_NAME, addrInfo[1]);
		ai.setField(AddressInfo.BUILDING_ROOM, addrInfo[2]);
		ai.setField(AddressInfo.BUILDING_ZONE, addrInfo[3]);
		ai.setField(AddressInfo.CITY, addrInfo[4]);
		ai.setField(AddressInfo.COUNTY, addrInfo[5]);
		ai.setField(AddressInfo.COUNTRY_CODE, addrInfo[6]);
		ai.setField(AddressInfo.CROSSING1, addrInfo[7]);
		ai.setField(AddressInfo.CROSSING2, addrInfo[8]);
		ai.setField(AddressInfo.DISTRICT, addrInfo[9]);
		ai.setField(AddressInfo.EXTENSION, addrInfo[10]);
		ai.setField(AddressInfo.PHONE_NUMBER, addrInfo[11]);
		ai.setField(AddressInfo.POSTAL_CODE, addrInfo[12]);
		ai.setField(AddressInfo.STATE, addrInfo[13]);
		ai.setField(AddressInfo.STREET, addrInfo[14]);
		ai.setField(AddressInfo.URL, addrInfo[15]);
		
		return new Landmark(name, desc, qc, ai);
	}
	
	protected static String landmarkToString (Landmark landmark) {
		
		String name = landmark.getName();
		String desc = landmark.getDescription();
		QualifiedCoordinates qc = landmark.getQualifiedCoordinates();
		
		String coors = Double.toString(qc.getLatitude()) + "," +
					Double.toString(qc.getLongitude()) + "," +
					Float.toString(qc.getAltitude()) + "," +
					Float.toString(qc.getHorizontalAccuracy()) + "," +
					Float.toString(qc.getVerticalAccuracy());
		
		AddressInfo ai = landmark.getAddressInfo();
		
		String addr = StringUtil.percentEscape(ai.getField(AddressInfo.BUILDING_FLOOR)) + "="
				+ StringUtil.percentEscape(ai.getField(AddressInfo.BUILDING_NAME)) + "="
				+ StringUtil.percentEscape(ai.getField(AddressInfo.BUILDING_ROOM)) + "="
				+ StringUtil.percentEscape(ai.getField(AddressInfo.BUILDING_ZONE)) + "="
				+ StringUtil.percentEscape(ai.getField(AddressInfo.CITY)) + "="
				+ StringUtil.percentEscape(ai.getField(AddressInfo.COUNTY)) + "="
				+ StringUtil.percentEscape(ai.getField(AddressInfo.COUNTRY_CODE)) + "="
				+ StringUtil.percentEscape(ai.getField(AddressInfo.CROSSING1)) + "="
				+ StringUtil.percentEscape(ai.getField(AddressInfo.CROSSING2)) + "="
				+ StringUtil.percentEscape(ai.getField(AddressInfo.DISTRICT)) + "="
				+ StringUtil.percentEscape(ai.getField(AddressInfo.EXTENSION)) + "="
				+ StringUtil.percentEscape(ai.getField(AddressInfo.PHONE_NUMBER)) + "="
				+ StringUtil.percentEscape(ai.getField(AddressInfo.POSTAL_CODE)) + "="
				+ StringUtil.percentEscape(ai.getField(AddressInfo.STATE)) + "="
				+ StringUtil.percentEscape(ai.getField(AddressInfo.STREET)) + "="
				+ StringUtil.percentEscape(ai.getField(AddressInfo.URL));
 		
		return StringUtil.percentEscape(name) + "|" + StringUtil.percentEscape(desc)
				+ "|" + coors + "|" + addr;
	}
	
	private File getCategoryFile (String category) throws IOException {
		
		File f =  new File(root, category + ((category.endsWith(".cat.txt") ? "" : CAT_EXT)));
		
		if (!f.isFile() || !f.canRead())
			throw new IOException("cannot read category file: " + f.getAbsolutePath());
		
		return f;
	}
	

	
}
