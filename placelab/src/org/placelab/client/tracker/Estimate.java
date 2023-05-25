/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.client.tracker;

import org.placelab.core.Coordinate;

/**
 * An Estimate is made by Trackers from Measurements.
 */
public interface Estimate {
    /**
     * @return the time when this Estimate was computed
     */
	public long getTimestamp();
    public Coordinate getCoord();
    public String getStdDevAsString();
    public int getStdDevInMeters();
    public void construct(long timestamp, Coordinate position, String stdDevString);
}
