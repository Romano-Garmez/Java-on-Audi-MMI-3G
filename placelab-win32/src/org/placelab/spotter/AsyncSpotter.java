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
 * This convenience class provides all the groundwork for Spotters
 * whose implementations are most naturally asynchronous.  Subclasses
 * provide their own startOneScan and cancelScan implementations, and
 * AsyncSpotter then builds the continuous scanning methods based on
 * those.
 */
public abstract class AsyncSpotter extends AbstractSpotter {
	private ScanThread scanThread=null;
	private EventSystem eventSystem=null;
	private Object evsTimerToken=null;
	private boolean scanInProgress=false;
	private boolean scanningOnce=false;
	
	/**
	 *  @return the interval that the spotter should wait before invoking the next scan 
	 */
	protected abstract long nextScanInterval();

	/**
	 * This performs a single scan of the environment that should call 
	 * {@link AbstractSpotter#notifyGotMeasurement(EventSystem, Measurement)} upon
	 * collecting the Measurement.
	 * @param evs the EventSystem to pass through to notifyGotMeasurement
	 */
	protected abstract void startOneScan(EventSystem evs);
	/**
	 * Stops the scan that was started in {@link #startOneScan(EventSystem)}
	 */
	protected abstract void cancelScan();
	/**
	 * Collect Measurements with a blocking implementation and return them.
	 */
	protected abstract Measurement getMeasurementImpl() throws SpotterException;
	
	public Measurement getMeasurement() throws SpotterException {
		if (scanThread != null || evsTimerToken != null) {
			throw new SpotterException("you cannot invoke getMeasurement() while a continuous scan operation is in progress");
		}
		return getMeasurementImpl();
	}
	public void scanOnce() {
		scanningOnce = true;
		startScanning();
	}
	public void scanOnce(EventSystem evs) {
		scanningOnce = true;
		startScanning(evs);
	}
	public void startScanning() {
		scanUsingBackgroundThread(null);
	}
	public void startScanning(EventSystem evs) {
		scanUsingEventSystem(evs);
	}
	public void stopScanning() {
		if (scanInProgress) {
			cancelScan();
			scanInProgress = false;
		}
		if (scanThread != null) {
			scanThread.cancel();
			scanThread = null;
		}
		if (evsTimerToken != null) {
			eventSystem.removeTimer(evsTimerToken);
			evsTimerToken = null;
		}
		eventSystem = null;
		scanningOnce = false;
	}
	protected void scanUsingBackgroundThread(EventSystem evs) {
		if (scanThread != null || evsTimerToken != null) {
			throw new UnsupportedOperationException("a continuous scan operation is already in progress");
		}
		eventSystem = evs;
		scanThread = new ScanThread(evs);
		scanThread.start();
	}
	protected void scanUsingEventSystem(EventSystem evs) {
		if (scanThread != null || evsTimerToken != null) {
			throw new UnsupportedOperationException("a continuous scan operation is already in progress");
		}

		eventSystem = evs;
		doOneEventSystemScan();
	}
	
	private void doOneEventSystemScan() {
		scanInProgress = true;
		startOneScan(eventSystem);
	}
	
	protected void scanDone() {
		if (scanThread==null && eventSystem==null) return;
		
		if (scanThread == null) {
			eventSystem.notifyTransientEvent(new EventListener() {
				public void callback(Object eventType, Object data) {
					scanDoneImpl();
				}
			}, null);
		} else {
			scanDoneImpl();
		}
	}
	private void scanDoneImpl() {
		if (scanningOnce) {
			scanningOnce = false;
			EventSystem evs=eventSystem;
			stopScanning();
			notifyEndOfScan(evs);
		}
		scanInProgress = false;
		long millis = nextScanInterval();
		if (scanThread == null) {
			if (eventSystem != null) {
				evsTimerToken = eventSystem.addTimer(millis, new EventListener() {
					public void callback(Object eventType, Object data) {
						doOneEventSystemScan();
					}
				}, null);
			}
		} else {
			synchronized(scanThread) {
				scanThread.notify();
			}
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
		public synchronized void cancel() {
			done = true;
		}
		public synchronized boolean isDone() { return done; }
		public void run() {
			try {
				while (true) {
					if (isDone()) return;
					
					startOneScan(evs);
					boolean waiting = true;
					while (waiting) {
						try {
							synchronized(this) {
								this.wait();
							}
							waiting = false;
						} catch (InterruptedException e1) {
						}
					}
					if (isDone()) return;
					
					long millis = nextScanInterval();
					if (millis == Integer.MIN_VALUE) break;
					
					try {
						if (millis > 0) Thread.sleep(millis);
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
