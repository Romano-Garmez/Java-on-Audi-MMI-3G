package org.placelab.test;

import org.placelab.collections.LinkedList;
import org.placelab.core.TwoDCoordinate;
import org.placelab.mapper.SimpleMapper;
import org.placelab.mapper.WiFiBeacon;

/**
 * 
 * Used for testing.
 */
public class FakeMapper extends SimpleMapper {

	public static final double LAB_LATITUDE_AP_0 = 40.018;
	public static final double LAB_LONGITUDE_AP_0 = -121.724;

	public static final double LAB_LATITUDE_AP_1 = 40.022;
	public static final double LAB_LONGITUDE_AP_1 = -121.735; //### These should be meters
	

	public LinkedList findBeacons(String uniqueId) {
		WiFiBeacon result;

		if (uniqueId.equals(FakeSpotter.LAB_AP_0_MAC)) {
			result = new WiFiBeacon();
			result.setPosition(new TwoDCoordinate(LAB_LATITUDE_AP_0,LAB_LONGITUDE_AP_0));
		} else if (uniqueId.equals(FakeSpotter.LAB_AP_1_MAC)){
			result = new WiFiBeacon();
			result.setPosition(new TwoDCoordinate(LAB_LATITUDE_AP_1,LAB_LONGITUDE_AP_1));
		} else {
			return null;
		}
		
		LinkedList list = new LinkedList();
		list.add(result);
		return list;
	}


	/* (non-Javadoc)
	 * @see org.placelab.mapper.Mapper#overrideOnPut()
	 */
	public boolean overrideOnPut() {
		// TODO Auto-generated method stub
		return false;
	}
}
