/*
 * Created on Jul 21, 2004
 *
 */
package org.placelab.spotter;

import org.placelab.collections.UnsupportedOperationException;
import org.placelab.core.Measurement;
import org.placelab.eventsystem.EventListener;
import org.placelab.eventsystem.EventSystem;

/**
 * This convenience class provides the groundwork for Spotters whose
 * implementations are most naturally synchronous.  An example of such
 * a Spotter is {@link WiFiSpotter}.  Subclasses provide their own
 * blocking getMeasurementImpl and the SyncSpotter uses that to offer
 * up the asynchronous spotter interface for users of the Spotter.
 */
public abstract class SyncSpotter extends AbstractSpotter {
	private ScanThread scanThread=null;
	private EventSystem eventSystem=null;
	private Object evsTimerToken=null;

	/** @return the interval that the spotter should wait before invoking the next scan */
	protected abstract long nextScanInterval();
	/**
	 *  Collects and returns a single Measurement.
	 */
	protected abstract Measurement getMeasurementImpl() throws SpotterException;
	public Measurement getMeasurement() throws SpotterException {
		if (scanThread != null || evsTimerToken != null) {
			throw new UnsupportedOperationException("you cannot invoke getMeasurement() while a continuous scan operation is in progress");
		}
		Measurement m;
		try {
		    m=getMeasurementImpl();
		    if(m != null) notifyGotMeasurement(m);
		} catch(SpotterException se) {
		    notifyGotException(se);
		    throw se;
		}
		return m;
	}
	public void startScanning() {
		scanUsingBackgroundThread(null);
	}
	public void startScanning(EventSystem evs) {
		scanUsingEventSystem(evs);
	}
	public void stopScanning() {
		try {
			if (scanThread != null) {
				/* notify the scanning thread that we are stopping */
				scanThread.cancel();
				/* wait for the scanning thread to stop */
				waitForThread(scanThread);
				scanThread = null;
			}
			if (evsTimerToken != null) {
				eventSystem.removeTimer(evsTimerToken);
				eventSystem = null;
				evsTimerToken = null;
			}
		} catch (RuntimeException t) {
			RuntimeException wrapper=new RuntimeException("stopScanning.4:"+t.getClass().getName());
			throw wrapper;
		}
			
	}
	protected void scanUsingBackgroundThread(EventSystem evs) {
		try {
			if (scanThread != null || evsTimerToken != null) {
				throw new UnsupportedOperationException("a continuous scan operation is already in progress");
			}
			eventSystem = evs;
			scanThread = new ScanThread(evs);
			scanThread.start();
		} catch (RuntimeException t) {
			RuntimeException wrapper=new RuntimeException("scanUsingBackgroundThread.2:"+t.getClass().getName());
			throw wrapper;
		}
	}
	protected void scanUsingEventSystem(EventSystem evs) {
		try {
			if (scanThread != null || evsTimerToken != null) {
				throw new UnsupportedOperationException("a continuous scan operation is already in progress");
			}
			
			eventSystem = evs;
			doOneEventSystemScan();
		} catch (RuntimeException t) {
			RuntimeException wrapper=new RuntimeException("scanUsingEventSystem.3:"+t.getClass().getName());
			throw wrapper;
		}

	}
	
	private void doOneEventSystemScan() {
		try {
			Measurement m = null;
			try {
				m = getMeasurementImpl();
			} catch (SpotterException ex) {
				notifyGotException(eventSystem, ex);
			}
			long millis = nextScanInterval();
			notifyGotMeasurement(eventSystem, m);
			if (millis == Integer.MIN_VALUE) {
				evsTimerToken = null; 
				return;
			} else {
				evsTimerToken = eventSystem.addTimer(millis, new EventListener() {
					public void callback(Object eventType, Object data) {
						EventSystem evs = (EventSystem) data;
						doOneEventSystemScan();
					}
				}, null);
			}
		} catch (RuntimeException t) {
			RuntimeException wrapper=new RuntimeException("DoOneEventSystemScan.1:"+t.getClass().getName());
			throw wrapper;
		}
	}
	public void errorInBackgroundThreadAndThreadDeath(Throwable t) {
		// FOR NOW WE DO NOTHING
	}
	
	private class ScanThread extends Thread {
		private boolean done=false;
		private EventSystem evs;
		
		public ScanThread(EventSystem evs) {
			this.evs = evs;
		}
		public void cancel() {
			done = true;
		}
		public void run() {
			try {
				while (!done) {
					Measurement m = null;
					try {
						m = getMeasurementImpl();
						notifyGotMeasurement(evs, m);
					} catch (SpotterException ex) {
						notifyGotException(evs, ex);
					}
					long nextScan = System.currentTimeMillis() + nextScanInterval();
					try {
					    final long SHORT_TIME=100;
					    while(!done && System.currentTimeMillis() < nextScan) Thread.sleep(SHORT_TIME);
					} catch (InterruptedException e) {
						// ignore this exception
					}
				}
			} catch (Throwable t) {
				errorInBackgroundThreadAndThreadDeath(t);
			}
		}
	}
	
}
