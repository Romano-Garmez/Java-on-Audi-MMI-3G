package org.placelab.spotter;

/**
 * SpotterListeners that also implement this interface will be notified
 * when the background operation for {@link Spotter#scanOnce()} concludes.
 */
public interface ScanOnceListener extends SpotterListener {
    /**
     * Sent when a Spotter doing scanOnce concludes its operation.
     * @param sender the Spotter that has concluded its scanOnce operation
     * @see Spotter#scanOnce()
     * @see Spotter#scanOnce(EventSystem)
     */
	public void endOfScan(Spotter sender);
}
