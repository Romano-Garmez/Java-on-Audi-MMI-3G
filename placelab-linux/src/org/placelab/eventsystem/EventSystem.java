/*
 * Created on Jul 2, 2004
 *
 */
package org.placelab.eventsystem;

/**
 * EventSystem implements a basic system for sending messages between
 * threads.
 * 
 */
public interface EventSystem {
    
    /**
     * Starts the EventSystem running in the current thread.  All
     * callbacks to EventListeners will be delivered in the this thread
     */
	public void run();
	
	/**
	 * Stops the EventSystem.  No more events will be delivered after this
	 * is called.
	 */
	public void stop();

	/**
	 * Add an event to the system to be sent after the specified delay
	 * @param timeoutMillis amount of time to wait before invoking the callback on the EventListener
	 * @param listener the EventListener to be called back
	 * @param data data to pass to the callback of the listener
	 * @return a token for the timer that can be used to remove this event from the EventSystem
	 * @see #removeTimer(Object)
	 */
	public Object addTimer(long timeoutMillis, EventListener listener, Object data);
	
	/**
	 * Remove an event from the EventSystem so that the callback to the EventListener for
	 * the event won't be called.  Has no effect if the callback has already been delivered.
	 * @param token a token returned from {@link #addTimer(long, EventListener, Object)}
	 */
	public void removeTimer(Object token);
	
	/**
	 * Add an EventListener to be notified whenever user defined events of 
	 * <code>eventType</code> are posted to the EventSystem with {@link #notifyEvent}.
	 * <p>
	 * Note that eventTypes are distinguished from one another by their hashCode() 
	 * methods.
	 * @param eventType the type of event to register to listen for
	 * @param listener an EventListener to be called back whenever events of
	 * <code>eventType</code> are posted to the EventSystem
	 * @return a token for the event which can be used to remove the listener
	 * @see #removeEventListener(Object)
	 */
	public Object addEventListener(Object eventType, EventListener listener);
	
	/**
	 * Remove an EventListener that was registered with addEventListener.
	 * Only removes a single EventListener, other EventListeners registered
	 * for the same event are unaffected.
	 * @param token a token returned from {@link #addEventListener(Object, EventListener)}
	 */
	public void removeEventListener(Object token);
	
	/**
	 * Notify all listeners registered through {@link #addEventListener(Object, EventListener)}
	 * for <code>eventType</code> that the event has occured
	 * @param eventType the user defined event that has occured
	 * @param data the data to give to the listener's callback
	 */
	public void notifyEvent(Object eventType, Object data);

	/**
	 * Notify a single event listener with the given data.  Typically this is used
	 * to send a single message, and an optional object between threads
	 */
	public void notifyTransientEvent(EventListener listener, Object data);
}
