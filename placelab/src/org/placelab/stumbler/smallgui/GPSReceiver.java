package org.placelab.stumbler.smallgui;

import org.placelab.core.Coordinate;
import org.placelab.core.GPSMeasurement;
import org.placelab.core.Measurement;
import org.placelab.spotter.NMEAGPSSpotter;
import org.placelab.spotter.SerialGPSSpotter;
import org.placelab.spotter.SerialJ9GPSSpotter;
import org.placelab.spotter.Spotter;

public class GPSReceiver extends Receiver {
	
	private Coordinate cord = null;
	private int hasLock = GPSMeasurement.DONT_HAVE_A_LOCK;
	
	public GPSReceiver() {
		super(new Class[][]{
				{
					GPSMeasurement.class,
					null
				}
		});
	}
	
	public void gotMeasurementImpl (Spotter s, Measurement m) {
		GPSMeasurement gm = (GPSMeasurement) m;
		cord = gm.getPosition();
		
		hasLock = GPSMeasurement.NO_INFO_RE_A_LOCK;
		
		if (gm.haveALock() == GPSMeasurement.HAVE_A_LOCK)
			hasLock = GPSMeasurement.HAVE_A_LOCK;
		
	}
	
	public int hasLock () {
		return hasLock;
	}
	
	public Coordinate getLastCoordinate () {
		return cord;
	}
	
	public Class[] getSupportedSpotters () {
		return new Class[]{
			SerialGPSSpotter.class,
			SerialJ9GPSSpotter.class,
			NMEAGPSSpotter.class
		};
	}
	
	public String getType () {
		return "GPS";
	}
}
