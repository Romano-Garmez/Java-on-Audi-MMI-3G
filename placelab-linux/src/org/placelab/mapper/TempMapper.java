/*
 * Created on 23-Sep-2004
 *
 */
package org.placelab.mapper;

import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.collections.Map;
import org.placelab.core.Coordinate;

/**
 * This mapper resides purely in memory, so it is NOT persistent.  Use this for debugging.
 */
public class TempMapper extends AbstractMapper {

    Map map;
    public TempMapper() {
        super(false);
        map = new HashMap();
    }

    public Iterator query(Coordinate c1, Coordinate c2) {
        System.err.println("TempMapper: query unsupported");
        return null;
    }

    protected LinkedList findBeaconsImpl(String id) {
        return (LinkedList) map.get(id);
    }

    protected boolean putBeaconsImpl(String id, LinkedList beacons) {
        map.put(id,beacons);
        return true;
    }

    private boolean isOpened = false;
    public boolean open() {
        isOpened = true;
        return true;
    }
    public boolean close() {
        isOpened = false;
        return true;
    }

    public boolean deleteAll() {
        map = new HashMap();
        return true;
    }

    public boolean isOpened() {
        return isOpened;
    }


    public void startBulkPuts() {
    }

    public void endBulkPuts() {
    }

}
