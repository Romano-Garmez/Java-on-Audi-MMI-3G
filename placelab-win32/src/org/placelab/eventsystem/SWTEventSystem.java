/*
 * Created on Jul 16, 2004
 *
 */
package org.placelab.eventsystem;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;


/**
 * An EventSystem for use in SWT applications.  This facilitates
 * working with background Spotters in applications that need to
 * have the messages integrated with the SWT event loop.
 * 
 *
 */
public class SWTEventSystem implements EventSystem {
	private Display display;
	private Shell shell;
	private int runCount=0;
	private HashMap eventListeners;

	/**
	 * Create a new SWTEventSystem for use with the given
	 * SWT Display and Shell.  The {@link #run()} method will
	 * still need to be invoked to start the event loop.
	 */
	public SWTEventSystem(Display display, Shell shell) {
		this.display = display;
		this.shell = shell;
		this.runCount = 0;
		this.eventListeners = new HashMap();
	}
	
	/**
	 * Starts listening for and dispatching messages and
	 * running the swt event loop for the display and shell
	 * given in the constructor.  All EventListener callbacks
	 * will be delivered in the SWT ui thread.
	 */
	public void run() {
		int keepGoing = ++runCount;
		while (runCount >= keepGoing && !shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				if (runCount >= keepGoing && !shell.isDisposed()) {
					display.sleep();
				}
			}
		}
	}

	public void stop() {
		display.asyncExec(new Runnable() {
			public void run() {
				runCount--;
			}
		});
	}

	private static class TimerRunnable implements Runnable {
		private EventListener listener;
		private Object data;
		private boolean cancelled;
		public TimerRunnable(EventListener listener, Object data) {
			this.listener = listener;
			this.data = data;
			this.cancelled = false;
		}
		public void run() {
			if (!cancelled) {
			    listener.callback(null, data);
			}
		}
		public void cancel() {
			cancelled = true;
		}
	}
	/* timer events */
	public Object addTimer(long timeoutMillis, EventListener listener, Object data) {
		TimerRunnable token = new TimerRunnable(listener, data);
		display.timerExec((int)timeoutMillis, token);
		return token;
	}
	public void removeTimer(Object token) {
		((TimerRunnable)token).cancel();
	}
	
	/* user-defined events that the EventSystem thread can listen for */
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
	
	/* use this method to create user-defined event notifications */
	public void notifyEvent(final Object eventType, final Object data) {
		display.asyncExec(new Runnable() {
			public void run() {
				doNotifyEvent(eventType, data);
			}
		});
	}
	
	private void doNotifyEvent(Object eventType, Object data) {
		LinkedList listeners = (LinkedList) eventListeners.get(eventType);
		if (listeners == null) return;
		for (Iterator it=listeners.iterator(); it.hasNext(); ) {
			EventListener listener = (EventListener) it.next();
			listener.callback(eventType, data);
		}
	}

	/* use this method to cause the EventSystem thread to invoke the EventListener as soon as possible */
	public void notifyTransientEvent(final EventListener listener, final Object data) {
		display.asyncExec(new Runnable() {
			public void run() {
				listener.callback(null, data);
			}
		});
	}
	
//	public static void main(String[] args) {
//		Display display = new Display();
//		Shell shell = new Shell(display, 0);
//		final EventSystem evs = new SWTEventSystem(display, shell);
//		System.out.println("Main thread = "+Thread.currentThread());
//		Thread t = new Thread() {
//			public void run() {
//				System.out.println("Second thread = "+Thread.currentThread());
//				System.out.println("foo");
//				evs.notifyTransientEvent(new EventListener() { public void callback(Object eventType, Object data) { System.out.println("invoking callback in "+Thread.currentThread()); } }, null);
//				evs.stop();
//			}
//		};
//		t.start();
//		evs.run();
//	}
}
