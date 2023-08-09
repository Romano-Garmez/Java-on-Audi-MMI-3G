package org.placelab.midp.debug;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.placelab.collections.LinkedList;
import org.placelab.collections.ListIterator;
import org.placelab.core.Measurement;
import org.placelab.spotter.InquiryBluetoothGPSSpotter;
import org.placelab.spotter.ScanOnceListener;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;

/**
 * Communicates with a bluetooth GPS device and displays a human readable
 * version of the NMEA sentences produced
 */
public class BluetoothGPSSpotterMidlet extends MIDlet implements 
		ScanOnceListener,CommandListener {

	protected Form form;
	protected boolean alive;
	protected StringItem gpsStringItem;
	
	protected List myList;
	public static BluetoothGPSSpotterMidlet instance;
	//protected Command startCommand = new Command("Start",Command.ITEM,1);
	StringBuffer sb;
	
	InquiryBluetoothGPSSpotter bgps;
	
	public BluetoothGPSSpotterMidlet() {
		super();
		instance = this;
		bgps = null;
		sb = new StringBuffer();
	}

	protected void startApp() throws MIDletStateChangeException {
		Display d = Display.getDisplay(this);
		d.setCurrent(getForm());
		//myList = new List("none",List.IMPLICIT);
		//d.setCurrent(myList);
		alive = true;
		start();
		//run();
		//new Thread(this).start();
		//System.err.println("done startapp");
	}

	protected void pauseApp() {
		alive = false;
	}

	protected void destroyApp(boolean flag) throws MIDletStateChangeException {
		alive = false;
	}

	protected Form getForm() {
		if (form == null) {
			form = new Form("Bluetooth GPS");
			form.append(getGPSStringItem());
			//form.addCommand(startCommand);
			form.setCommandListener(this);
		}
		return form;
	}

	public StringItem getGPSStringItem() {
		if (gpsStringItem == null) {
			gpsStringItem = new StringItem("GPS Readings", "");
		}
		return gpsStringItem;
	}

	public void start() {
		appendString("Starting Spotter");
		if(bgps == null) {
			bgps = new InquiryBluetoothGPSSpotter();
			bgps.open();			
			bgps.addListener(this);	
		}
		bgps.startScanning();
	}

	
	
	
	public void gotMeasurement(Spotter sender, Measurement m) {
		
		synchronized (this) {
			ms.clear();
			ms.add(m);
			updateList();
		}
	}

	public void endOfScan(Spotter sender) {
		synchronized (this) {
			measuring = false;
			notifyAll();
		}
	}

	LinkedList ms = new LinkedList();
	boolean measuring = false;

	public void updateList() {
		StringBuffer sb;
		sb = new StringBuffer("");
		if (ms.size() == 0) {
			sb.append("No Readings Yet");
		} else {
			ListIterator li = ms.listIterator();
			while (li.hasNext()) {
				Measurement m = (Measurement) li.next();
				sb.append("At time " + m.getTimestamp() + "\n");
				sb.append("\t"+m.toLogString());
			}
		}
		gpsStringItem.setText(sb.toString());
	}


	public void appendString(String s) {
		sb.append(s+"\n");
		gpsStringItem.setText(sb.toString());
	}

	public void commandAction(Command c, Displayable d) {
		//System.err.println("command action");
		//if(c == startCommand) {
		//	start();
		//}
	}

	public void spotterExceptionThrown(Spotter s,SpotterException ex) {
		ex.printStackTrace();
	}
}

