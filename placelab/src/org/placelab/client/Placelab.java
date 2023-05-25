/*
 * Created on 27-Aug-2004
 *
 */
package org.placelab.client;

import org.placelab.client.tracker.BeaconTracker;
import org.placelab.client.tracker.Estimate;
import org.placelab.client.tracker.EstimateListener;
import org.placelab.client.tracker.Tracker;
import org.placelab.collections.Iterator;
import org.placelab.collections.List;
import org.placelab.core.Measurement;
import org.placelab.mapper.Mapper;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.SpotterListener;

public class Placelab implements EstimateListener {

	protected List spotterList;
	protected BeaconTracker tracker;
	protected Mapper mapper;
	protected Estimate latestEstimate;

	protected Placelab() {
	}

	public Placelab(List spotterList, Mapper mapper, BeaconTracker tracker) {
		this.spotterList = spotterList;
		this.mapper = mapper;
		this.tracker = tracker;
	}

	public BeaconTracker getTracker() {
		return tracker;
	}
	
	public Mapper getMapper() {
		return mapper;
	}

	public void addEstimateListener(EstimateListener el) {
		if (tracker != null && el != null)
			tracker.addEstimateListener(el);
	}

	public void removeEstimateListener(EstimateListener el) {
		if (tracker != null && el != null)
			tracker.removeEstimateListener(el);
	}

	public synchronized void addSpotterListener(SpotterListener sl) {
		if (sl == null || spotterList == null)
			return;
		Iterator i = spotterList.iterator();
		while (i.hasNext()) {
			Spotter s = (Spotter) i.next();
			s.addListener(sl);
		}
	}

	public synchronized void removeSpotterListener(SpotterListener sl) {
		if (sl == null || spotterList == null)
			return;
		Iterator i = spotterList.iterator();
		while (i.hasNext()) {
			Spotter s = (Spotter) i.next();
			s.removeListener(sl);
		}
	}

	private boolean started = false;
	
	public synchronized void start() throws PlacelabException {
	    if(started) return;
		try {
		    started=true;
			if(mapper != null) {
			    if(!mapper.open()) throw new PlacelabException("Cannot start Mapper");
			}

			if (tracker != null) {
				tracker.reset();
				tracker.addEstimateListener(this);
			}
			if (spotterList != null) {
				Iterator i = spotterList.iterator();
				while (i.hasNext()) {
					Spotter s = (Spotter) i.next();
					if (tracker != null) {
						s.addListener(tracker);
					}
					s.open();
					s.startScanning();
				}
			}
		} catch (Throwable t) {
			throw new PlacelabException("Error during start: " + t);
		}
	}

	public synchronized void stop() throws PlacelabException {
	    if(!started) return;
		try {
			started=false;
			if(mapper != null) {
			    mapper.close();
			}
			if (tracker != null) {
				tracker.removeEstimateListener(this);
			}
			if (spotterList != null) {
				Iterator i = spotterList.iterator();
				while (i.hasNext()) {
				    try {
						Spotter s = (Spotter) i.next();
						if (tracker != null) {
							s.removeListener(tracker);
						}
						s.stopScanning();
						s.close();
				    } catch(SpotterException se) {				        
				    }
				}
			}
		} catch (Throwable t) {
			throw new PlacelabException("Error during stop: " + t);
		}
	}

	public Estimate getLatestEstimate() throws PlacelabException {
	    if(!started) throw new PlacelabException("getLatestEstimate: Placelab not started");
		try {
			if (tracker == null) return latestEstimate;
			tracker.updateWithoutMeasurement(tracker.getLastUpdatedTime());
			return latestEstimate;
		} catch (Exception e) {
			throw new PlacelabException("Error during getLatestEstimate: " + e);
		}
	}

	public void estimateUpdated(Tracker t, Estimate e, Measurement m) {
		latestEstimate = e;
	}
}