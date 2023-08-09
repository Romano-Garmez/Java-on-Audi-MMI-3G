/*
 * Created on Jun 20, 2004
 *
 */
package org.placelab.test;

import org.placelab.collections.HashMap;
import org.placelab.collections.LinkedList;
import org.placelab.core.TwoDCoordinate;
import org.placelab.mapper.Beacon;
import org.placelab.mapper.BluetoothBeacon;
import org.placelab.mapper.GSMBeacon;
import org.placelab.mapper.SimpleMapper;
import org.placelab.mapper.WiFiBeacon;

/**
 * 
 *
 */
public class TestMapper extends SimpleMapper {
	/**
	 * Find the Beacon for a single reading
	 */
	private HashMap map=new HashMap();
	
	public TestMapper() {
		super();
	}
	public void addBluetoothBeacon(String address, double lat, double lon) {
	    addBluetoothBeacon(address,new TwoDCoordinate(lat,lon));
	}
	public void addBluetoothBeacon(String address, TwoDCoordinate c) {
	    BluetoothBeacon b = new BluetoothBeacon();
	    b.setBluetoothAddress(address);
	    b.setPosition(c);
	    map.put(b.getId(),b);
	}
	public void addWiFiBeacon(String id, double lat, double lon) {
	    addWiFiBeacon(id,new TwoDCoordinate(lat, lon));
	}
	public void addWiFiBeacon(String id, TwoDCoordinate c) {
		WiFiBeacon b = new WiFiBeacon();
		b.setId(id);
		b.setPosition(c);
		map.put(b.getId(), b);
	}

    public void addGSMBeacon(String cellid, double latitude, double longitude) {
        addGSMBeacon(cellid, new TwoDCoordinate(latitude,longitude));
    }
    public void addGSMBeacon(String cellid, TwoDCoordinate c) {    
        GSMBeacon b = new GSMBeacon();
        b.setCellId(cellid);
        b.setAreaId("");
        b.setMCC("");
        b.setMNC("");
        b.setPosition(c);
        map.put(b.getId(),b);
    }

    public LinkedList findBeacons(String id) {
		Beacon b = (Beacon)map.get(id);
		LinkedList l = new LinkedList();
		if(b == null) System.out.println("TestMapper: cannot find beacon with id " + id);
		l.add(b);
		return l;
	}
	/* (non-Javadoc)
	 * @see org.placelab.mapper.Mapper#overrideOnPut()
	 */
	public boolean overrideOnPut() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
