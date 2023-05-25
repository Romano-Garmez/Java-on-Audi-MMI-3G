/*
 * Created on 07-Jul-2004
 *
 */
package org.placelab.mapper;

import org.placelab.collections.HashMap;
import org.placelab.core.Coordinate;
import org.placelab.core.Types;

/**
 * 
 *
 */
public class BluetoothBeacon extends Beacon implements NeighborhoodBeacon {
	private String humanReadableName;
	private String bluetoothAddress;
	private Coordinate position;
	private String[] neighbors;
	
	public BluetoothBeacon() {
	}
	
	public void fromHashMap(HashMap keyValues) {
	    bluetoothAddress = (String) keyValues.get(Types.ID);
	    humanReadableName = (String) keyValues.get(Types.HUMANREADABLENAME);
	    position = Types.newCoordinate((String) keyValues.get(Types.LATITUDE), (String) keyValues.get(Types.LONGITUDE));
	}

	public HashMap toHashMap() {
		HashMap map = new HashMap();
		map.put(Types.ID, bluetoothAddress);
		map.put(Types.HUMANREADABLENAME, humanReadableName);
		map.put(Types.LATITUDE, position.getLatitudeAsString());
		map.put(Types.LONGITUDE, position.getLongitudeAsString());
		return map;
	}
		
	public String getType() {
		return Types.BLUETOOTH;
	}
    /**
     * @return Returns the bluetoothAddress.
     */
    public String getBluetoothAddress() {
        return bluetoothAddress;
    }
    /**
     * @param bluetoothAddress The bluetoothAddress to set.
     */
    public void setBluetoothAddress(String bluetoothAddress) {
        this.bluetoothAddress = bluetoothAddress;
    }
    /**
     * @return Returns the humanReadableName.
     */
    public String getHumanReadableName() {
        return humanReadableName;
    }
    /**
     * @param humanReadableName The humanReadableName to set.
     */
    public void setHumanReadableName(String humanReadableName) {
        this.humanReadableName = humanReadableName;
    }
    /**
     * @return Returns the position.
     */
    public Coordinate getPosition() {
        return position;
    }
    /**
     * @param position The position to set.
     */
    public void setPosition(Coordinate position) {
        this.position = position;
    }

    /** Returns the bluetooth address (in general, should be a unique ID)
     * @see org.placelab.mapper.Beacon#getId()
     */
    public String getId() {
        return bluetoothAddress;
    }
    
	public String[] getNeighborhood() {
		return neighbors;
	}
	
	public void setNeighborhood(String[] nbrs) {
		neighbors = nbrs;
	}
	
	public int getMaximumRange() {
	    return 50;
	}
}
