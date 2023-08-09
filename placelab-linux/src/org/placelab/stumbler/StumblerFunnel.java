package org.placelab.stumbler;

import java.util.Hashtable;

import org.placelab.collections.HashSet;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.core.Measurement;
import org.placelab.core.ShutdownListener;
import org.placelab.eventsystem.EventListener;
import org.placelab.eventsystem.EventSystem;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.SpotterListener;

/**
 * StumblerFunnel manages and collates Measurements from multiple Spotters.  
 * 
 * The StumblerFunnel, with the use of {@link SpotterExtension} objects, can be configured to 
 * allow for virtually any type of stumbling semantics.  Examples of real and
 * potential uses are given below.
 * <p>
 * <b>Trigger, Independent, and Dependent Spotters<b><br>
 * <ul>
 * <li><b>Trigger Spotters</b>: These are spotters that are run asynchronously
 * on the user or spotter defined scan interval
 * (see {@link PeriodicScannable#setPeriodicScanInterval(long)}).  When they update, they cause
 * all Dependent Spotters to be queried for their latest updates (whether this
 * triggers an actual scan of the environment or just gets the latest update
 * from the dependent spotter depends on the configuration of the Spotter Extension
 * for that dependent spotter). When the StumblerFunnel is started, all Trigger 
 * Spotters will have their startScanning() methods called, so they should not be
 * started prior to providing them to the StumblerFunnel.
 * 
 * <li><b>Independent Spotters</b>: These spotters also run asynchronously on the
 * user or spotter defined scan interval, but they do not trigger any other
 * spotters to update upon the StumblerFunnel's receipt of a Measurement from them.
 * When the StumblerFunnel is started, all Independent Spotters will have their 
 * startScanning() methods called.
 * 
 * <li><b>Dependent Spotters</b>: These spotters only have their Measurements
 * collected when Trigger Spotters return Measurements, or when the timeout fires 
 * (see below).  They can be either
 * synchronous or asynchronous since they will have their Measurements collected
 * with {@link SpotterExtension#getLatestMeasurement()} which should return quickly.
 * Note that the StumblerFunnel does not start Dependent Spotters, so if you intend
 * to have them run asynchronously you will have to use {@link SpotterExtension#startScanning()}
 * yourself.
 * 
 * </ul>
 * <br>
 * <b>Timeout</b><br>
 * If you specify a positive timeout, if the registered Trigger spotters do not produce
 * Measurements by the timeout period (or if there are no Trigger spotters) all Dependent
 * Spotters will have their latest Measurements collected and pushed through to
 * StumblerFunnelUpdateListeners at that time.  Negative timeouts will make it such
 * that a Trigger Spotter must update in order for Measurements to be collected from
 * any Dependent spotters.
 * 
 * <p>
 * 
 * <b>Examples of StumblerFunnel configurations</b><br>
 * "Straight-line stumbler": This type of Stumbler doesn't have any dependent spotters.
 * This is if you just want updates to come in as the spotters make them available.
 * <code>
 * StumblerFunnel f = new StumblerFunnel(-1);
 * WiFiSpotter wifi = new WiFiSpotter(250); // wifi will produce updates every 250 ms
 * NMEAGPSSpotter gps = SerialGPSSpotter.newSpotter(); // gps will produce updates as fast as the hardware allows
 * SpotterExtension wifiExt = new SpotterExtension(wifi, true, -1);
 * SpotterExtension gpsExt = new SpotterExtension(gps, false, SpotterExtension.GPS_STALE_TIME);
 * f.addIndependentSpotter(wifiExt);
 * f.addIndependentSpotter(gpsExt);
 * f.addUpdateListener(new LogWriter());
 * f.start();
 * </code>
 * <p>
 * "Place Lab Stumbler": This is (a simplification) of the default config used by PlacelabStumbler.
 * The idea is to place WiFi as accurately as possible, so WiFi should be collected when GPS Measurements
 * come in.  Running WiFi fast and independent of the GPS as above will probably see more APs, but
 * it won't produce logs that are as accurate as this method.
 * <code>
 * StumblerFunnel f = new StumblerFunnel(-1); // you might specify 2000 or something here to get something when GPS isn't active
 * WiFiSpotter wifi = new WiFiSpotter();
 * NMEAGPSSpotter gps = SerialGPSSpotter.newSpotter();
 * SpotterExtension wifiExt = new SpotterExtension(wifi, true, -1);
 * SpotterExtension gpsExt = new SpotterExtension(gps, false, SpotterExtension.GPS_STALE_TIME);
 * f.addDependentSpotter(wifiExt);
 * f.addTriggerSpotter(gpsExt);
 * f.addUpdateListener(new AudioNotifier());
 * f.start();
 * </code>
 * <br>
 *  
 * Listeners to StumblerFunnel will get notifications containing the measurements for
 * the StumblerSpotters as they are available according to the rules outlined above
 * The StumblerFunnel begins its funneling operations after start() is called and
 * runs these in a new thread.  This means that stumblerUpdated notifications will
 * not come back in the same thread as which start() was called.  If this won't work
 * for you, you can use start(EventSystem evs) and callbacks will be delivered by
 * the given EventSystem.
 * 
 * Finally, note that the update Hashtable is keyed by the Spotters, not the SpotterExtensions.
 */
public class StumblerFunnel extends Thread implements SpotterListener {
    
    protected HashSet dependentSpotters;
    protected HashSet triggerSpotters;
    protected HashSet independentSpotters;
    protected HashSet shutdowns;
    protected HashSet updateListeners;
    protected boolean shuttingDown = false;
    protected boolean suspend = false;
    protected boolean doUpdate = false;
    
    protected EventSystem eventSystem;

    protected Timeout timeout;

    protected volatile boolean timeoutFired = false;
    
    // a queue of Spotters
    protected LinkedList updateSenders = null;
    // a queue of Measurements
    protected LinkedList updateSenderMeasurements = null;
    
    /** Create a StumblerFunnel with default timeout, as set
     *  in placelab.StumblerFunnel.timeout system property or
     *  2000 ms if not set
     */
    public StumblerFunnel() {
        this(getDefaultTimeout());
    }
    
    private static long getDefaultTimeout() {
        long timeout = 2000;
        try {
            timeout = Long.parseLong(
                    System.getProperty("placelab.StumblerFunnel.timeout"));
        } catch (Exception e) {
            
        }
        return timeout;
    }
    
    
    /**
     * Create a new StumblerFunnel.  If no trigger spotters have signaled that
     * they have new Measurements available by timeout, then all dependent stumblers are polled
     * for their Measurements.
     */
    public StumblerFunnel(long timeout) {
        triggerSpotters = new HashSet();
        independentSpotters = new HashSet();
        dependentSpotters = new HashSet();
        updateListeners = new HashSet();
        updateSenderMeasurements = new LinkedList();
        updateSenders = new LinkedList();
        shutdowns = new HashSet();
        this.timeout = new Timeout(this, timeout);
    }
    
    public void start(EventSystem evs) {
        this.eventSystem = evs;
        this.start();
    }
    
    public void run() {
        this.startTriggerSpotters();
        this.startIndependentSpotters();
        timeout.start();
		while (!shuttingDown) {
			if (suspend) {
				synchronized (this) {
					try {
						sleep(1000);
					} catch (InterruptedException ie) {
					}
				}
			} else {
				if (doUpdate) {
					doUpdate = false;
					this.pulse();
				}
				synchronized (this) {
					try {
						this.wait();
					} catch (InterruptedException ie) {
					}
				}
			}
		}
	}
    
    
    protected void startTriggerSpotters() {
        Iterator i = triggerSpotters.iterator();
        while(i.hasNext()) {
            Spotter sp = (Spotter)i.next();
            try {
				sp.open();
			} catch (SpotterException e1) {
				e1.printStackTrace();
			}
            sp.startScanning();
        }
    }
    protected void startIndependentSpotters() {
        Iterator i = independentSpotters.iterator();
        while(i.hasNext()) {
            Spotter sp = (Spotter)i.next();
            try {
				sp.open();
			} catch (SpotterException e1) {
				e1.printStackTrace();
			}
            sp.startScanning();
        }
    }
    
    /**
     * Adds a spotter to be watched by the StumblerFunnel.  All SpotterExtensions are
     * also ShutdownListeners, so they are automatically added to the shutdownListeners list too
     * @param spotter to be added to the StumblerFunnel
     */
    public void addDependentSpotter(SpotterExtension spotter) {
        checkRunning();
        dependentSpotters.add(spotter);
        shutdowns.add(spotter);
    }
    
    /**
     * Adds a spotter to be watched by the StumblerFunnel.  All SpotterExtensions are
     * also ShutdownListeners, so they are automatically added to the shutdownListeners list too.
     * A Trigger Spotter is one that the StumblerFunnel will always fire an update in response
     * to an update from.
     * @param spotter to be added to the StumblerFunnel
     */
    public void addTriggerSpotter(SpotterExtension spotter) {
        checkRunning();
        triggerSpotters.add(spotter.getSpotter());
        shutdowns.add(spotter);
        spotter.addListener(this);
    }
    
    /**
     * Adds a spotter to be watched by the StumblerFunnel.  Independent Spotters are spotters
     * for which you want to collect all Measurements they can produce, but for which their updates
     * don't trigger any further response.
     */
    public void addIndependentSpotter(SpotterExtension spotter) {
        checkRunning();
        independentSpotters.add(spotter.getSpotter());
        shutdowns.add(spotter);
        spotter.addListener(this);
    }
    
    /**
     * Remove a SpotterExtension of any type from the StumblerFunnel
     * @param remove the spotter (and) extension to be removed
     */
    public void removeSpotter(SpotterExtension remove) {
        checkRunning();
        triggerSpotters.remove(remove);
        independentSpotters.remove(remove);
        dependentSpotters.remove(remove);
        shutdowns.remove(remove);
    }
    
    private void checkRunning() {
        if(this.isAlive() && !suspend) {
            throw new IllegalStateException("You can only modify the state of the StumblerFunnel when it is stopped or suspended");
        }
    }
    
    /**
     * Register to be notified after the pulse with the latest Measurements from all the spotters
     */
    public void addUpdateListener(StumblerFunnelUpdateListener sul) {
        checkRunning();
        updateListeners.add(sul);
    }
    
    /**
     * Register to be notified when the StumblerFunnel has been signaled to shutdown.
     */
    public void addShutdownListener(ShutdownListener listener) {
        checkRunning();
        shutdowns.add(listener);
    }
    
    public void removeUpdateListener(StumblerFunnelUpdateListener sul) {
        checkRunning();
        updateListeners.remove(sul);
    }
   
    public void removeShutdownListener(ShutdownListener listener) {
        checkRunning();
        shutdowns.remove(listener);
    }
    
    long lastPulse = System.currentTimeMillis();
    
    protected void pulse() {
        if(suspend) return;
        long now = System.currentTimeMillis();
        long diff = now - lastPulse;
        lastPulse = now;
        //Logger.println("Time since last pulse: " + diff, Logger.MEDIUM);
    	if(timeoutFired) {
    	    long start = System.currentTimeMillis();
    	    timeoutFired = false;
    		Hashtable response = new Hashtable();
	        Iterator i = dependentSpotters.iterator();
	        while(i.hasNext()) {
	        	SpotterExtension sp = (SpotterExtension)i.next();
	            Measurement mps;
				try {
					mps = sp.getLatestMeasurement();
				    if(mps != null) response.put(sp.getSpotter(), mps);
				} catch (SpotterException e1) {
					e1.printStackTrace();
				}
	        }
	        //System.out.println("Time to collect dependent: " + (System.currentTimeMillis() - start));
    	    start = System.currentTimeMillis();
	        this.notifyUpdate(response);
    	    //System.out.println("Time to notify: " + (System.currentTimeMillis() - start));
    	} else {
	    	while(updateSenders.size() > 0) {
	    		Spotter updateSender = null;
		        synchronized(updateSenders) {
		        	updateSender = (Spotter)updateSenders.removeFirst();
		        }
		        Measurement updateSenderMeasurement = null;
		        synchronized(updateSenderMeasurements) {
		        	updateSenderMeasurement = 
		        		(Measurement)updateSenderMeasurements.removeFirst();
		        }
	    		Hashtable response = new Hashtable();
		        boolean trigger = false;
		        trigger = triggerSpotters.contains(updateSender);
		        if(trigger) {
		            Iterator i = dependentSpotters.iterator();
			        while(i.hasNext()) {
			        	SpotterExtension sp = (SpotterExtension)i.next();
			            Measurement mps;
						try {
							mps = sp.getLatestMeasurement();
							if(mps != null) response.put(sp.getSpotter(), mps);
						} catch (SpotterException e1) {
							e1.printStackTrace();
						}
			        }
		        }
		        response.put(updateSender, updateSenderMeasurement);
		        notifyUpdate(response);
	    	}
    	}
    }
    
    protected void notifyUpdate(final Hashtable response) {
        for(Iterator it = updateListeners.iterator();it.hasNext();) {
            final StumblerFunnelUpdateListener l = ((StumblerFunnelUpdateListener)it.next());
            if(eventSystem != null) {
	            eventSystem.notifyTransientEvent(new EventListener() {
	                public void callback(Object eventType, Object data) {
	                    l.stumblerUpdated(response);
	                }
	            }, null);
            } else {
                l.stumblerUpdated(response);
            }
        }
    }
    
    public void gotMeasurement(Spotter sender, Measurement measurement) {
        if(triggerSpotters.contains(sender)) timeout.reset();
        
        // drop all measurements while suspended.
        if (suspend)
        		return;
        
        synchronized(updateSenders) { updateSenders.add(sender); }
        synchronized(updateSenderMeasurements) { updateSenderMeasurements.add(measurement); }
        synchronized(this) {
            doUpdate = true;
            this.notify();
        }
    }
    
    public void suspendListen() {
    	suspend = true;
    }
    
    public boolean isSuspended () {
    		return suspend;
    }
    
    public void resumeListen() {
    	suspend = false;
    }
    
    public void shutdown() {
        shuttingDown = true;
        timeout.shutdown();
        synchronized(shutdowns) {
	        Iterator i = shutdowns.iterator();
	        while(i.hasNext()) {
	        	Object elem = i.next();
	        	if (elem instanceof Spotter) {
	        		Spotter s = (Spotter) elem;
	        		s.stopScanning();
	        		try {
						s.close();
					} catch (SpotterException e1) {
						e1.printStackTrace();
					}
	        	}
	        	if (elem instanceof ShutdownListener) ((ShutdownListener)elem).shutdown();
	        }
        }
    }
    
    protected class Timeout extends Thread {
		long timeout_in_ms;
		Object syncObject;
		boolean timeoutPleaseDie = false;
		boolean rollover = false;
		public Timeout(Object syncObject, long timeout_in_ms) {
			this.timeout_in_ms = timeout_in_ms;
			this.syncObject = syncObject;
		}
		public void run() {
		    if(timeout_in_ms < 0) return;
			while ( timeoutPleaseDie == false ) {
			    synchronized(this) {
				    try {
				        this.wait(timeout_in_ms); 
				    } catch (InterruptedException e) {
				        continue;
				    }
			    }
				if(rollover) {
				    rollover = false;
				    continue;
				}
				synchronized(syncObject) {
					timeoutFired = true;
				    doUpdate = true;
					syncObject.notify();
				}
			}
			timeoutPleaseDie = false;
		}
		public void setNewTimeout(int newtimeout_in_ms) {
			timeout_in_ms = newtimeout_in_ms;
		}
		public long getTimeout() {
			return timeout_in_ms;
		}
		public void shutdown() {
			timeoutPleaseDie = true;
		}
		public void reset() {
		    rollover = true;
		    synchronized(this) {
		        this.notify();
		    }
		}
	}
    
	/* (non-Javadoc)
	 * @see org.placelab.spotter.SpotterListener#spotterExceptionThrown(org.placelab.spotter.SpotterException)
	 */
	public void spotterExceptionThrown(Spotter s,SpotterException ex) {
		// TODO Auto-generated method stub
		
	}
    
}
