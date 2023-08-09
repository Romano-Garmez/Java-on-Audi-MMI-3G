package org.placelab.stumbler;

import java.util.Hashtable;


/**
 * After the StumblerFunnel pulses, it shoots out updates to all of its updateListeners
 * with a Hashtable where StumblerSpotters key to Vectors of MeasurementPackages
 * 
 */
public interface StumblerFunnelUpdateListener {

    /**
     * This message is sent after every StumblerFunnel has new data available
     * Note that this callback is sent in the thread that the StumblerUpdateListener used to
     * subscribe itself to the StumblerFunnel.
     * @param measurements updates each StumblerSpotter keys to its MeasurementPackages
     * for this update.
     */
    public void stumblerUpdated(Hashtable measurements);
    
}
