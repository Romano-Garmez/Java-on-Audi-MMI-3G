package org.placelab.midp.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.placelab.client.PlacelabException;
import org.placelab.client.PlacelabPhone;
import org.placelab.client.tracker.IntersectionTracker;
import org.placelab.collections.HashMap;
import org.placelab.collections.LinkedList;
import org.placelab.midp.EventLogger;
import org.placelab.util.StringUtil;

/**
 * This is the XMapDemo MidLet which is the entry point for showing position
 * estimates on a map. Map information is loaded from a text file which contains
 * the image location, and lat/lon coordinates for the bottom left and top right
 * corners of the image. 
 *
 */

public class MapDemoMidlet extends MIDlet implements CommandListener{
	public static final String MAPFILE = "MapData.txt";
	
	// identifiers for the file
	public static final String AREA = "AREA";
	public static final String MAP = "MAP";
	public static final String LEFTLAT = "LEFTLAT";
	public static final String LEFTLON = "LEFTLON";
	public static final String RIGHTLAT = "RIGHTLAT";
	public static final String RIGHTLON = "RIGHTLON";
	
	//Canvas for the Demo
	MapDemoCanvas canvas;
	
	List areaList;
	LinkedList areas;

	Display display;
	PlacelabPhone placelab;
	Command showC,exitC;
	
	public MapDemoMidlet() {
		areaList = new List("Area List",List.IMPLICIT);
		areaList.setFitPolicy(List.TEXT_WRAP_ON);
		showC = new Command("Show Map", Command.OK, 1);
		exitC = new Command("Exit",Command.EXIT,1);
		areaList.addCommand(showC);
		areaList.addCommand(exitC);
		areaList.setCommandListener(this);

		areas = new LinkedList();
		//READ IN MAP DATA
		readData();

		//load placelab
		try {
			placelab = new PlacelabPhone();
            ((IntersectionTracker) placelab.getTracker()).setMaxLife(40000); //40 seconds
			placelab.start();			
		} catch(PlacelabException pe) {
			EventLogger.logError(pe);
			pe.printStackTrace();
		}
	}

	/*
	 * Reads data from the map file, constructing private
	 * MapData objects to hold the information
	 */
	private void readData() {
		InputStream is = getClass().getResourceAsStream("/" + MAPFILE);
		if (is == null) {
			waitForAlert("Cannot find resource file " + MAPFILE);
			return;
		}

		try {
			Reader reader = new InputStreamReader(is);
			if(reader == null) {
			    waitForAlert("Loading failed - null Reader");
			    return;
			}
			for(int i=0;true;i++) {
					String line = StringUtil.readLine(reader);
					if (line == null) break;
					if(line.length()==0) continue;
					if(line.startsWith("#")) continue;

					parseLine(line.trim());
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/*
	 * Parses a line of the MapData file
	 */
	private void parseLine(String line) {
		HashMap map = StringUtil.storageStringToHashMap(line);
		if (map==null || map.isEmpty()) {
			System.err.println("bad line: "+line);
			return;
		}
		
		MapData md = new MapData((String) map.get(MapDemoMidlet.AREA),
								 (String) map.get(MapDemoMidlet.MAP),
								 (String) map.get(MapDemoMidlet.LEFTLAT),
								 (String) map.get(MapDemoMidlet.LEFTLON),
								 (String) map.get(MapDemoMidlet.RIGHTLAT),
								 (String) map.get(MapDemoMidlet.RIGHTLON));
		
		areaList.append((String) map.get(MapDemoMidlet.AREA),null);
		areas.add(md);
	}
	
	public void waitForAlert(String str) {
		Alert a = new Alert("XMapDemo", str, null, AlertType.INFO);
		display.setCurrent(a,areaList);
		while (a.isShown());
	}
	
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		try {
			placelab.stop();
		} catch(PlacelabException pe) { 
			pe.printStackTrace();
		}	
	}

	protected void startApp() throws MIDletStateChangeException {
		display = Display.getDisplay(this);
		display.setCurrent(areaList);
	}

	protected void pauseApp() {		
	}

	public void commandAction(Command c, Displayable d) {
		if(c == List.SELECT_COMMAND  || c == showC) {
			//get the selected index
			MapData md = (MapData) areas.get(areaList.getSelectedIndex());
			
			canvas = new MapDemoCanvas(md.getLeftLon(),md.getLeftLat(),
									md.getRightLon(),md.getRightLat(),
									md.getMapImage(),this,placelab);
			Thread t = new Thread(canvas);
			display.setCurrent(canvas);
			t.start();
		} else if(c == exitC) {
			try {
				destroyApp(false);
			}catch(Exception e){}
			notifyDestroyed();
		}
	}

	public void setHome() {
		display.setCurrent(areaList);
	}
	
	/*
	 * Private inner class to hold information for each map.
	 */
	private class MapData {
		String mapFileName;
		String leftLat,leftLon;
		String rightLat,rightLon;
		String areaName;
		Image mapImage;
		
		public MapData(String areaName,String mapFileName,String leftLat,String leftLon,String rightLat,String rightLon) {
			this.mapFileName = mapFileName;
			this.leftLat = leftLat;
			this.leftLon = leftLon;
			this.rightLat = rightLat;
			this.rightLon = rightLon;
			this.areaName = areaName;
		}

		private void loadImage() {
			try {
				mapImage = Image.createImage("/"+mapFileName);
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}

		public Image getMapImage() { 
			if(mapImage == null)
				loadImage();
	
			return mapImage; 
		}
		
		public String getMapFileName() { return mapFileName; }
		public String getLeftLat() { return leftLat; }
		public String getLeftLon() { return leftLon; }
		public String getRightLat() { return rightLat; }
		public String getRightLon() { return rightLon; }
		public String getAreaName() { return areaName; }
	}
}
