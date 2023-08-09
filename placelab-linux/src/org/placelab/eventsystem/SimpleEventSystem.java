package org.placelab.eventsystem;

import java.util.Comparator;

import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.util.ns1.DHeap;

/*
 * Created on Aug 10, 2004
 *
 */

/**
 * 
 *
 */
public class SimpleEventSystem implements EventSystem {
	private DHeap timers;
	private HashMap eventListeners;
	private int runCount=0;
	
	private class QueueEventComparator implements Comparator {
	    public int compare(Object o1, Object o2) {
	        QueueEvent qe1 = (QueueEvent)o1;
	        QueueEvent qe2 = (QueueEvent)o2;
	        if(qe1.timeout > qe2.timeout) return 1;
	        else if(qe1.timeout == qe2.timeout) return 0;
	        else return -1;
	    }
	}
	
	private class QueueEvent {
		public QueueEvent(long t, EventListener l, Object d) {
			timeout = t;
			listener = l;
			data = d;
		}
		public long timeout;
		public EventListener listener;
		public Object data;
	}
	
	public SimpleEventSystem() {
		timers = new DHeap(new QueueEventComparator(), 
		        4, 256);
		eventListeners = new HashMap();
		runCount = 0;
	}
	public void run() {
		QueueEvent event=null;
		int keepGoing;
		synchronized(timers) { keepGoing = ++runCount; }
		while (true) {
			synchronized(timers) {
				if (runCount < keepGoing) break;
				while (timers.isEmpty()) {
					try { timers.wait(); } catch (InterruptedException e) {}
				}
				if (runCount < keepGoing) break;
				
				long now = System.currentTimeMillis();
				event = (QueueEvent) timers.findMin();
				if (event.timeout <= now) {
					timers.deleteMin();
				} else {
					long interval = event.timeout - now;
					event = null;
					try { timers.wait(interval); } catch (InterruptedException e) {}
					if (runCount < keepGoing) break;
				}
			}
			
			if (event != null) {
				// process event
				event.listener.callback(null, event.data);
			}
		}
	}

	public void stop() {
		synchronized(timers) {
			timers.makeEmpty();
			runCount--;
		}
	}

	public Object addTimer(long timeoutMillis, EventListener listener,
			Object data) {
		QueueEvent event = new QueueEvent(System.currentTimeMillis()+timeoutMillis, listener, data);
		synchronized(timers) {
			timers.insert(event);
			timers.notify();
		}
		return event;
	}

	public void removeTimer(Object token) {
		QueueEvent event = (QueueEvent) token;
		synchronized(timers) {
			timers.remove(event);
		}
	}

	public Object addEventListener(Object eventType, EventListener listener) {
		LinkedList listeners = (LinkedList)eventListeners.get(eventType);
		if (listeners == null) {
			listeners = new LinkedList();
			eventListeners.put(eventType, listeners);
		}
		listeners.add(listener);
		Object []rv = new Object[2];
		rv[0] = eventType;
		rv[1] = listener;
		return rv;
	}
	public void removeEventListener(Object token) {
		Object[] arr = (Object[]) token;
		Object eventType = arr[0];
		Object listener = arr[1];
		
		LinkedList listeners = (LinkedList) eventListeners.get(eventType);
		if (listeners != null) listeners.remove(listener);
	}

	public void notifyEvent(final Object eventType, final Object data) {
		addTimer(0, new EventListener() {
			public void callback(Object dummy, Object d) {
				doNotifyEvent(eventType, data);
			}
		}, null);
	}
	private void doNotifyEvent(Object eventType, Object data) {
		LinkedList listeners = (LinkedList) eventListeners.get(eventType);
		if (listeners == null) return;
		for (Iterator it=listeners.iterator(); it.hasNext(); ) {
			EventListener listener = (EventListener) it.next();
			listener.callback(eventType, data);
		}
	}

	public void notifyTransientEvent(EventListener listener, Object data) {
		addTimer(0, listener, data);
	}
}
