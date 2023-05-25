/*
 * Created on Jun 23, 2004
 */
package org.placelab.midp.debug;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.midlet.MIDlet;

import org.placelab.collections.LinkedList;
import org.placelab.collections.ListIterator;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.Coordinate;
import org.placelab.core.Measurement;
import org.placelab.mapper.Beacon;
import org.placelab.midp.GSMReading;
import org.placelab.midp.GSMSpotter;
import org.placelab.midp.RMSMapper;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.SpotterListener;

/**
 * 
 */

public class GSMSpotterMidlet extends MIDlet implements Runnable,CommandListener,SpotterListener{
	
	private static Display display;
	private boolean alive;
	private boolean measuring;
	private Form f;
	private StringItem si;
	private Command exitCommand = new Command("Exit", Command.EXIT, 1);
	private Command startCommand = new Command("Get GSM Data", Command.ITEM, 1);
	private RMSMapper mapper;
	
	protected GSMSpotter spotter;
	protected LinkedList readingList;
	
	public GSMSpotterMidlet() {
		display = Display.getDisplay(this);
		f = new Form("GSM Spotter");
		si = new StringItem("Placelab GSM Spotter ", " ");
		f.append(si);
		f.addCommand(exitCommand);
		f.addCommand(startCommand);
		f.setCommandListener(this);
		display.setCurrent(f);
		readingList = new LinkedList();
		mapper = new RMSMapper();
	}
	
	public void startApp() {
		alive = true;
		Thread t = new Thread(this);
	    t.start();
	}
	
	public void pauseApp() {
		alive = true;
	}
	
	public void destroyApp(boolean condition) {
		alive = false;
	}
	
	
	public void run() {
		mapper.open();
		
		spotter = new GSMSpotter();
		try {
		    spotter.open();
		} catch(SpotterException e) {
		    si.setText("Cannot open link with native spotter");
		}
		spotter.addListener(this);
		
		while (alive) {
            readingList.clear();
            measuring=true;
            spotter.startScanning();

            while(measuring) {
                try {
                    synchronized(this) {
                        wait(10*1000);
                    }
                } catch (InterruptedException e) {
                }
            }
        }

		spotter.removeListener(this);
		try {
		    spotter.close();
		} catch(SpotterException e) {
		    si.setText("Cannot close link with native spotter");
		}	
		mapper.close();
	}	

	
	 public void commandAction(Command c, Displayable s) {
	 	if(c == startCommand) {
	 		run();
	 	} else if (c == exitCommand) {
	         destroyApp(true);
	         notifyDestroyed();
	      } 
	 }

	/* (non-Javadoc)
	 * @see org.placelab.spotter.SpotterListener#gotMeasurement(org.placelab.core.Measurement)
	 */
	public void gotMeasurement(Spotter sender, Measurement m) {
	      synchronized(this) {
            readingList.add(m);
            updateList();

            /* the measurement is over */
            sender.stopScanning();
            measuring=false;
            notifyAll();
	      }
	}

	private void updateList() {
        StringBuffer sb;
        sb = new StringBuffer("");
        if (readingList.size() == 0) {
            sb.append("No cell detected");
        } else {
            ListIterator li = readingList.listIterator();
            while (li.hasNext()) {
                BeaconMeasurement m = (BeaconMeasurement) li.next();
                sb.append("At time " + m.getTimestamp() + "\n");
                for (int i = 0; i < m.numberOfReadings(); i++) {
                    GSMReading gr = (GSMReading) m.getReading(i);
                    sb.append(gr.toString());
                    Beacon b = mapper.findBeacon(gr.getId());
                   System.err.println(gr.getId()+" "+b);
                    if(b != null) {
                    	Coordinate c = b.getPosition();
                    	sb.append("  Lat: "+c.getLatitudeAsString()+"\n  Lon: "+c.getLongitudeAsString()+"\n");
                    }
                }
            }
        }
        si.setText(sb.toString());
    }

	public void spotterExceptionThrown(Spotter s,SpotterException ex) {
		ex.printStackTrace();
	}
}

