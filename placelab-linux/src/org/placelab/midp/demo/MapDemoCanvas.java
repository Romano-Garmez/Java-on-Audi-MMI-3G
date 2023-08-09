package org.placelab.midp.demo;

import java.io.*;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;

import org.placelab.client.PlacelabException;
import org.placelab.client.PlacelabPhone;
import org.placelab.client.tracker.Estimate;
import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.core.Coordinate;
import org.placelab.core.Types;
import org.placelab.mapper.Beacon;
import org.placelab.midp.EventLogger;
import org.placelab.midp.RMSMapper;
import org.placelab.util.FixedPointLong;
import org.placelab.util.FixedPointLongException;

/**
 * This is the Canvas used by the XMapDemo to display images on the phone screen.
 * The canvas renders a background image along with all known beacons as dots 
 * on the map. As estimates are computed by Place Lab, an icon is updated to
 * show the estimated position.
 * 
 */

class MapDemoCanvas extends GameCanvas implements Runnable,CommandListener{
	private final static int OFFSET = 5;

	protected Image mapImage;
	public static Image beaconImage;
	public static Image beaconImageSeen;
	public static Image locationImage;
	public static Image gsmImage;
	public static int locationImageWidth,locationImageHeight;
	
	//load the images once
	static {
		try {
			beaconImage = Image.createImage("/icon_bt.png");
			gsmImage = Image.createImage("/icon_gsm.png");
			beaconImageSeen = Image.createImage("/icon_seen.png");
			locationImage = Image.createImage("/icon_me.png");
			locationImageWidth = locationImage.getWidth();
			locationImageHeight = locationImage.getHeight();

		} catch(IOException ioe) {
			EventLogger.logError(ioe);
			ioe.printStackTrace();
		}
	}
	
	boolean focus;
	boolean stop;
	
	int xPos,yPos;
	int screenWidth,screenHeight;
	int imageWidth,imageHeight;
	int delayTime;
	
	long xCoordLeft,yCoordLeft;
	long xCoordRight,yCoordRight;
	long scaleX,scaleY;

	
	HashMap beacons; //linked list of BeaconPositions
	Estimate latestEstimate;

	PlacelabPhone placelab;
	RMSMapper map;

	MapDemoMidlet demoMidlet;

	Command backCommand = new Command("Back", Command.BACK,1);
	Command focusOnCommand = new Command("Focus On",Command.ITEM,1);
	Command focusOffCommand = new Command("Focus Off",Command.ITEM,1);
	
	/**
	 * Takes the bottom left and top right corner as reference points
	 * as a String (e.g. (0.091394,52.21017) and (0.092679,52.210718333))
	 * NOTE: Longitude is x-axis and Latitude is y-axis
	 * 
	 * A linear mapping is assumed between the number of lat/lon per pixel
	 * 
	 * @param xCoordLeftStr bottom left corner longitude value
	 * @param yCoordLeftStr bottom left corner latitude value
	 * @param xCoordRightStr top right corner longitude value
	 * @param yCoordRightStr top right corner latitude value
	 * @param mapImage background map image
	 * @param midlet map demo midlet
	 * @param placelab placelab phone daemon
	 */
	public MapDemoCanvas(String xCoordLeftStr,String yCoordLeftStr,
					  String xCoordRightStr,String yCoordRightStr,
					  Image mapImage,MapDemoMidlet midlet,PlacelabPhone placelab) {
		super(true);
		demoMidlet = midlet;
		
		//load images
			this.mapImage = mapImage;
			imageWidth = mapImage.getWidth();
			imageHeight = mapImage.getHeight();		


		//calculate scales
		try {
			xCoordLeft = FixedPointLong.stringToFlong(xCoordLeftStr);
			yCoordLeft = FixedPointLong.stringToFlong(yCoordLeftStr);
			xCoordRight = FixedPointLong.stringToFlong(xCoordRightStr);
			yCoordRight = FixedPointLong.stringToFlong(yCoordRightStr);

			long widthFlong = FixedPointLong.stringToFlong(Integer.toString(imageWidth));
			scaleX = FixedPointLong.div(widthFlong,(xCoordRight-xCoordLeft));
			long heightFlong = FixedPointLong.stringToFlong(Integer.toString(imageHeight));
			scaleY = FixedPointLong.div(heightFlong,(yCoordRight-yCoordLeft));

//			System.err.println("scaleX: "+FixedPointLong.flongToString(scaleX) + " scaleY: "+FixedPointLong.flongToString(scaleY));
		} catch(FixedPointLongException fple) {
			EventLogger.logError(fple);
			fple.printStackTrace();
		}

		screenWidth = getWidth();
		screenHeight = getHeight();

		xPos = 0;
		yPos = 0;
		delayTime = 20;

		//placelab will open the map
		this.placelab = placelab;
		map = (RMSMapper) placelab.getMapper();
			
	
		//read in beacons
		beacons = new HashMap();
		readBeacons();

		focus = true;
		addCommand(focusOffCommand);
		addCommand(backCommand);
		setCommandListener(this);
	}


	/**
	 * Stop the demo
	 */
	public void stop() {
		stop = true;
	}
	
	
	public void run() {
		try {
		stop = false;
		Graphics g = getGraphics();
		while(!stop) {
				checkKeys();
				tick();
				render(g);
			try {
				Thread.sleep(delayTime);
			} catch(InterruptedException ie) {
				EventLogger.logError(ie);
				ie.printStackTrace();
			}
		}

	} catch(Throwable t) {
		t.printStackTrace();
		EventLogger.logError("demo canvas "+
	        					t.getClass().getName()+":"+t.getMessage());
	}

	}
	
	public void render(Graphics g) {
		int estimateX = 0;
		int estimateY = 0;
		if(latestEstimate != null) {
			estimateX = getMapPositionX(latestEstimate.getCoord().getLongitudeAsString());
			estimateY = getMapPositionY(latestEstimate.getCoord().getLatitudeAsString());

			//put our estimate in the middle
			if(focus) {
				xPos = Math.min(0,Math.max(screenWidth-imageWidth,(-1*estimateX) + (screenWidth/2)));
				yPos = Math.min(0,Math.max(screenHeight-imageHeight,(-1*estimateY) + (screenHeight/2)));
			}
		}
		
		//render background image
		g.drawImage(mapImage, xPos,yPos, Graphics.TOP | Graphics.LEFT);

		//render beacons
		LinkedList beaconList = getBeacons();
		for(Iterator i = beaconList.iterator();i.hasNext();) {
			BeaconPosition bp = (BeaconPosition) i.next();
			
			//xPos and yPos are negative, so add them
			if(bp.isSeen()) {
				g.drawImage(beaconImageSeen, bp.getX() + xPos, bp.getY() + yPos, Graphics.TOP | Graphics.LEFT);
				bp.setSeen(false); //reset the boolean
			} else if(bp.getType().equals(Types.GSM)){
				g.drawImage(gsmImage, bp.getX() + xPos, bp.getY() + yPos, Graphics.TOP | Graphics.LEFT);				
			} else {
				g.drawImage(beaconImage, bp.getX() + xPos, bp.getY() + yPos, Graphics.TOP | Graphics.LEFT);				
			}
		}
			
		//render estimate coordinates
		if(intersectsWithScreen(estimateX,estimateY)) {
			g.drawImage(locationImage,estimateX+xPos,estimateY+yPos,Graphics.TOP | Graphics.LEFT);
		}
		if(estimateX > imageWidth || estimateY > imageHeight)
			g.drawString("OFF THE MAP",5,0,Graphics.TOP | Graphics.LEFT);
		else
			g.drawString(estimateX+","+estimateY,5,0,Graphics.TOP | Graphics.LEFT);

		flushGraphics();
	}

	protected void checkKeys() {
		int keyStates = getKeyStates();
		
		if ((keyStates & RIGHT_PRESSED) != 0) { // right pointer stick key
			xPos = Math.max(xPos-OFFSET,screenWidth-imageWidth);
		} else if ((keyStates & LEFT_PRESSED) != 0) { //left pointer stick key
			xPos = Math.min(xPos+OFFSET,0);
		} else if ((keyStates & UP_PRESSED) != 0) { //up pointer stick key
			yPos = Math.min(yPos+OFFSET,0);
		} else if ((keyStates & DOWN_PRESSED) != 0) { //down pointerstick key
			yPos = Math.max(yPos-OFFSET,screenHeight-imageHeight);
		}
	}

	public void tick() {
		try {
			Estimate e = placelab.getLatestEstimate();			
			if(e == null) 
				return;
			latestEstimate = e;

			for(Iterator i = placelab.getEstimateBeacons();i.hasNext();) {
				Beacon b = (Beacon) i.next();
				BeaconPosition bp = (BeaconPosition) beacons.get(b.getId());
				if(bp == null) {
					//this beacon is not within the map coordinates
					continue;
				}
				bp.setSeen(true);
			}
		} catch(PlacelabException pe) {
			EventLogger.logError(pe);
			pe.printStackTrace();
		} 
	}
	
	
	/**
	 * Read beacons in from mapper that are within the bounds of 
	 * this map's coordinate space
	 */
	private void readBeacons() {		
		//mapper is opened by PlacelabPhone
		try {
			Coordinate left = Types.newCoordinate(FixedPointLong.flongToString(yCoordLeft),
												  FixedPointLong.flongToString(xCoordLeft));
			Coordinate right = Types.newCoordinate(FixedPointLong.flongToString(yCoordRight),
					                               FixedPointLong.flongToString(xCoordRight));
			for(Iterator i = map.query(left,right);i.hasNext();) {
				Beacon b = (Beacon) i.next();
				Coordinate c = b.getPosition();
				BeaconPosition bp = new BeaconPosition(b.getType(),c.getLongitudeAsString(),c.getLatitudeAsString());
				beacons.put(b.getId(),bp);
			}
		} catch(FixedPointLongException fple) {
			EventLogger.logError(fple);
			fple.printStackTrace();
		}
	}
	
	/**
	 * Get the beacons that should show up on screen
	 * @return linked list of beacons
	 */
	public LinkedList getBeacons() {		
		LinkedList intersectingBeacons = new LinkedList();

try {
		for(Iterator i = beacons.values().iterator();i.hasNext();) {
			BeaconPosition bp = (BeaconPosition) i.next();

			if(intersectsWithScreen(bp.getX(),bp.getY())) {
				intersectingBeacons.add(bp);
			}
		}
} catch(Throwable t) {
	t.printStackTrace();
	EventLogger.logError("getbeacons"+
        					t.getClass().getName()+":"+t.getMessage());
}

		return intersectingBeacons; 
	}

	/**
	 * Tests whether the x,y value intersects with the screen
	 * @param xValue x value of the object
	 * @param yValue y value of the object
	 * @return true if intersects, false otherwise
	 */
	protected boolean intersectsWithScreen(int xValue, int yValue) {
		return (xValue < ((-1 * xPos) + screenWidth)) && 
				(xValue > (-1 * xPos)) &&
				(yValue < ((-1*yPos) + screenHeight)) &&
				(yValue > (-1*yPos));
		
	}

	protected int getMapPositionX(String xCoordinate) {
		try {
		long xCoord = FixedPointLong.stringToFlong(xCoordinate);
		long deltaX = xCoord - xCoordLeft;
		long dX = FixedPointLong.mult(deltaX,scaleX);
		return Integer.parseInt(FixedPointLong.flongToString(FixedPointLong.intPart(dX)));
		} catch(FixedPointLongException fple) {
			EventLogger.logError(fple);
			fple.printStackTrace();
			return 0;
		}
	}

	protected int getMapPositionY(String yCoordinate) {
		try {
		long yCoord = FixedPointLong.stringToFlong(yCoordinate);
		long deltaY = yCoordRight - yCoord;
		long dY = FixedPointLong.mult(deltaY,scaleY);
		return Integer.parseInt(FixedPointLong.flongToString(FixedPointLong.intPart(dY)));
		} catch(FixedPointLongException fple) {
			EventLogger.logError(fple);

			fple.printStackTrace();
			return 0;
		}
	}

	
	/**
	 * Private inner class to store each beacon position and whether
	 * it is being used to comput an estimate
	 */
	private class BeaconPosition {
		long xCoord,yCoord; //stored as flongs representing lat/lon
		int xPosition,yPosition; // integer position on the map
		boolean seen;
		String type;
		
		public BeaconPosition(String type,String xCoordStr,String yCoordStr) {
			this.type = type;
			try {
				xCoord = FixedPointLong.stringToFlong(xCoordStr);
				yCoord = FixedPointLong.stringToFlong(yCoordStr);

				xPosition = getMapPositionX(xCoordStr);
				yPosition = getMapPositionY(yCoordStr);
				seen = false;

			} catch(FixedPointLongException fple) {
				EventLogger.logError(fple);

				fple.printStackTrace();
			}
		}
		
		public int getX() {
			return xPosition;
		}
		public int getY() {
			return yPosition;
		}
		
		public String getType() {
			return type;
		}
		
		public String toString() { 
			return "Type: " + type + " X: "+xPosition + " Y: "+yPosition;
		}
		
		public boolean isSeen() {
			return seen;
		}
		
		public void setSeen(boolean seen) {
			this.seen = seen;
		}
	}


	public void commandAction(Command c, Displayable d) {
		if(c == backCommand) {
			stop=true;
			demoMidlet.setHome();
		} else if(c == focusOnCommand) {
			focus = true;
			removeCommand(focusOnCommand);
			addCommand(focusOffCommand);
			flushGraphics();
		} else if(c == focusOffCommand) {
			focus = false;
			removeCommand(focusOffCommand);
			addCommand(focusOnCommand);
		}
	}
	
	
// TYS - tweaking code, leave here for now
//	int[] lons = { 15, 198, 265,350 ,548, 530,540,623};
//	int[] lats = { 347, 223, 388,220, 388, 190,70,250 };
//
//	for(int i=0;i<lats.length;i++) {
//		try {
//			long f1 = FixedPointLong.stringToFlong(Integer.toString(lons[i]));
//			long deltaX = FixedPointLong.div(f1,scaleX);				
//			long lon = xCoordLeft + deltaX;
//			
//			long f2 = FixedPointLong.stringToFlong(Integer.toString(lats[i]));
//			long deltaY = FixedPointLong.div(f2,scaleY);
//			long lat = yCoordRight - deltaY;
//
//			System.err.println(FixedPointLong.flongToString(lat)+","+FixedPointLong.flongToString(lon));
//
//			
//			BeaconPosition bp = new BeaconPosition(Types.BLUETOOTH,FixedPointLong.flongToString(lon),
//													FixedPointLong.flongToString(lat));
//			beacons.put(Integer.toString(i),bp);
//			System.err.println("loading: "+bp);
//			
//		} catch(FixedPointLongException fple) {
//			fple.printStackTrace();
//		}
//	}	
}