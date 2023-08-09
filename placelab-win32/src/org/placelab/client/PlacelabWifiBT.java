/*
 * Created on 30-Aug-2004
 *
 */
package org.placelab.client;

import org.placelab.client.tracker.IntersectionTracker;
import org.placelab.collections.LinkedList;
import org.placelab.mapper.JDBMMapper;
import org.placelab.spotter.BluetoothSpotter;
import org.placelab.spotter.WiFiSpotter;

/**
  *
 */
public class PlacelabWifiBT extends Placelab {

    public PlacelabWifiBT() {
            spotterList = new LinkedList();
            spotterList.add(new BluetoothSpotter());
            spotterList.add(new WiFiSpotter());
            try {
                mapper = new JDBMMapper();
            } catch (Exception e) {
                System.out.println(e);
            }
            tracker = new IntersectionTracker(mapper);
    }

}
