package org.placelab.jsr0179;

import javax.microedition.location.Coordinates;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.ProximityListener;

import org.placelab.client.tracker.BeaconTracker;
import org.placelab.client.tracker.CentroidTracker;
import org.placelab.client.tracker.Estimate;
import org.placelab.client.tracker.TwoDPositionEstimate;
import org.placelab.client.tracker.EstimateListener;
import org.placelab.client.tracker.Tracker;
import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.collections.List;
import org.placelab.core.Measurement;
import org.placelab.core.TwoDCoordinate;
import org.placelab.mapper.CompoundMapper;
import org.placelab.mapper.Mapper;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.WiFiSpotter;

public class LocationProviderImpl extends LocationProvider implements EstimateListener {

	/** Default timeout when getting a location, in seconds. */
	protected static final int DEFAULT_TIMEOUT = 5;
	
	/** Default polling interval used for LocationListener's */
	protected static final int DEFAULT_INTERVAL = 10;
	
	/** The max age an estimate can be before it is considered stale */
	protected static final int DEFAULT_MAXAGE = 2;
	
	private Mapper mapper;
	private BeaconTracker tracker;
	private List spotters;
	
	private Estimate lastEstimate;
	private long lastEstimateTimestamp = 0;
	
	private volatile boolean reset = false;
	
	private LocationNotifier locationNotifier = null;
	private ProximityNotifier proximityNotifier = null;	
	
	public LocationProviderImpl () throws LocationException {
		
		spotters = new LinkedList();
		spotters.add(new WiFiSpotter());
		
		mapper = CompoundMapper.createDefaultMapper(true, true);
		
		tracker = new CentroidTracker(mapper);
		tracker.addEstimateListener(this);
		
		for (Iterator i = spotters.iterator(); i.hasNext(); ) {
			Spotter s = (Spotter) i.next();
			try {
				s.open();
				s.addListener(tracker);
				s.startScanning();
			} catch (SpotterException e) {
				throw new LocationException("Unable to load spotter: " + e.toString());
			}
		}
		
	}
	
	
	public int getState() {
		
		if (lastEstimate == null)
			return OUT_OF_SERVICE;
		
		if (System.currentTimeMillis() - lastEstimateTimestamp >= 30000)
			return OUT_OF_SERVICE;	// if we haven't any estimates in the past N seconds
		else if (((TwoDPositionEstimate)lastEstimate).getTwoDPosition().getLatitude() == Double.NaN) {
			return TEMPORARILY_UNAVAILABLE; 	// if we're getting measurements but no Lat/Lon data
		} else {
			return AVAILABLE;
		}
	
	}

	/**
	 * Performs a synchronous request for location.
	 */
	public Location getLocation(int timeout) throws LocationException,
			InterruptedException {
	
		if ( (timeout == 0) || (timeout < -1) )
			throw new IllegalArgumentException("timeout == 0 || timeout < -1");
		
		if (timeout == -1) 
			timeout = DEFAULT_TIMEOUT;
		
		if (getState() == LocationProvider.OUT_OF_SERVICE)
			throw new LocationException("out of service.");
		
		long timestamp = lastEstimateTimestamp;
		
		synchronized(this) {
			this.wait(timeout*1000);
		}
		
		if (reset) {
			reset = false;
			throw new InterruptedException("reset");
		}
		
		if (timestamp == lastEstimateTimestamp)
			throw new LocationException("timed out getting locaiton.");
		
		return new LocationImpl(lastEstimate);
	}
	
	public Location getLastKnownLocationImpl () {
		if (lastEstimate == null) 
			return null;
		
		return new LocationImpl(lastEstimate);
	}

	public void setLocationListener(LocationListener listener, int interval,
			int timeout, int maxAge) {
		
		if (locationNotifier != null)
			locationNotifier.cancel();
				
		if (interval == -1)
			interval = DEFAULT_INTERVAL;
		
		if (timeout == -1)
			timeout = DEFAULT_TIMEOUT;
		
		if (maxAge == -1)
			maxAge = DEFAULT_MAXAGE;
		
		locationNotifier = new LocationNotifier(listener, interval*1000, timeout*1000, maxAge*1000);
		locationNotifier.start();
	}

	public void reset() {
		reset = true;
		synchronized(this) {
			this.notify();
		}
	}
	
	public void addProximityListenerImpl(ProximityListener listener, Coordinates coordinates, float proximityRadius) throws LocationException {
		
		if (getState() == OUT_OF_SERVICE) 
			listener.monitoringStateChanged(false);
		
		if (proximityNotifier == null) {
			proximityNotifier = new ProximityNotifier();
			proximityNotifier.start();
		}
		
		proximityNotifier.addListener(listener, coordinates, proximityRadius);
	}
	
	public void removeProximityListenerImpl(ProximityListener listener) {
		if (proximityNotifier != null)
			proximityNotifier.removeListener(listener);
	}

	public void estimateUpdated(Tracker t, Estimate e, Measurement m) {
		lastEstimate = e;
		lastEstimateTimestamp = e.getTimestamp();
		
		synchronized (this) {
			this.notify();	// wait is in getLocation
		}
		
		// We have to do our notifications and distance calcs in a separate thread.
		if (proximityNotifier != null) {
			synchronized (proximityNotifier) {
				proximityNotifier.notify();
			}
		}
		
	}
	
	class LocationNotifier extends Thread {
		
		private volatile boolean cancel = false;
		
		private LocationListener listener;
		private int intervalMillis;
		private int timeoutMillis;
		private int maxAgeMillis;
		
		public LocationNotifier (LocationListener listener, int intervalMillis, int timeoutMillis, int maxAgeMillis) {
			this.listener = listener;
			this.intervalMillis = intervalMillis;
			this.timeoutMillis = timeoutMillis;
			this.maxAgeMillis = maxAgeMillis;
		}
		
		public void cancel () {
			cancel = true;
			
			// tap on the thread to wake up
			synchronized (this) {
				this.notify();
			}
			
			// wait for it to finish
			try {
				this.join();
			} catch (InterruptedException e) {}
		}
		
		public void run () {
			
			int lastState = getState();
			
			for (;!cancel;) {
				long start = System.currentTimeMillis();
				
				if (getState() != lastState) {
					listener.providerStateChanged(LocationProviderImpl.this, getState());
					lastState = getState();
				}
				
				Location loc;
				if (System.currentTimeMillis() <= lastEstimateTimestamp + maxAgeMillis) {
					loc = new LocationImpl(lastEstimate);
				} else {
					try {
						loc = getLocation((intervalMillis+timeoutMillis)/1000);
					} catch (Exception e) {
						loc = new LocationImpl(null, "timeout");
					}
				}
				
				listener.locationUpdated(LocationProviderImpl.this, loc);
				
				long timeToAcquire = System.currentTimeMillis() - start;
				
				if (intervalMillis - timeToAcquire > 0) {
					try {
						synchronized (this) {
							this.wait(intervalMillis-timeToAcquire);
						}
					} catch (InterruptedException e) {
						
					}
				}
			}
			
		}
	}
	
	class ProximityNotifier extends Thread {
		private HashMap map = new HashMap();
		private volatile boolean cancel = false;
		
		public synchronized void addListener (ProximityListener listener, Coordinates coordinates, float proximityRadius) {
			LinkedList l;
			if (map.containsKey(listener)) {
				l = (LinkedList) map.get(listener);
			} else {
				l = new LinkedList();
				map.put(listener, l);
			}
			
			l.add(new ProximityListenerSpec(coordinates, proximityRadius));
		}
		
		public synchronized void removeListener (ProximityListener listener) {
			map.remove(listener);
		}
		
		public void run () {
			for (;!cancel;) {
				synchronized (this) {
					try {
						this.wait();
					} catch (InterruptedException e) {}
				}
	 			
				// notify our listeners
				
				if (map.size() == 0)
					continue;
				
				TwoDCoordinate c = (TwoDCoordinate) ((TwoDPositionEstimate)LocationProviderImpl.this.lastEstimate).getTwoDPosition();
				Coordinates here = new Coordinates(c.getLatitude(), c.getLongitude(), 0F);
				
				for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
					ProximityListener listener = (ProximityListener) i.next();
					LinkedList l = (LinkedList) map.get(listener);
					
					for (Iterator n = l.iterator(); n.hasNext(); ) {
						ProximityListenerSpec spec = (ProximityListenerSpec) n.next();
						
						if (here.distance(spec.coordinates) < spec.radius)
							listener.proximityEvent(spec.coordinates, new LocationImpl(LocationProviderImpl.this.lastEstimate));
						
					}
				}
			}
		}
		
		public void cancel () {
			cancel = true;
			
			// tap on the thread to wake up
			synchronized (this) {
				this.notify();
			}
			
			// wait for it to finish
			try {
				this.join();
			} catch (InterruptedException e) {}
		}
		
		class ProximityListenerSpec {
			
			public ProximityListenerSpec (Coordinates coordinates, float radius) {
				this.coordinates = coordinates;
				this.radius = radius;
			}
			
			public Coordinates coordinates;
			public float radius;
		}
	}
}

