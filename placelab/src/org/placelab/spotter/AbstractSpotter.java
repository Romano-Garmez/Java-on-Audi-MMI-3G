package org.placelab.spotter;

import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.collections.ListIterator;
import org.placelab.core.Measurement;
import org.placelab.eventsystem.EventListener;
import org.placelab.eventsystem.EventSystem;

/**
 * Provides common functionality for sending notifications to
 * listeners and doing single scans.  Spotters which use
 * a stream based approach might choose to subclass this class,
 * but generally Spotters should subclass either SyncSpotter
 * or AsyncSpotter for the most convenience.
 * @see SyncSpotter
 * @see AsyncSpotter
 */
public abstract class AbstractSpotter implements Spotter {
	private LinkedList listeners;
	
	public AbstractSpotter() {
		listeners = new LinkedList();
	} 
	public void addListener(SpotterListener listener) {
		if (listener != null) {
			saveIterators(null);
			listeners.add(listener);
			resetIterators();
		}
	}
	public void removeListener(SpotterListener listener) {
		saveIterators(listener);
		listeners.remove(listener);
		resetIterators();
	}

	private static class ScanOnceImplementation implements SpotterListener {
		private AbstractSpotter spotter;
		private EventSystem evs;
		public ScanOnceImplementation(AbstractSpotter s, EventSystem e) {
			spotter = s;
			evs = e;
			
			spotter.addListener(this);
			if (evs == null) spotter.startScanning();
			else spotter.startScanning(evs);
		}
		public void gotMeasurement(Spotter s, Measurement m) {
			// we are done scanning
			spotter.stopScanning();
			spotter.removeListener(this);
			spotter.notifyEndOfScan(evs);
		}
		/* (non-Javadoc)
		 * @see org.placelab.spotter.SpotterListener#spotterExceptionThrown(org.placelab.spotter.SpotterException)
		 */
		public void spotterExceptionThrown(Spotter s,SpotterException ex) {
			ex.printStackTrace();
		}
	}
	public void scanOnce() {
		new ScanOnceImplementation(this, null);
	}
	public void scanOnce(EventSystem evs) {
		new ScanOnceImplementation(this, evs);
	}
	protected void notifyGotMeasurement(Measurement m) {
		Iterator it=null;
		try {
			it = listenerIterator();
		} catch (RuntimeException e) {
			throw new RuntimeException("notifyGotMeasurement.1:"+e.getMessage());
		}
		while (it.hasNext()) {
			SpotterListener listener=null;
			try {
				listener = (SpotterListener) it.next();
			} catch (RuntimeException e) {
				throw new RuntimeException("notifyGotMeasurement.2:"+e.getMessage());
			}
			try {
				listener.gotMeasurement(this, m);
			} catch (RuntimeException e) {
			    e.printStackTrace();
				throw new RuntimeException("notifyGotMeasurement.3:"+listener.getClass().getName()+":"+e.getMessage());
			}
		}
		try {
			disposeIterator(it);
		} catch (RuntimeException e) {
			throw new RuntimeException("notifyGotMeasurement.4:"+e.getMessage());
		}
	}
	protected void notifyGotException(SpotterException ex) {
		Iterator it = listenerIterator(); 
		while (it.hasNext()) {
			SpotterListener listener = (SpotterListener) it.next();
			listener.spotterExceptionThrown(this, ex);
		}
		disposeIterator(it);
	}
	protected void notifyGotMeasurement(EventSystem evs, Measurement m) {
		if (evs==null) {
			try {
				notifyGotMeasurement(m);
			} catch (RuntimeException e){
				throw new RuntimeException("notifyGotMeasurement(EVS).1:"+e.getMessage());
			}
		} else {
			evs.notifyTransientEvent(new EventListener() {
				public void callback(Object eventType, Object data) {
					notifyGotMeasurement((Measurement)data);
				}
			}, m);
		}
	}
	protected void notifyGotException(EventSystem evs, SpotterException ex) {
		if (evs==null) {
			notifyGotException(ex);
		} else {
			evs.notifyTransientEvent(new EventListener() {
				public void callback(Object eventType, Object data) {
					notifyGotException((SpotterException)data);
				}
			}, ex);
		}
	}
	protected void notifyEndOfScan() {
		Iterator it = listenerIterator(); 
		while (it.hasNext()) {
			SpotterListener listener = (SpotterListener) it.next();
			if (listener instanceof ScanOnceListener) {
				((ScanOnceListener)listener).endOfScan(this);
			}
		}
		disposeIterator(it);
	}
	protected void notifyEndOfScan(EventSystem evs) {
		if (evs==null) {
			notifyEndOfScan();
		} else {
			evs.notifyTransientEvent(new EventListener() {
				public void callback(Object eventType, Object data) {
					notifyEndOfScan();
				}
			}, null);
		}
	}
	
	public void waitForThread(Thread t) {
		if (t==Thread.currentThread()) return;
		boolean done = false;
		while (!done) {
			try {
				t.join();
				done = true;
			} catch (InterruptedException e) {
			}
		}
	}
	
	public abstract void startScanning();
	public abstract void startScanning(EventSystem evs);
	public abstract void stopScanning();
		
	private class ListenerIterator implements Iterator {
		ListIterator it;
		int savedIndex;
		
		public ListenerIterator() {
			it = listeners.listIterator(0);
		}
		public boolean hasNext() {
			return it.hasNext();
		}
		public Object next() {
			return it.next();
		}
		public void remove() {
			it.remove();
		}
		public void save(int deleteIndex) {
			savedIndex = it.nextIndex();
			if (deleteIndex >= 0 && deleteIndex < savedIndex) {
				savedIndex--;
			}
		}
		public void reset() {
			it = listeners.listIterator(savedIndex);
		}
	}
	private LinkedList iterators = new LinkedList();
	private Iterator listenerIterator() {
		Iterator it = new ListenerIterator();
		iterators.add(it);
		return it;
	}
	private void disposeIterator(Iterator it) {
		iterators.remove(it);
	}
	private void saveIterators(SpotterListener delete) {
		int deleteIndex = (delete != null ? listeners.indexOf(delete) : -1);
		for (Iterator it = iterators.iterator(); it.hasNext(); ) {
			ListenerIterator li = (ListenerIterator) it.next();
			li.save(deleteIndex);
		}
	}
	private void resetIterators() {
		for (Iterator it = iterators.iterator(); it.hasNext(); ) {
			ListenerIterator li = (ListenerIterator) it.next();
			li.reset();
		}
	}
}
