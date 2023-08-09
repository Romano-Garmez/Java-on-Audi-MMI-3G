package org.placelab.client.tracker;

import org.placelab.core.Measurement;

/**
 * This filters estimateUpdated measurements by either count or
 * time.  
 * So, for instance, you could say that you only want to 
 * get estimateUpdated every 3 seconds or you could filter by
 * count and get only every fifth estimateUpdated.
 * <p>
 * You use this by creating it with your EstimateListener object
 * and then registering this with the Tracker you wish to filter
 * estimateUpdated events from.
 * 
 * 
 */
public class FilteredEstimateListener implements EstimateListener {
	private long interval;
	private int intervalType;
	private long update;
	private EstimateListener listener;
	
	public static final int FILTER_BY_TIME=1;
	public static final int FILTER_BY_NUMBER=1;
	
	/**
	 * Create a FilteredEstimateListener
	 * @param listener the object to receive the estimateUpdated messages
	 * @param interval the minimum time or count between estimateUpdated messages
	 * you want to take
	 * @param intervalType either {@link #FILTER_BY_NUMBER} or {@link #FILTER_BY_TIME}
	 */
	public FilteredEstimateListener(EstimateListener listener, long interval, int intervalType) {
		this.listener = listener;
		this.interval = interval;
		this.intervalType = intervalType;
		
		update = 0L;
	}
	public void estimateUpdated(Tracker t, Estimate e, Measurement m) {
		boolean doUpdate = false;
		if (intervalType == FILTER_BY_TIME) {
			long now = System.currentTimeMillis();
			if (now - update >= interval) {
				doUpdate = true;
				update = now;
			}
		} else {
			update++;
			if (update >= interval) {
				doUpdate = true;
				update = 0;
			}
		}
		if (doUpdate) listener.estimateUpdated(t, e, m);
	}

}
