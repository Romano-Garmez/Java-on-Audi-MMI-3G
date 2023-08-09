package org.placelab.midp.debug;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.placelab.collections.LinkedList;
import org.placelab.collections.ListIterator;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.BluetoothReading;
import org.placelab.core.Coordinate;
import org.placelab.core.Measurement;
import org.placelab.mapper.Beacon;
import org.placelab.midp.RMSMapper;
import org.placelab.spotter.BluetoothSpotter;
import org.placelab.spotter.ScanOnceListener;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;

/**
 * Bluetooth spotter midlet that does continuous inquiries and returns a list of all
 * seen bluetooth devices. If the device is a known beacon, it's coordinates
 * are returned as well.
 */
public class BluetoothSpotterMidlet extends MIDlet implements Runnable,
        ScanOnceListener {

    protected Form form;
    protected boolean alive;
    protected StringItem beaconListStringItem;
    protected BluetoothSpotter bs;
    protected RMSMapper mapper;
    
    public BluetoothSpotterMidlet() {
        super();
        mapper = new RMSMapper();
    }

    protected void startApp() throws MIDletStateChangeException {
        Display d = Display.getDisplay(this);
        d.setCurrent(getForm());
        alive = true;
        //run();
        new Thread(this).start();
    }

    protected void pauseApp() {
        alive = false;
    }

    protected void destroyApp(boolean flag) throws MIDletStateChangeException {
        alive = false;
    }

    protected Form getForm() {
        if (form == null) {
            form = new Form("Bluetooth Spotter");
            form.append(getBeaconListStringItem());
        }
        return form;
    }

    public StringItem getBeaconListStringItem() {
        if (beaconListStringItem == null) {
            beaconListStringItem = new StringItem("Bluetooth Beacons", "");
        }
        return beaconListStringItem;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        bs = new BluetoothSpotter();
        try {
            bs.open();
        } catch (SpotterException se) {
            // should never happen on anything that can
            // run this program
            System.exit(1);
        }
        bs.addListener(this);
        
        while (alive) {
            ms.clear();
            measuring=true;
            bs.scanOnce();
            while(measuring) {
                try {
                    synchronized(this) {
                        wait(3000);
                    }
                } catch (InterruptedException e) {
                }
            }
        }
        bs.removeListener(this);
        bs.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.placelab.spotter.SpotterListener#gotMeasurement(org.placelab.core.Measurement)
     */
    public void gotMeasurement(Spotter sender, Measurement m) {
        synchronized(this) {
            ms.add(m);
            updateList();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.placelab.spotter.SpotterListener#endOfMeasurements()
     */
    public void endOfScan(Spotter sender) {
        synchronized(this) {
            measuring=false;
            notifyAll();
        }
    }

    LinkedList ms = new LinkedList();
    boolean measuring = false;

    public void updateList() {
        StringBuffer sb;
        sb = new StringBuffer("");
        if (ms.size() == 0) {
            sb.append("No beacons visible.");
        } else {
            ListIterator li = ms.listIterator();
            while (li.hasNext()) {
                BeaconMeasurement m = (BeaconMeasurement) li.next();
           //     sb.append("At time " + m.getTimestamp() + "\n");
                for (int i = 0; i < m.numberOfReadings(); i++) {
                    BluetoothReading br = (BluetoothReading) m.getReading(i);
                    //sb.append(br.details()+"\n");
                    sb.append(br.getId()+" " + br.getHumanReadableName() + "\n");
                    Beacon b = mapper.findBeacon(br.getId());
                    if(b != null) {
                    	Coordinate c = b.getPosition();
                    	sb.append("  Lat: "+c.getLatitudeAsString()+"\n  Lon: "+c.getLongitudeAsString()+"\n");
                    }
                    //br.getbluetoothAddress + " ("
                    // + br.humanReadableName + ")\n
                    // " +
                    // bm.majorDeviceClass+"["+bm.minorDeviceClass+"],"+bm.serviceClass+"\n"
                    // );
                }
            }
        }
        sb.append("Found: " + ms.size() + " devices\n");
        beaconListStringItem.setText(sb.toString());
    }

	public void spotterExceptionThrown(Spotter s,SpotterException ex) {
		ex.printStackTrace();
	}

}