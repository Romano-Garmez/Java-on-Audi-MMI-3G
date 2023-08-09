package org.placelab.spotter;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.BeaconReading;
import org.placelab.core.BluetoothReading;
import org.placelab.core.Measurement;

public class ListenerBluetoothGPSSpotter extends BluetoothGPSSpotter implements SpotterListener {
	private static long SMALL_AMOUNT_OF_TIME=500; /* milliseconds */
	private long LARGE_AMOUNT_OF_TIME; /* milliseconds */
	private static long TOO_OLD_TO_CHECK = 20000;
	
	// when not doing my own scanning for bt gps devices
	// i get updates from some external controller (PlacelabStumbler
	// most likely) that contain bluetooth readings.  I try to
	// establish connections to likely ones until I succeed.  Failures
	// go on the blacklist until such time as candidatesToSearch is empty
	// and then the blacklist will be searched again.
	// (i use LinkedList here to save space on the phones)
	private LinkedList candidatesToSearch;
	private LinkedList candidatesSearched;
	
	// a helper class to store the bluetooth readings for likely
	// candidates for bt gps devices
	private class CandidatePackage {
		public long timestamp;
		public BluetoothReading reading;
		public CandidatePackage(long timestamp,
				BluetoothReading reading) {
			this.timestamp = timestamp;
			this.reading = reading;
		}
	}	
	
	public ListenerBluetoothGPSSpotter(BluetoothSpotter bt) {
	    LARGE_AMOUNT_OF_TIME = 30 * 1000;
	    bt.addListener(this);
	}
	
	public void open() {
	    super.open();
		candidatesToSearch = new LinkedList();
		candidatesSearched = new LinkedList();
	}
	
	public void close() {
	    super.close();
	    candidatesToSearch = null;
	    candidatesSearched = null;
	}
	
	public void gotMeasurement(Spotter sp, Measurement m) {
		if(serviceUrl != null) return;
		boolean doNotify = false;
		BeaconReading[] teeth = ((BeaconMeasurement)m).getReadings();
		synchronized(candidatesToSearch) {
			for(int i = 0; i < teeth.length; i++) {
				if(!(teeth[i] instanceof BluetoothReading)) {
					continue;
//					throw new IllegalArgumentException("InquiryBluetoothGPSSpotter can " +
//							"only listen to a BluetoothSpotter.");
				}
				BluetoothReading tooth = (BluetoothReading)teeth[i];
				if(tooth.getHumanReadableName().toLowerCase().indexOf("gps") >= 0) {
					// i was asked to do it this way, because the reasoning is
					// that there are only ever a few gps devices around, but the memory
					// savings of using a LinkedList outweighs the processing savings
					// of using a HashMap.
					boolean found = false;
					Iterator it = candidatesSearched.iterator();
					while(it.hasNext()) {
						CandidatePackage cp = (CandidatePackage)it.next();
						if(cp.reading.getId().equals(tooth.getId())) {
							// update the timestamp because when I run out of
							// readings in candidatesToSearch, I turn over 
							// candidatesSearched into candidatesToSearch
							// and try them all again, supposing they aren't stale
							// that way if a connection failed for some funky reason
							// you aren't hosed forever.
							cp.timestamp = m.getTimestamp();
							found = true;
							break;
						}
					}
					if(!found) {
						// search candidatesToSearch and see if there is already
						// an entry for this one
						found = false;
						it = candidatesToSearch.iterator();
						while(it.hasNext()) {
							CandidatePackage cp = (CandidatePackage)it.next();
							if(cp.reading.getId().equals(tooth.getId())) {
								cp.timestamp = m.getTimestamp();
								found = true;
								break;
							}
						}
						if(!found) {
							CandidatePackage cp = new CandidatePackage(m.getTimestamp(),
									tooth);
							candidatesToSearch.add(cp);
						}
					}
				}
			}
			if(candidatesToSearch.size() > 0) doNotify = true;
		}
		if(doNotify) {
			synchronized(this) {
				this.notify();
			}
		}
	}
	public void spotterExceptionThrown(Spotter sp, SpotterException se) {
		// i'm sure not the one to handle this
	}
	
	
	protected void findGPS() {
	    LinkedList notChecked = new LinkedList();
		// make a temp copy of candidatesToSearch for our thread here
		// i can't just synchronize on the whole thing because the 
		// bluetooth connection stuff is quite slow and i don't want to
		// block in gotMeasurement
		synchronized(candidatesToSearch) {
			Iterator i = candidatesToSearch.iterator();
			while(i.hasNext()) {
				notChecked.add(i.next());
			}
		}
		Iterator i = notChecked.iterator();
		while(i.hasNext()) {
			CandidatePackage cp = (CandidatePackage)i.next();
			if((System.currentTimeMillis() -
					cp.timestamp) > TOO_OLD_TO_CHECK) {
				synchronized(candidatesToSearch) {
					candidatesToSearch.remove(cp);
				}
				continue;
			} else {
				synchronized(candidatesToSearch) {
					candidatesToSearch.remove(cp);
					// no need to check if candidatesSearched
					// contains cp because it doesn't.
					candidatesSearched.add(cp);
				}
			}
			System.out.println("trying to connect to " + cp.reading.getHumanReadableName());
			// fabricate the whole thing
			String fabUrl = 
				"btspp://" // this means bt serial port service 
				+ cp.reading.getId()
				+ ":1"  // bt gps uses 1 as the RFCOMM channel
				+ ";authenticate=false"  // NOAUTHENTICATE_NOENCRYPT
				+ ";encrypt=false";
			try {
				state = "Attempting to connect to " + cp.reading.getHumanReadableName()
					+ " " + cp.reading.getId();
				System.out.println("state: " + state);
				conn = (StreamConnection)Connector.open(fabUrl);
				is = conn.openDataInputStream();
			} catch (Exception e) {
				state = "Failed connection attempt to " + cp.reading.getHumanReadableName();
				System.out.println(state);
				serviceUrl = null;
				continue;
			}
			// if i get here, then the connection succeeded
			serviceUrl = fabUrl;
			state = "Connected " + cp.reading.getHumanReadableName();
			break;
		}
		if (serviceUrl == null) {//this happens when there are no gps devices
			try {
				state = "Sleeping until suitable devices are found";
				//System.err.println("sleeping...");
				long currentMillis = 0;
				while(currentMillis < LARGE_AMOUNT_OF_TIME) {
					synchronized(candidatesToSearch) {
						if(candidatesToSearch.size() > 0) break;
					}
					if(scanThread.isDone()) {
						break;
					}
					synchronized(this) {
						this.wait(SMALL_AMOUNT_OF_TIME);
					}
					currentMillis += SMALL_AMOUNT_OF_TIME;
				}
				synchronized(candidatesToSearch) {
					if(candidatesToSearch.size() == 0) {
						// then its time to turn over the ones that
						// have already been checked.  note that even
						// if TOO_OLD_TO_CHECK is less than LARGE_AMOUNT_OF_TIME
						// this still works because gotMeasurement updates
						// the timestamps on CandidatePackages in 
						// candidatesSearched.
						this.candidatesToSearch.addAll(candidatesSearched);
						this.candidatesSearched.clear();
					}
				}
				state = "Trying Again";
			} catch(InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}
	
}
