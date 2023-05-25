/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.core;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.placelab.collections.Iterator;
import org.placelab.collections.UnsupportedOperationException;
import org.placelab.midp.GSMReading;

/**
 * BeaconMeasurements contain all the BeaconReadings seen
 * during a single scan of the environment for Beacons.
 */
public class BeaconMeasurement extends Measurement {
	protected BeaconReading[] readings=null;
	protected int numReadings=0;
	
	// this flag should be only in the constructor (or maybe the first
	// call to addReading()), otherwise alternating this flag during
	// addReading is confusing
	private boolean mergeDuplicateReadings =  true;
	
	public BeaconMeasurement(long timestamp) {
		super(timestamp);
	}
	
	/**
	 * @param timestamp the time at which the scan for BeaconReadings began
	 * @param mergeDuplicateReadings merge BeaconReadings that have the same unique id
	 */
	public BeaconMeasurement(long timestamp, boolean mergeDuplicateReadings) {
		super(timestamp);
		this.mergeDuplicateReadings = mergeDuplicateReadings;
	}
	
	/**
	 * @param timestamp the time at which the scan for BeaconReadings began
	 * @param readings the set of BeaconReadings to start with
	 */
	public BeaconMeasurement(long timestamp, BeaconReading[] readings) {
		super(timestamp);
		addReadings(readings);
	}
	
	private void growArrayIfNeeded(int numNeeded) {
		int size = (readings==null ? 0 : readings.length);
		if (numReadings + numNeeded <= size) return;
		
		while (numReadings + numNeeded >= size) {
			if (size==0) size = 4;
			size = size * 2;
		}
		BeaconReading[] newReadings = new BeaconReading[size];
		if (readings != null && readings.length > 0) {
			System.arraycopy(readings, 0, newReadings, 0, readings.length);
		}
		readings = newReadings;
	}
	
	private int numOfDuplicateReadings = 0;
	private int numOfDuplicateReadingsMerged = 0;
	
	/**
	 * Add a BeaconReading to this BeaconMeasurement
	 * @param r the BeaconReading to add
	 * @return whether or not a BeaconReading with the same unique id as <code>r</code>
	 * already existed in this BeaconMeasurement
	 */
	public boolean addReading(BeaconReading r) {
		growArrayIfNeeded(1);
		
		// brute force? average case is 1-2 iters
		for(int i = 0; i < numReadings; i++) {
			BeaconReading ri = readings[i];

			if (! ri.getId().equals(r.getId()))
				continue;
			
			// a duplicate reading is found			
			if (mergeDuplicateReadings) {
				// pick the strongest ss
				if (ri.getNormalizedSignalStrength() < 
					r.getNormalizedSignalStrength()) {
					readings[i] = r;
				}
				++numOfDuplicateReadingsMerged;
				return false;
			} else {
				++numOfDuplicateReadings;
				break;
			}
		}
		
		readings[numReadings] = r;
		++numReadings;
		return true;
	}

	/**
	 * Batch load the given readings
	 */
	public void addReadings(BeaconReading[] readings) {
		if (readings == null)
			return;
		growArrayIfNeeded(readings.length);
		for (int i=0; i < readings.length; i++) {
			//readings[numReadings++] = readings[i];
			addReading(readings[i]);
		}
	}

	/**
	 * Gets all the readings for this BeaconReading.  Returns a copy
	 * so that changes to the returned array do not affect the
	 * BeaconMeasurement
	 */
	public BeaconReading[] getReadings() {
		if (numReadings==0 || readings==null) {
			return null;
		}
		if (numReadings==readings.length) {
			return readings;
		}
		
		/* create a copy of the array if the array size is not the
		 * same as the actual number of elements in the array
		 */
		BeaconReading[] retval = new BeaconReading[numReadings];
		System.arraycopy(readings, 0, retval, 0, numReadings);
		return retval;
	}
	
	public int numberOfReadings() { 
		return (readings==null ? 0 : numReadings);
	}
	
	public BeaconReading getReading(int index) {
		if (index < 0 || index >= numberOfReadings()) return null;
		return readings[index];
	}
	
	/*
	 * XXX: if mergeDuplicateReading is false, then this is only first match
	 */
	public BeaconReading getReadingById(String id) {
		for(int i = 0; i < numberOfReadings(); i++) {
			if (readings[i].getId().equals(id))
				return readings[i];
		}
		return null;
	}
	
	public String toLogString() {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < this.numberOfReadings(); i++) {
            sb.append(Types.TYPE+"="+this.getReading(i).getType()+"|"+Types.TIME+"=" + getTimestamp() + "|" + this.getReading(i).toLogString());
            if(i != (this.numberOfReadings() - 1)) sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }

    public String toShortString() {
    	StringBuffer sb = new StringBuffer();
    	for(int i = 0; i < this.numberOfReadings(); i++) {
            sb.append("" + this.getReading(i).toShortString());
            if(i != (this.numberOfReadings() - 1)) sb.append(System.getProperty("line.separator"));
        }
    	return sb.toString();
    }
    
    
    public byte[] toCompressedBytes() {
    	// XXX: this only works as long as all BeaconReadings in the BeaconMeasurement are of the same type
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeUTF(getReading(0).getType());
			dos.writeLong(getTimestamp());
	    	for(int i=0; i < this.numberOfReadings();i++) {
	    		this.getReading(i).toCompressedBytes(dos);
	    	}    	
	    	dos.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
			return null;
		}
    	return baos.toByteArray();
    }
    
    public BeaconMeasurement(String type, long time, DataInputStream dis) throws IOException {
    	super(time);

		try {
			while(dis.available() > 0) {
				BeaconReading r = null;
				if (type.equals(Types.WIFI)) {
					r = new WiFiReading(dis);
				} else if (type.equals(Types.BLUETOOTH)) {
					r = new BluetoothReading(dis);
				} else if (type.equals(Types.GSM)) {
					r = new GSMReading(dis);
				}
				addReading(r);
			}
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
    }

	

	public Iterator iterator() { return new BRIterator(); }
	private class BRIterator implements Iterator {
		int i=0;
		public boolean hasNext() {
			return (i < numberOfReadings());
		}

		public Object next() {
			if (!hasNext()) return null;
			return getReading(i++);
		}

		public void remove() {
			throw new UnsupportedOperationException("cannot remove a BeaconReading from a BeaconMeasurement");
		}		
	}

}
