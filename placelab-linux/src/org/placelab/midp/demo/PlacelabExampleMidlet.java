/*
 * Created on Jun 23, 2004
 */
package org.placelab.midp.demo;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;
import javax.microedition.midlet.MIDlet;

import org.placelab.client.Placelab;
import org.placelab.client.PlacelabException;
import org.placelab.client.PlacelabPhone;
import org.placelab.client.tracker.Estimate;
import org.placelab.client.tracker.EstimateListener;
import org.placelab.client.tracker.Tracker;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.Measurement;
import org.placelab.mapper.Beacon;
import org.placelab.midp.GSMSpotter;
import org.placelab.midp.RMSMapper;
import org.placelab.spotter.BluetoothSpotter;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.SpotterListener;

/**
 * This is an example MIDlet that uses the Place Lab daemon to obtain
 * position estimates. The demo shows current GSM cell information,
 * all nearby bluetooth devices, and a position estimate if available.
 * 
 * As required by Place Lab, the map database needs to be populated with
 * known beacons that can be seen in order to generate position estimates.
 *  
 */

public class PlacelabExampleMidlet extends MIDlet implements CommandListener,SpotterListener,EstimateListener,Runnable{
	
	private static Display display;
	private Form f;
	private StringItem latlon;
	private StringItem gsm;
	private StringItem bluetooth;
	
	private Command exitCommand = new Command("Exit", Command.EXIT, 1);
	Placelab daemon = null;
	RMSMapper mapper;
	
	public PlacelabExampleMidlet() {
		display = Display.getDisplay(this);
		f = new Form("PlacelabTest");
		latlon = new StringItem("Latitude/Longitude", "");
		gsm = new StringItem("GSM Spotter","");
		bluetooth = new StringItem("Bluetooth Spotter","");
		f.append(latlon);
		f.append(new Spacer(1000, 10));
		f.append(gsm);
		f.append(new Spacer(1000, 10));
		f.append(bluetooth);
		f.addCommand(exitCommand);
		f.setCommandListener(this);
		display.setCurrent(f);
	}
	
	public void startApp() {
		new Thread(this).start();
		
	}
	
	public void run() {
		if(daemon == null) daemon = new PlacelabPhone();

		mapper = (RMSMapper) daemon.getMapper();
		daemon.addEstimateListener(this);
		daemon.addSpotterListener(this);
		latlon.setText("Waiting...");
		gsm.setText("Waiting...");
		bluetooth.setText("Waiting...");
		try {
		    daemon.start();
		} catch(PlacelabException se) {
		    latlon.setText("Exception starting daemon: " + se.toString());
		}

//		while(alive);
	}
	
	public void pauseApp() {
	    destroyApp(false);
	}
	
	public void destroyApp(boolean condition) {
	    try {
	        daemon.stop();
	    } catch(PlacelabException se) {
		    latlon.setText(se.getMessage());
	    }
	    daemon.removeEstimateListener(this);
	    daemon.removeSpotterListener(this);
	}
	
	
	 public void commandAction(Command c, Displayable s) {
	 	if (c == exitCommand) {
	         destroyApp(true);
	         notifyDestroyed();
	    } 
	 }

	public void gotMeasurement(Spotter sender, Measurement m) {
	    if(sender == null || m == null) return;
	    String mapString = "";
	    if(m instanceof BeaconMeasurement) {
	        try {
	            BeaconMeasurement bm = (BeaconMeasurement) m;
	            if(daemon.getMapper() == null) {
	                mapString = " (MapperError)";
	            } else if(bm.numberOfReadings() > 0 && bm.getReading(0) != null) {
	                Beacon b = daemon.getMapper().findBeacon(bm.getReading(0).getId());
	                if(b != null) {
	                    if(b.getPosition().isNull()) mapString = " (Null)"; else mapString = " (Known: " + b.getPosition().toString() + ")";
	                }
	            }
	        } catch(Exception e) {
	            mapString = " exception while mapping: " + e;
	        }
        }
	     if(sender instanceof BluetoothSpotter) {
	         bluetooth.setText(m.toShortString() + mapString);
	     } else if(sender instanceof GSMSpotter) {
	         gsm.setText(m.toShortString() + mapString);
	     } else {
	     
	     }
	}

	public void estimateUpdated(Tracker t, Estimate e, Measurement m) {
	    if(t == null || e == null) return;

	    if(e.getCoord() == null && mapper.numBeacons() != 0) {
	    	latlon.setText("This area has no known beacons. Use the Stumbler MIDlet to stumble for beacons.");
	    } else if(mapper.numBeacons() == 0) {
	    	latlon.setText("No beacons in mapper. Use the Stumbler MIDlet to load map data.");
	    } else {
	    	latlon.setText(e.toString());
	    }
	}
	
	public void spotterExceptionThrown(Spotter s,SpotterException ex) {
	    if(s == null) return;
	    if(s instanceof GSMSpotter) {
	        gsm.setText("Exception!: " + ex);
	    } else if(s instanceof BluetoothSpotter) {
	        bluetooth.setText("Exception!: " + ex);
	    }
	}
}

/*
LinkedList l = new LinkedList();
l.add(new FakeSpotter());
Mapper m = new RMSMapper();
daemon = new Placelab(l,m, new IntersectionTracker(m));


if(uniqueId.startsWith("3")) {
    GSMBeacon b = new GSMBeacon();
    b.setPosition(new FixedTwoDCoordinate("15.0","15.0"));
    b.setAreaId("52025");
    b.setCellId("9163");
    b.setMCC("310");
    b.setMNC("380");
    LinkedList l = new LinkedList();
    l.add(b);
    return l;
}
*/