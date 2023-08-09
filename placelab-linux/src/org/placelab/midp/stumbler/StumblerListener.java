/*
 * Created on Sep 1, 2004
 *
 */
package org.placelab.midp.stumbler;

import org.placelab.core.Measurement;

/**
 * Interface that classes who listen to stumbler updates should implement
 */
public interface StumblerListener {
	public void gotMeasurement(Measurement[] m);
}
