package org.placelab.stumbler.gui;

import org.placelab.collections.LinkedList;
import org.placelab.core.BeaconReading;
import org.placelab.core.TwoDCoordinate;
import org.placelab.mapper.Mapper;

public class BeaconHistory {

    public LinkedList readings;
    public BeaconHistoryHelper highestRssi = null;
    public long mostRecent = 0;
    // no point in keeping more than the last 25 in the table
    public static final int MAX_READINGS = 25;
    
    public BeaconHistory() {
        readings = new LinkedList();
    }
    
    public long getMostRecent() { return mostRecent; }
    
    public void addReading(BeaconReading reading, TwoDCoordinate gpsLL, long timestamp) {
        addReading(new BeaconHistoryHelper(reading, gpsLL, timestamp));
    }
    
    public void addReading(BeaconHistoryHelper newReading) {
        if(highestRssi == null) {
            highestRssi = newReading;
        } else{
            if(!this.highestRssi.getId().equals(
                    newReading.getId())) {
                throw new IllegalArgumentException("newReading hasn't the same bssid as the readings in this BeaconHistoryReading");
            } else {
                if(readingIsHigherThanHighest(newReading)) highestRssi = newReading;
                if(readings.size() > MAX_READINGS) {
                	// delete the oldest one first
                	readings.removeLast();
                }
                readings.add(newReading);
            }
        }
        if(mostRecent < newReading.timestamp) mostRecent = newReading.timestamp;
    }
    
    public boolean readingIsHigherThanHighest(BeaconHistoryHelper h) {
        if(highestRssi == null) return true;
        else {
        	if(highestRssi.hasGPS()) {
        		if(h.hasGPS() && h.timestamp > highestRssi.timestamp) return true;
        		else return false;
        	} else {
        		if(h.timestamp > highestRssi.timestamp) return true;
        		else return false;
        	}
        }
        /*else if(h.hasGPS() && !highestRssi.hasGPS()
        		|| !highestRssi.hasGPS()) return true;
        else if(h.hasGPS() && highestRssi.hasGPS() && 
                (h.reading.getNormalizedSignalStrength() >= highestRssi.reading.getNormalizedSignalStrength())) return true;
        else return false;*/
        
    }
    
    public static class BeaconHistoryHelper {
        public BeaconReading reading;
        public long timestamp;
        public TwoDCoordinate gpsLL;
        public BeaconHistoryHelper(BeaconReading reading, TwoDCoordinate gpsLL, long timestamp) {
            this.reading = reading;
            this.timestamp = timestamp;
            this.gpsLL = gpsLL;
        }
        public boolean isNew(Mapper inMapper) {
            return (inMapper.findBeacon(reading.getId()) == null);
        }
        public boolean hasGPS() {
            if(gpsLL == null) return false;
            if(gpsLL.getLatitude() != 0.0 || gpsLL.getLongitude() != 0.0) return true;
            else return false;
        }
        public String getId() {
            return reading.getId();
        }
    }
    
    public int hashCode() {
        if(highestRssi == null) return 0;
        return highestRssi.reading.getId().hashCode();
    }
    
}
