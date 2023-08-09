
package org.placelab.spotter;

/**
 * SpotterExceptions are thrown in response to various errors that
 * Spotters can encounter.  It is meant to be subclassed to provide
 * more specific information.
 * <p>
 * Since a Spotter may not be operating in the same thread as the 
 * rest of an application, SpotterExceptions are propagated using
 * the {@link SpotterListener#spotterExceptionThrown(Spotter, SpotterException)}
 * method, which can be delivered either in the background thread or by
 * using the EventSystem if one is registered with the Spotter.
 */
public class SpotterException extends Exception {
	public SpotterException(String s) {
		super(s);
	}
	public SpotterException(Throwable t) {
		super(t.getMessage());
	}
}
