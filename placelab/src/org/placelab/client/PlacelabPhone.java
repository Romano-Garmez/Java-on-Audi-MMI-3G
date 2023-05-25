/*
 * Created on 30-Aug-2004
 *
 */
package org.placelab.client;

import org.placelab.client.tracker.IntersectionTracker;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.midp.EventLogger;
import org.placelab.midp.GSMSpotter;
import org.placelab.midp.RMSMapper;
import org.placelab.spotter.BluetoothSpotter;

/**
 * PlacelabPhone encodes a good set of defaults for phones, i.e.
 * Bluetooth and GSM Spotters, RMS Mapper, and Intersection Tracker.
 */
public class PlacelabPhone extends Placelab {

    public PlacelabPhone() {
        this(true);
    }
    
    public PlacelabPhone(boolean useTracker) {
        spotterList = new LinkedList();
        BluetoothSpotter bt=
        	new BluetoothSpotter(10000,true) {
        		public void errorInBackgroundThreadAndThreadDeath(Throwable t) {
        			EventLogger.logError("BT spotter: background thread:"+
        					t.getClass().getName()+":"+t.getMessage());
        		}
        };
        GSMSpotter gsm=
        	new GSMSpotter() {
        		public void errorInBackgroundThreadAndThreadDeath(Throwable t) {
        			EventLogger.logError("GSM spotter: background thread:"+
        					t.getClass().getName()+":"+t.getMessage());
        		}
        };	
        spotterList.add(bt);
        spotterList.add(gsm);
        if(useTracker) {
            mapper = new RMSMapper();
            tracker = new IntersectionTracker(mapper);
        }
    }

    public Iterator getEstimateBeacons() {
    	return ((IntersectionTracker) tracker).getLociBeacons();
    }
}
